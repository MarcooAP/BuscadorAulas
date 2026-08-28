package mx.ipn.escom.buscadoraulas

import org.junit.Test
import org.junit.Assert.*
import mx.ipn.escom.buscadoraulas.data.model.Route
import mx.ipn.escom.buscadoraulas.data.model.RouteStep
import mx.ipn.escom.buscadoraulas.data.model.RouteStepType
import mx.ipn.escom.buscadoraulas.routing.RouteEngine

/**
 * Pruebas unitarias para RouteEngine.
 */
class RouteEngineTest {

    private fun makeStep(type: RouteStepType) = RouteStep(type, "descripcion", 5f)

    private val sampleRoutes = listOf(
        Route(
            fromId = "entrada_principal",
            toId = "cafeteria",
            steps = listOf(
                makeStep(RouteStepType.FORWARD),
                makeStep(RouteStepType.LEFT),
                makeStep(RouteStepType.DESTINATION)
            ),
            estimatedMinutes = 2
        ),
        Route(
            fromId = "entrada_principal",
            toId = "direccion",
            steps = listOf(
                makeStep(RouteStepType.FORWARD),
                makeStep(RouteStepType.RIGHT),
                makeStep(RouteStepType.FORWARD),
                makeStep(RouteStepType.DESTINATION)
            ),
            estimatedMinutes = 3
        ),
        Route(
            fromId = "cafeteria",
            toId = "entrada_principal",
            steps = listOf(
                makeStep(RouteStepType.RIGHT),
                makeStep(RouteStepType.FORWARD),
                makeStep(RouteStepType.DESTINATION)
            ),
            estimatedMinutes = 2
        )
    )

    private val engine = RouteEngine(sampleRoutes)

    @Test
    fun `ruta directa encontrada correctamente`() {
        val route = engine.findRoute("entrada_principal", "cafeteria")
        assertNotNull("Debe encontrar ruta directa", route)
        assertEquals("entrada_principal", route!!.fromId)
        assertEquals("cafeteria", route.toId)
    }

    @Test
    fun `ruta misma ubicacion devuelve DESTINATION`() {
        val route = engine.findRoute("cafeteria", "cafeteria")
        assertNotNull(route)
        assertEquals(1, route!!.steps.size)
        assertEquals(RouteStepType.DESTINATION, route.steps[0].type)
    }

    @Test
    fun `ruta inversa calculada cuando no hay directa`() {
        val route = engine.findRoute("cafeteria", "direccion")
        assertNotNull("Debe encontrar ruta via hub", route)
        assertEquals("cafeteria", route!!.fromId)
        assertEquals("direccion", route.toId)
    }

    @Test
    fun `ultimo paso es siempre DESTINATION`() {
        val route = engine.findRoute("entrada_principal", "direccion")
        assertNotNull(route)
        assertEquals(RouteStepType.DESTINATION, route!!.steps.last().type)
    }

    @Test
    fun `ruta inexistente devuelve null`() {
        val route = engine.findRoute("salon_4b", "laboratorio_redes")
        assertNull("Debe devolver null si no hay camino", route)
    }

    @Test
    fun `ruta tiene pasos no vacios`() {
        val route = engine.findRoute("entrada_principal", "cafeteria")
        assertNotNull(route)
        assertTrue("La ruta debe tener pasos", route!!.steps.isNotEmpty())
    }
}
