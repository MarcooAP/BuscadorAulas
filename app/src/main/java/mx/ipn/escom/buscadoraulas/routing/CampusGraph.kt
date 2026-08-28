package mx.ipn.escom.buscadoraulas.routing

import mx.ipn.escom.buscadoraulas.data.model.RouteStepType
import java.util.PriorityQueue

data class CampusNode(val id: String, val building: Int, val floor: Int, val description: String)
data class Edge(val to: String, val weight: Float, val direction: RouteStepType, val instruction: String)

object CampusGraph {
    val nodes = mutableMapOf<String, CampusNode>()
    val edges = mutableMapOf<String, MutableList<Edge>>()

    private fun addNode(id: String, b: Int, f: Int, desc: String) {
        nodes[id] = CampusNode(id, b, f, desc)
        edges[id] = mutableListOf()
    }

    private fun addEdge(from: String, to: String, weight: Float, dir: RouteStepType, inst: String) {
        edges[from]?.add(Edge(to, weight, dir, inst))
    }
    
    private fun addBiEdge(from: String, to: String, weight: Float, fDir: RouteStepType, fInst: String, bDir: RouteStepType, bInst: String) {
        addEdge(from, to, weight, fDir, fInst)
        addEdge(to, from, weight, bDir, bInst)
    }

