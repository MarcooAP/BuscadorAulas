package mx.ipn.escom.buscadoraulas.routing

import mx.ipn.escom.buscadoraulas.data.model.Route
import mx.ipn.escom.buscadoraulas.data.model.RouteStep
import mx.ipn.escom.buscadoraulas.data.model.RouteStepType

object EscomRouteGenerator {
    
    fun generateRoute(fromId: String, toId: String): Route? {
        if (fromId == toId) {
            return Route(fromId, toId, listOf(RouteStep(RouteStepType.DESTINATION, "Ya te encuentras en este lugar", 0f)), 0)
        }

        val pathNodes = CampusGraph.findPath(fromId, toId) ?: return null
        val steps = mutableListOf<RouteStep>()
        
        var i = 0
        while (i < pathNodes.size - 1) {
            val curr = pathNodes[i]
            val next = pathNodes[i + 1]
            
            // 1. Start inside a room moving to hallway
            if (curr.id.matches(Regex("\\d{4}")) && next.id.startsWith("H_")) {
                val currentRoomNum = curr.id.takeLast(2).toInt()
                // Determine direction based on next target in hallway
                var targetRoomNum = currentRoomNum
                var j = i + 1
                while (j < pathNodes.size) {
                    val p = pathNodes[j]
                    if (p.id.startsWith("H_")) {
                        targetRoomNum = p.id.takeLast(2).toInt()
                    } else if (p.id.startsWith("STAIR")) {
                        val stairSector = if (p.id.contains("STAIR_A")) 9 else 4
                        targetRoomNum = stairSector
                        break
                    } else if (p.id.matches(Regex("\\d{4}"))) {
                        targetRoomNum = p.id.takeLast(2).toInt()
                        break
                    } else if (!p.id.startsWith("H_")) {
                        // Going exterior
                        if (currentRoomNum >= 6) targetRoomNum = 12 else targetRoomNum = 1
                        break
                    }
                    j++
                }
                
                val b = curr.building
                val goingToMenor = targetRoomNum < currentRoomNum
                val goingToMayor = targetRoomNum > currentRoomNum
                
                if (goingToMenor || goingToMayor) {
                    val isE3 = b == 3
                    val turn = if (isE3) {
                        if (goingToMenor) RouteStepType.LEFT else RouteStepType.RIGHT
                    } else {
                        if (goingToMenor) RouteStepType.RIGHT else RouteStepType.LEFT
                    }
                    val dirText = if (turn == RouteStepType.RIGHT) "derecha" else "izquierda"
                    steps.add(RouteStep(turn, "Gira a la $dirText para incorporarte al pasillo", 2f))
                }
                i++
                continue
            }
            
            // 2. Hallway movement compaction
            if (curr.id.startsWith("H_") && next.id.startsWith("H_")) {
                val startRoom = curr.id.takeLast(2).toInt()
                var endH = curr
                var endRoom = startRoom
                var j = i
                while (j < pathNodes.size - 1 && pathNodes[j].id.startsWith("H_") && pathNodes[j+1].id.startsWith("H_")) {
                    if (pathNodes[j].building != pathNodes[j+1].building) break
                    endH = pathNodes[j+1]
                    endRoom = endH.id.takeLast(2).toInt()
                    j++
                }
                if (j > i) {
                    val isMenor = endRoom < startRoom
                    val dir = if (isMenor) "numeración menor" else "numeración mayor"
                    val dist = Math.abs(endRoom - startRoom) * 5f
                    steps.add(RouteStep(RouteStepType.FORWARD, "Avanza por el pasillo hacia $dir hasta el Salón ${endH.id.substring(2)}", dist))
                    i = j
                    continue
                }
            }
            
            // 3. Hallway to Stair
            if (curr.id.startsWith("H_") && next.id.startsWith("STAIR")) {
                steps.add(RouteStep(RouteStepType.FORWARD, "Dirígete a la escalera", 2f))
                i++
                continue
            }
            
            // 4. Stair to Stair (Vertical)
            if (curr.id.startsWith("STAIR") && next.id.startsWith("STAIR") && curr.building == next.building && curr.floor != next.floor) {
                val isUp = next.floor > curr.floor
                val type = if (isUp) RouteStepType.UPSTAIRS else RouteStepType.DOWNSTAIRS
                val floorName = when(next.floor) {
                    0 -> "Planta Baja"
                    1 -> "primer piso"
                    2 -> "segundo piso"
                    else -> "piso ${next.floor}"
                }
                val text = if (isUp) "Sube al $floorName" else "Baja a $floorName"
                steps.add(RouteStep(type, text, 10f))
                i++
                continue
            }
            
            // 5. Stair to Hallway
            if (curr.id.startsWith("STAIR") && next.id.startsWith("H_")) {
                steps.add(RouteStep(RouteStepType.FORWARD, "Incorpórate al pasillo", 2f))
                i++
                continue
            }
            
            // Default edge fallback
            val edge = CampusGraph.edges[curr.id]?.find { it.to == next.id }
            if (edge != null) {
                steps.add(RouteStep(edge.direction, edge.instruction, edge.weight))
            }
            i++
        }
        
        val destNode = CampusGraph.nodes[toId]
        val destName = destNode?.description ?: toId
        steps.add(RouteStep(RouteStepType.DESTINATION, "Has llegado a $destName", 0f))
        
        return Route(fromId, toId, steps, estimateMinutes(steps))
    }

    private fun estimateMinutes(steps: List<RouteStep>): Int {
        val totalMeters = steps.sumOf { it.distanceMeters.toDouble() }
        return ((totalMeters / 60.0) + steps.count {
            it.type == RouteStepType.UPSTAIRS || it.type == RouteStepType.DOWNSTAIRS
        } * 0.5).toInt().coerceAtLeast(1)
    }

    fun allRooms(): List<RoomLocation> = buildList {
        for (b in 1..3) for (f in 0..2) for (r in 1..12) {
            add(RoomLocation(b, f, r))
        }
    }
}
