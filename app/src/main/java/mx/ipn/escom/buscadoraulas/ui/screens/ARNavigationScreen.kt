package mx.ipn.escom.buscadoraulas.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ar.core.*
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.delay
import mx.ipn.escom.buscadoraulas.data.model.RouteStep
import mx.ipn.escom.buscadoraulas.data.model.RouteStepType
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel
import mx.ipn.escom.buscadoraulas.ui.viewmodel.NavigationState
import mx.ipn.escom.buscadoraulas.ui.viewmodel.NavigationViewModel
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Pantalla de navegación AR usando ARCore + SceneView 2.2.1.
 *
 * Lifecycle: ARScene nativo con activity+lifecycle vinculado correctamente.
 * No se usa AndroidView ni ARSceneView crudo. Validado físicamente en Infinix.
 *
 * ARROW.GLB — geometría real (inspeccionada del binario):
 *   Eje principal: Y  (min 0.0 → max 0.55 m)
 *   Punta hacia: +Y (arriba en espacio del modelo)
 *   Ancho X/Z: ±0.1 m
 *   → El modelo está parado verticalmente (pin vertical).
 *
 * Para acostarlo sobre el suelo y apuntar hacia adelante:
 *   BASE PITCH: −90° en X  (baja +Y hacia +Z = "hacia adelante")
 *   DIRECTION YAW: ángulo horizontal calculado desde la cámara + delta del paso.
 *
 * ORIENTACIÓN (heading):
 *   Se usa el vector "forward" de la pose de cámara proyectado en el plano XZ,
 *   en lugar de una fórmula de cuaternión simplificada que falla con pitch alto.
 *
 * ESCALA:
 *   El modelo mide 0.55 m en Y. Después de acostar (pitch −90°), el eje largo
 *   pasa a Z. Queremos ~1.0 m de longitud en el mundo AR.
 *
 * POSICIÓN:
 *   Hit test en la región central-baja de la pantalla.
 *   Buscamos un hit que caiga en un plano horizontal a distancia útil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARNavigationScreen(
    mainViewModel: MainViewModel,
    navViewModel: NavigationViewModel,
    onArrived: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val route by mainViewModel.currentRoute.collectAsState()
    val destination by mainViewModel.selectedDestination.collectAsState()
    val navState by navViewModel.navigationState.collectAsState()
    val currentStep by navViewModel.currentStep.collectAsState()

    var isArSupported by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (availability.isSupported) {
            isArSupported = true
            navViewModel.onArInitialized()
        } else {
            isArSupported = false
            navViewModel.onArNotAvailable("Este dispositivo no es compatible con ARCore.")
        }
    }

    LaunchedEffect(route) {
        route?.let { navViewModel.setRoute(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Navegación AR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isArSupported == false || navState is NavigationState.ArNotAvailable -> {
                    ArNotAvailableContent(
                        reason = (navState as? NavigationState.ArNotAvailable)?.reason
                            ?: "ARCore no está disponible en este dispositivo.",
                        onBack = onBack
                    )
                }
                navState is NavigationState.Arrived -> {
                    ArrivedContent(
                        destinationName = destination?.displayName ?: "Destino",
                        onFinish = {
                            mainViewModel.completeNavigation()
                            onArrived()
                        }
                    )
                }
                else -> {
                    ARSceneNativeContent(
                        currentStep = currentStep,
                        onPlaneDetected = { navViewModel.onPlaneDetected() },
                        onNextStepRequested = { navViewModel.nextStep() },
                        onSessionError = { reason -> navViewModel.onArNotAvailable(reason) },
                        state = navState
                    )
                }
            }
        }
    }
}

