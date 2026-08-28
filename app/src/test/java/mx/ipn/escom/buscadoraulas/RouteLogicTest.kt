package mx.ipn.escom.buscadoraulas

import mx.ipn.escom.buscadoraulas.routing.EscomRouteGenerator
import org.junit.Test
import org.junit.Assert.*

class RouteLogicTest {
    @Test
    fun testLogicalRoutes() {
        val pairs = listOf(
            Pair("1012", "1108"),
            Pair("1012", "biblioteca"),
            Pair("1012", "direccion"),
            Pair("1012", "cafeteria"),
            Pair("1012", "palapas"),
            Pair("1012", "estacionamiento_cic"),
            Pair("3012", "3001"),
            Pair("2012", "2001"),
            Pair("2212", "2110"),
            Pair("3203", "entrada_principal")
        )
        
        for (pair in pairs) {
            println("--- Route: ${pair.first} -> ${pair.second} ---")
            val route = EscomRouteGenerator.generateRoute(pair.first, pair.second)
            assertNotNull("Route should not be null", route)
            route!!.steps.forEach { step ->
                println("  - ${step.type}: ${step.description}")
            }
            println()
        }
    }
}
