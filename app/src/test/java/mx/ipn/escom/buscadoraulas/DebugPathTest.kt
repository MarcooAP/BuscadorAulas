package mx.ipn.escom.buscadoraulas

import mx.ipn.escom.buscadoraulas.routing.CampusGraph
import org.junit.Test

class DebugPathTest {
    @Test
    fun printPath() {
        val path = CampusGraph.shortestPath("1012", "biblioteca")
        println(path.joinToString(" -> ") { it.id })
    }
}
