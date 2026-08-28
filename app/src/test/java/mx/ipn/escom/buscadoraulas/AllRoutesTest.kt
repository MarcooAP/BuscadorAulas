package mx.ipn.escom.buscadoraulas

import mx.ipn.escom.buscadoraulas.routing.EscomRouteGenerator
import org.junit.Test
import java.io.File

class AllRoutesTest {
    @Test
    fun output() {
        val pairs = listOf(
            Pair("1012", "1001"),
            Pair("1001", "1012"),
            Pair("2012", "2001"),
            Pair("3012", "3001"),
            Pair("3001", "3012"),
            Pair("1012", "1112"),
            Pair("1112", "1012"),
            Pair("1012", "1212"),
            Pair("1212", "1012"),
            Pair("1012", "2012"),
            Pair("1012", "3012"),
            Pair("1012", "entrada_secundaria"),
            Pair("1012", "entrada_principal"),
            Pair("1012", "gestion_escolar"),
            Pair("1012", "biblioteca"),
            Pair("1012", "direccion"),
            Pair("1012", "cafeteria"),
            Pair("1012", "palapas"),
            Pair("1012", "estacionamiento_cic")
        )
        
        val sb = StringBuilder()
        for (pair in pairs) {
            sb.appendLine("--- Route: ${pair.first} -> ${pair.second} ---")
            val route = EscomRouteGenerator.generateRoute(pair.first, pair.second)
            route?.steps?.forEach { step ->
                sb.appendLine("  - ${step.type}: ${step.description}")
            }
            sb.appendLine("")
        }
        File("test_all_routes.txt").writeText(sb.toString())
    }
}