@Composable
private fun ARSceneNativeContent(
    currentStep: RouteStep?,
    onPlaneDetected: () -> Unit,
    onNextStepRequested: () -> Unit,
    onSessionError: (String) -> Unit,
    state: NavigationState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes()

    val hasPlacedArrow   = remember { mutableStateOf(false) }
    val planeVisible     = remember { mutableStateOf(true) }
    val arrowAnchorNode  = remember { mutableStateOf<AnchorNode?>(null) }
    val noSurfaceMsg     = remember { mutableStateOf(false) }

    LaunchedEffect(currentStep) {
        if (currentStep != null) {
            val old = arrowAnchorNode.value
            if (old != null) {
                childNodes.remove(old)
                old.destroy()
                arrowAnchorNode.value = null
            }
            childNodes.clear()
            hasPlacedArrow.value = false
            noSurfaceMsg.value   = false
            planeVisible.value   = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            arrowAnchorNode.value?.destroy()
            arrowAnchorNode.value = null
            childNodes.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            childNodes = childNodes,
            planeRenderer = planeVisible.value,
            activity = context as? ComponentActivity,
            lifecycle = lifecycleOwner.lifecycle,
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            onSessionFailed = { ex ->
                onSessionError("Error al iniciar sesión AR: ${ex.localizedMessage}")
            },
            onSessionUpdated = { session, frame ->
                if (!hasPlacedArrow.value && arrowAnchorNode.value == null) {
                    session.getAllTrackables(Plane::class.java)
                        .firstOrNull {
                            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                    it.trackingState == TrackingState.TRACKING
                        } ?: return@ARScene

                    val displayMetrics = context.resources.displayMetrics
                    val hitX = displayMetrics.widthPixels / 2f
                    val hitY = displayMetrics.heightPixels * 0.55f

                    val hitResults = frame.hitTest(hitX, hitY)
                    val validHit = hitResults.firstOrNull { hit ->
                        val t = hit.trackable
                        t is Plane && t.isPoseInPolygon(hit.hitPose) &&
                                t.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    } ?: run {
                        noSurfaceMsg.value = true
                        return@ARScene
                    }

                    val camPose = frame.camera.pose
                    val hitPose = validHit.hitPose
                    val dx = hitPose.tx() - camPose.tx()
                    val dz = hitPose.tz() - camPose.tz()
                    val distXZ = sqrt((dx * dx + dz * dz).toDouble()).toFloat()
                    if (distXZ < 0.8f) {
                        return@ARScene
                    }

                    noSurfaceMsg.value = false

                    val heading = cameraHeadingDegrees(frame)
                    val delta   = deltaYawForStep(currentStep?.type)
                    val finalYaw = heading + delta
                    
                    val basePitch = when (currentStep?.type) {
                        RouteStepType.UPSTAIRS -> -20f
                        RouteStepType.DOWNSTAIRS -> -160f
                        else -> -90f
                    }

                    hasPlacedArrow.value = true
                    onPlaneDetected()

                    try {
                        val anchor    = validHit.createAnchor()
                        val anchorNode = AnchorNode(engine, anchor)

                        val modelInstance = modelLoader.createModelInstance("models/arrow.glb")
                        if (modelInstance != null) {
                            modelInstance.materialInstances.forEach {
                                it.setParameter("baseColorFactor", com.google.android.filament.Colors.RgbaType.SRGB, 0.0f, 0.6f, 1.0f, 1.0f)
                            }
                            
                            val modelNode = ModelNode(modelInstance = modelInstance).apply {
                                scale    = Float3(0.6f, 1.2f, 0.03f)
                                rotation = Float3(basePitch, finalYaw, 0f)
                                position = Float3(0f, 0.02f, 0f)
                            }
                            anchorNode.addChildNode(modelNode)
                        }

                        childNodes.add(anchorNode)
                        arrowAnchorNode.value = anchorNode

                    } catch (e: Exception) {
                        e.printStackTrace()
                        hasPlacedArrow.value = false
                    }
                }
            }
        )

        LaunchedEffect(hasPlacedArrow.value) {
            if (hasPlacedArrow.value) {
                delay(1500L)
                planeVisible.value = false
            }
        }

        if (noSurfaceMsg.value && !hasPlacedArrow.value) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Apunta al suelo hasta ver la superficie",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        ARNavigationOverlay(
            state = state,
            currentStep = currentStep,
            onNextStep = { onNextStepRequested() }
        )
    }
}

private fun cameraHeadingDegrees(frame: Frame): Float {
    return try {
        val m = FloatArray(16)
        frame.camera.displayOrientedPose.toMatrix(m, 0)
        val fx = -m[8]
        val fz = -m[10]
        val len = sqrt((fx * fx + fz * fz).toDouble()).toFloat()
        if (len < 1e-6f) return 0f
        val nx = fx / len
        val nz = fz / len
        Math.toDegrees(atan2(nx.toDouble(), nz.toDouble())).toFloat()
    } catch (e: Exception) {
        0f
    }
}

private fun deltaYawForStep(type: RouteStepType?): Float = when (type) {
    RouteStepType.LEFT      -> -90f
    RouteStepType.RIGHT     ->  90f
    RouteStepType.UTURN     -> 180f
    else                    ->   0f
}

@Composable
private fun ARNavigationOverlay(
    state: NavigationState,
    currentStep: RouteStep?,
    onNextStep: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is NavigationState.Initializing,
            NavigationState.DetectingPlane -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "Buscando superficie…",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mueve el teléfono lentamente apuntando al piso.",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is NavigationState.Navigating,
            NavigationState.PlaneDetected -> {
                val stepIndex  = (state as? NavigationState.Navigating)?.currentStepIndex ?: 0
                val totalSteps = (state as? NavigationState.Navigating)?.totalSteps ?: 1
                val isLastStep = stepIndex + 1 >= totalSteps

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val icon = when (currentStep?.type) {
                                RouteStepType.LEFT        -> Icons.Default.TurnLeft
                                RouteStepType.RIGHT       -> Icons.Default.TurnRight
                                RouteStepType.UPSTAIRS    -> Icons.Default.ArrowUpward
                                RouteStepType.DOWNSTAIRS  -> Icons.Default.ArrowDownward
                                RouteStepType.UTURN       -> Icons.Default.Undo
                                RouteStepType.DESTINATION -> Icons.Default.Flag
                                else                      -> Icons.Default.ArrowForward
                            }
                            Icon(icon, contentDescription = null,
                                modifier = Modifier.size(48.dp), tint = Color.White)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentStep?.description ?: "Avanza hacia tu destino",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Paso ${stepIndex + 1} de $totalSteps",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            if (isLastStep) Icons.Default.Flag else Icons.Default.SkipNext,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isLastStep) "Llegué a mi destino" else "Siguiente indicación",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ArrivedContent(destinationName: String, onFinish: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                modifier = Modifier.size(96.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Text("¡Has llegado a $destinationName!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("La ruta se ha completado y se guardó en tu historial.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Finalizar navegación", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ArNotAvailableContent(reason: String, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.ViewInAr, contentDescription = null,
                modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text("AR no disponible",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(reason, style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack) { Text("Volver") }
        }
    }
}
