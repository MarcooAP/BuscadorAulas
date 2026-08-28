package mx.ipn.escom.buscadoraulas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.ipn.escom.buscadoraulas.data.model.RouteStepType
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSummaryScreen(
    viewModel: MainViewModel,
    onStartAR: () -> Unit,
    onBack: () -> Unit
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val destination by viewModel.selectedDestination.collectAsState()
    val route by viewModel.currentRoute.collectAsState()
    val routeError by viewModel.routeError.collectAsState()

    LaunchedEffect(currentLocation, destination) {
        if (currentLocation != null && destination != null && route == null) {
            viewModel.calculateRoute()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de ruta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Desde", style = MaterialTheme.typography.labelSmall)
                        Text(
                            currentLocation?.displayName ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hasta", style = MaterialTheme.typography.labelSmall)
                        Text(
                            destination?.displayName ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            when {
                routeError != null -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                routeError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                route != null -> {
                    val r = route!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoChip(
                            icon = Icons.Default.Timer,
                            text = "~${r.estimatedMinutes} min"
                        )
                        InfoChip(
                            icon = Icons.Default.LinearScale,
                            text = "${r.steps.size} pasos"
                        )
                    }

                    Text(
                        "Instrucciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(r.steps) { index, step ->
                            StepCard(
                                index = index + 1,
                                description = step.description,
                                type = step.type,
                                isLast = index == r.steps.lastIndex
                            )
                        }
                    }

                    Button(
                        onClick = onStartAR,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar navegación AR", style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StepCard(
    index: Int,
    description: String,
    type: RouteStepType,
    isLast: Boolean
) {
    val icon = when (type) {
        RouteStepType.FORWARD     -> Icons.Default.ArrowUpward
        RouteStepType.LEFT        -> Icons.Default.TurnLeft
        RouteStepType.RIGHT       -> Icons.Default.TurnRight
        RouteStepType.UPSTAIRS    -> Icons.Default.ArrowUpward
        RouteStepType.DOWNSTAIRS  -> Icons.Default.ArrowDownward
        RouteStepType.UTURN       -> Icons.Default.Undo
        RouteStepType.DESTINATION -> Icons.Default.Flag
    }
    val containerColor = if (type == RouteStepType.DESTINATION)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLast) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "$index",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}
