package mx.ipn.escom.buscadoraulas.routing

import mx.ipn.escom.buscadoraulas.data.model.Route
import mx.ipn.escom.buscadoraulas.data.model.RouteStep
import mx.ipn.escom.buscadoraulas.data.model.RouteStepType

/**
 * Motor de rutas con tres niveles de resolución:
 *
 * 1. Ruta directa en routes.json (static)
 * 2. Ruta inversa de routes.json (invertida automáticamente)
 * 3. Generación algorítmica por EscomRouteGenerator (salones ESCOM + POIs)
 * 4. Ruta de 2 pasos via Entrada Principal como hub de emergencia
 */
class RouteEngine(private val routes: List<Route>) {

    fun findRoute(fromId: String, toId: String): Route? {
        if (fromId == toId) return createSameLocationRoute(fromId)

        // 1. Ruta directa en JSON
        val directRoute = routes.find { it.fromId == fromId && it.toId == toId }
        if (directRoute != null) return directRoute

        // 2. Ruta inversa del JSON
        val inverseRoute = routes.find { it.fromId == toId && it.toId == fromId }
        if (inverseRoute != null) return invertRoute(inverseRoute)

        // 3. Generación algorítmica (salones ESCOM y POIs conocidos)
        val generated = EscomRouteGenerator.generateRoute(fromId, toId)
        if (generated != null) return generated

        // 4. Hub via Entrada Principal como fallback
        val hubId = "entrada_principal"
        if (fromId != hubId && toId != hubId) {
            val toHub = findRoute(fromId, hubId)
            val fromHub = findRoute(hubId, toId)
            if (toHub != null && fromHub != null) {
                return combineRoutes(toHub, fromHub)
            }
        }

        return null
    }

    fun getAvailableDestinations(fromId: String): List<String> {
        return routes
            .filter { it.fromId == fromId }
            .map { it.toId }
            .distinct()
    }

    private fun createSameLocationRoute(locationId: String): Route {
        return Route(
            fromId = locationId,
            toId = locationId,
            steps = listOf(
                RouteStep(
                    type = RouteStepType.DESTINATION,
                    description = "Ya te encuentras en este lugar",
                    distanceMeters = 0f
                )
            ),
            estimatedMinutes = 0
        )
    }

    /**
     * Invierte una ruta: LEFT↔RIGHT, UPSTAIRS↔DOWNSTAIRS, FORWARD sigue FORWARD.
     */
    private fun invertRoute(original: Route): Route {
        val invertedSteps = original.steps
            .filter { it.type != RouteStepType.DESTINATION }
            .reversed()
            .map { step ->
                RouteStep(
                    type = when (step.type) {
                        RouteStepType.LEFT      -> RouteStepType.RIGHT
                        RouteStepType.RIGHT     -> RouteStepType.LEFT
                        RouteStepType.UPSTAIRS  -> RouteStepType.DOWNSTAIRS
                        RouteStepType.DOWNSTAIRS-> RouteStepType.UPSTAIRS
                        else                    -> step.type
                    },
                    description = step.description,
                    distanceMeters = step.distanceMeters
                )
            } + RouteStep(
            type = RouteStepType.DESTINATION,
            description = "Has llegado a tu destino",
            distanceMeters = 0f
        )

        return Route(
            fromId = original.toId,
            toId = original.fromId,
            steps = invertedSteps,
            estimatedMinutes = original.estimatedMinutes
        )
    }

    private fun combineRoutes(first: Route, second: Route): Route {
        val combinedSteps = first.steps.filter { it.type != RouteStepType.DESTINATION } +
                second.steps
        return Route(
            fromId = first.fromId,
            toId = second.toId,
            steps = combinedSteps,
            estimatedMinutes = first.estimatedMinutes + second.estimatedMinutes
        )
    }
}