    init {
        for (b in 1..3) {
            for (f in 0..2) {
                // Nodes for each room
                for (r in 1..12) {
                    val rId = String.format("%d%d%02d", b, f, r)
                    addNode(rId, b, f, "Salón $rId")
                }
                // Hallway nodes corresponding to each room
                for (r in 1..12) {
                    val rId = String.format("%d%d%02d", b, f, r)
                    val hId = String.format("H_%d%d%02d", b, f, r)
                    addNode(hId, b, f, "Pasillo frente a Salón $rId")
                    // Conexión Room <-> Hallway
                    addBiEdge(rId, hId, 1f, RouteStepType.FORWARD, "Sal del salón", RouteStepType.FORWARD, "Entra al salón")
                }
                
                // Hallway connections
                for (r in 1..11) {
                    val current = String.format("H_%d%d%02d", b, f, r)
                    val next = String.format("H_%d%d%02d", b, f, r + 1)
                    addEdge(current, next, 5f, RouteStepType.FORWARD, "Pasillo hacia mayor")
                    addEdge(next, current, 5f, RouteStepType.FORWARD, "Pasillo hacia menor")
                }

                // Escaleras
                val sA = "STAIR_A_E${b}_F${f}"
                val sB = "STAIR_B_E${b}_F${f}"
                addNode(sA, b, f, "Escalera A E$b")
                addNode(sB, b, f, "Escalera B E$b")
                
                // Conectadas al pasillo en los sectores 09 y 04
                val h04 = String.format("H_%d%d%02d", b, f, 4)
                val h09 = String.format("H_%d%d%02d", b, f, 9)
                
                addBiEdge(h04, sB, 2f, RouteStepType.FORWARD, "Dirígete a la escalera", RouteStepType.FORWARD, "Incorpórate al pasillo")
                addBiEdge(h09, sA, 2f, RouteStepType.FORWARD, "Dirígete a la escalera", RouteStepType.FORWARD, "Incorpórate al pasillo")

                // Conexión vertical
                if (f > 0) {
                    val sADown = "STAIR_A_E${b}_F${f-1}"
                    val sBDown = "STAIR_B_E${b}_F${f-1}"
                    addBiEdge(sA, sADown, 10f, RouteStepType.DOWNSTAIRS, "Baja las escaleras", RouteStepType.UPSTAIRS, "Sube las escaleras")
                    addBiEdge(sB, sBDown, 10f, RouteStepType.DOWNSTAIRS, "Baja las escaleras", RouteStepType.UPSTAIRS, "Sube las escaleras")
                }
            }
        }
        
        // Exteriors and POIs
        addNode("EXPLANADA", 0, 0, "Explanada central")
        addNode("entrada_principal", 4, 0, "Entrada Principal")
        addNode("gestion_escolar", 4, 0, "Gestión Escolar")
        addNode("biblioteca", 4, 0, "Biblioteca")
        addNode("direccion", 4, 1, "Dirección")
        addNode("STAIR_E4_F0", 4, 0, "Escalera E4 PB")
        addNode("STAIR_E4_F1", 4, 1, "Escalera E4 P1")
        
        addBiEdge("STAIR_E4_F0", "STAIR_E4_F1", 10f, RouteStepType.UPSTAIRS, "Sube a Dirección", RouteStepType.DOWNSTAIRS, "Baja a PB")
        addBiEdge("STAIR_E4_F1", "direccion", 5f, RouteStepType.FORWARD, "Avanza hacia Dirección", RouteStepType.FORWARD, "Avanza a escaleras")
        
        addBiEdge("EXPLANADA", "STAIR_E4_F0", 15f, RouteStepType.FORWARD, "Cruza hacia Edificio 4", RouteStepType.FORWARD, "Sal a Explanada")
        addBiEdge("STAIR_E4_F0", "gestion_escolar", 10f, RouteStepType.LEFT, "Gira a la izquierda hacia Gestión Escolar", RouteStepType.FORWARD, "Sal al vestíbulo")
        addBiEdge("STAIR_E4_F0", "biblioteca", 10f, RouteStepType.RIGHT, "Gira a la derecha hacia Biblioteca", RouteStepType.FORWARD, "Sal al vestíbulo")
        addBiEdge("STAIR_E4_F0", "entrada_principal", 20f, RouteStepType.RIGHT, "Dirígete hacia Entrada Principal", RouteStepType.FORWARD, "Ingresa al campus")
        
        addBiEdge("H_1001", "H_2001", 15f, RouteStepType.RIGHT, "Cruza hacia Edificio 2", RouteStepType.LEFT, "Cruza hacia Edificio 1")
        addBiEdge("H_2001", "EXPLANADA", 10f, RouteStepType.FORWARD, "Avanza hacia Explanada", RouteStepType.FORWARD, "Avanza hacia Edificio 2")
        addBiEdge("EXPLANADA", "H_3001", 15f, RouteStepType.RIGHT, "Cruza hacia Edificio 3", RouteStepType.LEFT, "Cruza hacia Explanada")
        
        addNode("entrada_secundaria", 0, 0, "Entrada Secundaria")
        addNode("cafeteria", 0, 0, "Cafetería")
        addNode("palapas", 0, 0, "Palapas")
        addNode("estacionamiento_cic", 0, 0, "Estacionamiento CIC")
        
        addBiEdge("H_1012", "entrada_secundaria", 10f, RouteStepType.FORWARD, "Sal por el fondo hacia Entrada Secundaria", RouteStepType.FORWARD, "Entra por el pasillo del Edificio 1")
        addBiEdge("H_2012", "cafeteria", 10f, RouteStepType.FORWARD, "Sal por el fondo hacia Cafetería", RouteStepType.FORWARD, "Entra por el pasillo del Edificio 2")
        addBiEdge("H_3012", "palapas", 10f, RouteStepType.FORWARD, "Sal por el fondo hacia Palapas", RouteStepType.FORWARD, "Entra por el pasillo del Edificio 3")
        addBiEdge("palapas", "estacionamiento_cic", 25f, RouteStepType.FORWARD, "Avanza hacia Estacionamiento CIC", RouteStepType.FORWARD, "Avanza hacia Palapas")
    }

    fun findPath(startId: String, endId: String): List<CampusNode>? {
        if (!nodes.containsKey(startId) || !nodes.containsKey(endId)) return null
        val dist = mutableMapOf<String, Float>().withDefault { Float.POSITIVE_INFINITY }
        val prev = mutableMapOf<String, String>()
        dist[startId] = 0f
        val pq = PriorityQueue<Pair<String, Float>>(compareBy { it.second })
        pq.add(Pair(startId, 0f))

        while (pq.isNotEmpty()) {
            val element = pq.poll() ?: break
            val u = element.first
            val d = element.second
            if (u == endId) break
            if (d > dist.getValue(u)) continue

            edges[u]?.forEach { edge ->
                val v = edge.to
                val alt = d + edge.weight
                if (alt < dist.getValue(v)) {
                    dist[v] = alt
                    prev[v] = u
                    pq.add(Pair(v, alt))
                }
            }
        }
        if (!prev.containsKey(endId)) return null

        val path = mutableListOf<String>()
        var curr = endId
        path.add(curr)
        while (curr != startId) {
            curr = prev[curr]!!
            path.add(0, curr)
        }
        return path.map { nodes[it]!! }
    }
}
