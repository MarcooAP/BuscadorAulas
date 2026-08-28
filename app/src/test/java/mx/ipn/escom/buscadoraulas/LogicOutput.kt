package mx.ipn.escom.buscadoraulas

import mx.ipn.escom.buscadoraulas.routing.EscomRouteGenerator
import org.junit.Test
import java.io.File

class LogicOutput {
    @Test
    fun output() {
        val pairs = listOf(
            Pair("1012", "1001"),
            Pair("2012", "2001"),
            Pair("3012", "3001"),
            Pair("1001", "1012"),
            Pair("3001", "3012"),
            Pair("1012", "1008"),
            Pair("1008", "1012"),
            Pair("1002", "1005"),
            Pair("1009", "1004"),
            Pair("1012", "1108"),
            Pair("1108", "1012"),
            Pair("2212", "2110"),
            Pair("3203", "entrada_principal")
        )
        val sb = StringBuilder()
        for (pair in pairs) {
            sb.appendLine("--- Route: ${pair.first} -> ${pair.second} ---")
            val route = EscomRouteGenerator.generateRoute(pair.first, pair.second)
            route?.steps?.forEach { step ->
                sb.appendLine("  - ${step.type}: ${step.description}")
            }
            sb.appendLine()
        }
        File("test_output.txt").writeText(sb.toString())
    }
}
