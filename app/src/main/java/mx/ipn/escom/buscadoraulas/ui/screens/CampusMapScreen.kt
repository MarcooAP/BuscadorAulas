package mx.ipn.escom.buscadoraulas.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val route by viewModel.currentRoute.collectAsState()
    val fromLocation = route?.fromId?.let { viewModel.getLocationById(it) }
    
    var selectedFloor by remember { mutableStateOf(fromLocation?.floor ?: 0) }
    val floors = listOf("Planta Baja", "Piso 1", "Piso 2")

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa del Campus") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedFloor,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                floors.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFloor == index,
                        onClick = { 
                            selectedFloor = index 
                            scale = 1f
                            offset = Offset.Zero
                        },
                        text = { Text(title) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F0F0))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            val newOffset = offset + pan
                            val maxX = 1000f * scale
                            val maxY = 1000f * scale
                            offset = Offset(
                                x = newOffset.x.coerceIn(-maxX, maxX),
                                y = newOffset.y.coerceIn(-maxY, maxY)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val textMeasurer = rememberTextMeasurer()
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceColor = MaterialTheme.colorScheme.surface
                val currentRoute = route
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    val canvasW = 1200f
                    val canvasH = 1200f
                    val startX = (size.width - canvasW) / 2f
                    val startY = (size.height - canvasH) / 2f
                    val nodeCoordinates = mutableMapOf<String, Offset>()
                    
                    if (selectedFloor < 2) {
                        val e4Rect = drawBuilding(
                            "E4", Offset(startX + canvasW*0.3f, startY + canvasH*0.1f), Size(canvasW*0.4f, canvasH*0.15f), 
                            primaryColor, surfaceColor, textMeasurer
                        )
                        if (selectedFloor == 0) {
                            nodeCoordinates["STAIR_E4_F0"] = Offset(e4Rect.left + e4Rect.width/2, e4Rect.bottom - 20f)
                            nodeCoordinates["gestion_escolar"] = drawPoi("Gestión Escolar", Offset(e4Rect.left + 20f, e4Rect.bottom - 40f), textMeasurer)
                            nodeCoordinates["biblioteca"] = drawPoi("Biblioteca", Offset(e4Rect.right - 100f, e4Rect.bottom - 40f), textMeasurer)
                            nodeCoordinates["entrada_principal"] = drawPoi("Entrada Principal", Offset(e4Rect.right + 20f, e4Rect.top + 20f), textMeasurer)
                        } else if (selectedFloor == 1) {
                            nodeCoordinates["STAIR_E4_F1"] = Offset(e4Rect.left + e4Rect.width/2, e4Rect.bottom - 20f)
                            nodeCoordinates["direccion"] = drawPoi("Dirección", Offset(e4Rect.left + e4Rect.width/2 - 40f, e4Rect.bottom - 40f), textMeasurer)
                        }
                    }

                    if (selectedFloor == 0) {
                        drawRect(
                            color = Color(0xFFE0E0E0),
                            topLeft = Offset(startX + canvasW*0.4f, startY + canvasH*0.28f),
                            size = Size(canvasW*0.2f, canvasH*0.15f)
                        )
                        drawPoi("Letras ESCOM", Offset(startX + canvasW*0.43f, startY + canvasH*0.32f), textMeasurer)
                        drawPoi("Asta Bandera", Offset(startX + canvasW*0.43f, startY + canvasH*0.38f), textMeasurer)
                        nodeCoordinates["EXPLANADA"] = Offset(startX + canvasW*0.5f, startY + canvasH*0.34f)
                    }

                    fun buildHallway(b: Int, floor: Int, buildingRect: androidx.compose.ui.geometry.Rect, isMirrored: Boolean) {
                        val yTop = buildingRect.top + 30f
                        val yBottom = buildingRect.bottom - 30f
                        val x = buildingRect.left + buildingRect.width/2
                        
                        drawLine(Color.LightGray, Offset(x, yTop), Offset(x, yBottom), strokeWidth = 4f)
                        
                        for (r in 1..12) {
                            val rId = String.format("%d%d%02d", b, floor, r)
                            val hId = String.format("H_%d%d%02d", b, floor, r)
                            val fraction = (r - 1) / 11f
                            val ptY = yTop + (yBottom - yTop) * fraction
                            val ptX = x
                            nodeCoordinates[hId] = Offset(ptX, ptY)
                            
                            val rx = if (isMirrored) ptX - 25f else ptX + 25f
                            nodeCoordinates[rId] = Offset(rx, ptY)
                            
                            val roomColor = Color(0xFFBBDEFB)
                            val rectWidth = 20f
                            val rectHeight = 15f
                            val rTopLeft = Offset(rx - rectWidth/2, ptY - rectHeight/2)
                            drawRect(roomColor, rTopLeft, Size(rectWidth, rectHeight))
                            drawRect(Color.Gray, rTopLeft, Size(rectWidth, rectHeight), style = Stroke(1f))
                            
                            if (r == 1 || r == 12 || r == 6) {
                                val label = String.format("%02d", r)
                                val textOffset = if (isMirrored) Offset(rx - 30f, ptY - 8f) else Offset(rx + 15f, ptY - 8f)
                                drawText(textMeasurer, label, textOffset, TextStyle(fontSize = 10.sp, color = Color.DarkGray))
                            }
                            
                            if (r == 12 || r == 6) {
                                val bx = if (isMirrored) rx - 20f else rx + 20f
                                drawRect(Color(0xFF81D4FA), Offset(bx - 6f, ptY - 6f), Size(12f, 12f))
                                drawText(textMeasurer, "WC", Offset(bx - 6f, ptY - 12f), TextStyle(fontSize = 6.sp, color = Color.DarkGray))
                            }
                        }
                        
                        val sAId = "STAIR_A_E${b}_F${floor}"
                        val sBId = "STAIR_B_E${b}_F${floor}"
                        val fracA = (9 - 1) / 11f
                        val fracB = (4 - 1) / 11f
                        val saY = yTop + (yBottom - yTop) * fracA
                        val sbY = yTop + (yBottom - yTop) * fracB
                        val stairX = if (isMirrored) x + 25f else x - 25f
                        nodeCoordinates[sAId] = Offset(stairX, saY)
                        nodeCoordinates[sBId] = Offset(stairX, sbY)
                        
                        drawRect(Color(0xFFFFCC80), Offset(stairX - 10f, saY - 10f), Size(20f, 20f))
                        drawText(textMeasurer, "Esc", Offset(stairX - 10f, saY - 20f), TextStyle(fontSize = 8.sp))
                        drawRect(Color(0xFFFFCC80), Offset(stairX - 10f, sbY - 10f), Size(20f, 20f))
                        drawText(textMeasurer, "Esc", Offset(stairX - 10f, sbY - 20f), TextStyle(fontSize = 8.sp))
                    }

                    val e1Rect = drawBuilding(
                        "E1", Offset(startX + canvasW*0.1f, startY + canvasH*0.4f), Size(canvasW*0.15f, canvasH*0.4f),
                        primaryColor, surfaceColor, textMeasurer
                    )
                    buildHallway(1, selectedFloor, e1Rect, false)
                    if (selectedFloor == 0) {
                        nodeCoordinates["entrada_secundaria"] = drawPoi("Entrada Sec.", Offset(e1Rect.left, e1Rect.bottom + 20f), textMeasurer)
                    }

                    val e2Rect = drawBuilding(
                        "E2", Offset(startX + canvasW*0.425f, startY + canvasH*0.55f), Size(canvasW*0.15f, canvasH*0.4f),
                        primaryColor, surfaceColor, textMeasurer
                    )
                    buildHallway(2, selectedFloor, e2Rect, false)
                    if (selectedFloor == 0) {
                        nodeCoordinates["cafeteria"] = drawPoi("Cafetería", Offset(e2Rect.left, e2Rect.bottom + 20f), textMeasurer)
                    }

                    val e3Rect = drawBuilding(
                        "E3", Offset(startX + canvasW*0.75f, startY + canvasH*0.4f), Size(canvasW*0.15f, canvasH*0.4f),
                        primaryColor, surfaceColor, textMeasurer
                    )
                    buildHallway(3, selectedFloor, e3Rect, true)
                    if (selectedFloor == 0) {
                        nodeCoordinates["palapas"] = drawPoi("Palapas", Offset(e3Rect.left, e3Rect.bottom + 20f), textMeasurer)
                        nodeCoordinates["estacionamiento_cic"] = drawPoi("Estacionamiento CIC", Offset(e3Rect.right + 20f, e3Rect.bottom + 80f), textMeasurer)
                    }
                    
                    if (currentRoute != null) {
                        val pathNodes = mx.ipn.escom.buscadoraulas.routing.CampusGraph.findPath(currentRoute.fromId, currentRoute.toId)
                        if (pathNodes != null) {
                            val path = Path()
                            var started = false
                            
                            for (i in pathNodes.indices) {
                                val node = pathNodes[i]
                                val pt = nodeCoordinates[node.id]
                                if (pt != null) {
                                    if (!started) {
                                        path.moveTo(pt.x, pt.y)
                                        started = true
                                    } else {
                                        path.lineTo(pt.x, pt.y)
                                    }
                                } else {
                                    if (i > 0 && pathNodes[i-1].id.startsWith("STAIR")) {
                                        val prevNode = pathNodes[i-1]
                                        val prevPt = nodeCoordinates[prevNode.id]
                                        if (prevPt != null) {
                                            val dirText = if (node.floor > prevNode.floor) "Sube a Piso ${node.floor}" else "Baja a Piso ${node.floor}"
                                            drawText(textMeasurer, dirText, Offset(prevPt.x + 20f, prevPt.y - 10f), TextStyle(color = Color.Magenta, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                    started = false
                                }
                            }
                            drawPath(path, color = Color.Magenta, style = Stroke(width = 8f))
                        }
                        
                        val fromPt = nodeCoordinates[currentRoute.fromId]
                        val toPt = nodeCoordinates[currentRoute.toId]
                        
                        if (fromPt != null) {
                            drawCircle(color = Color.Blue, radius = 16f, center = fromPt)
                            drawText(textMeasurer, "Estás aquí", Offset(fromPt.x + 20f, fromPt.y - 10f), TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                        
                        if (toPt != null) {
                            drawCircle(color = Color.Red, radius = 16f, center = toPt)
                            drawText(textMeasurer, "Destino", Offset(toPt.x + 20f, toPt.y - 10f), TextStyle(color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    } else {
                        val fromPt = fromLocation?.id?.let { nodeCoordinates[it] }
                        if (fromPt != null) {
                            drawCircle(color = Color.Blue, radius = 16f, center = fromPt)
                            drawText(textMeasurer, "Estás aquí", Offset(fromPt.x + 20f, fromPt.y - 10f), TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color.Blue, shape = androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aquí", fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Destino", fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color.Magenta))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ruta", fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFCC80)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Escalera", fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF81D4FA)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WC", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBuilding(
    name: String,
    topLeft: Offset,
    size: Size,
    borderColor: Color,
    fillColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
): androidx.compose.ui.geometry.Rect {
    drawRoundRect(color = fillColor, topLeft = topLeft, size = size, cornerRadius = CornerRadius(12f, 12f))
    drawRoundRect(color = borderColor, topLeft = topLeft, size = size, cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 4f))
    drawText(textMeasurer = textMeasurer, text = name, topLeft = Offset(topLeft.x + size.width / 2 - 30f, topLeft.y + 10f), style = TextStyle(color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold))
    return androidx.compose.ui.geometry.Rect(topLeft, size)
}

private fun DrawScope.drawPoi(
    name: String,
    topLeft: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
): Offset {
    drawText(textMeasurer = textMeasurer, text = name, topLeft = topLeft, style = TextStyle(color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
    return Offset(topLeft.x + 10f, topLeft.y + 10f)
}
