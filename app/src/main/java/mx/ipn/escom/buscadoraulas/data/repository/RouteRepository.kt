package mx.ipn.escom.buscadoraulas.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.ipn.escom.buscadoraulas.data.model.Route
import java.io.InputStreamReader

/**
 * DTO para deserializar el JSON de rutas.
 */
private data class RouteDto(
    val fromId: String,
    val toId: String,
    val estimatedMinutes: Int,
    val steps: List<RouteStepDto>
)

private data class RouteStepDto(
    val type: String,
    val description: String,
    val distanceMeters: Float
)

/**
 * Repositorio que carga las rutas desde assets/routes.json
 * y provee acceso al RouteEngine para calcular caminos.
 */
class RouteRepository(private val context: Context) {

    private val gson = Gson()
    private var cachedRoutes: List<Route>? = null

    fun getAllRoutes(): List<Route> {
        return cachedRoutes ?: loadRoutesFromAssets().also { cachedRoutes = it }
    }

    fun findRoute(fromId: String, toId: String): Route? {
        return getAllRoutes().find { it.fromId == fromId && it.toId == toId }
    }

    private fun loadRoutesFromAssets(): List<Route> {
        return try {
            val inputStream = context.assets.open("routes.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<RouteDto>>() {}.type
            val dtos: List<RouteDto> = gson.fromJson(reader, type) ?: emptyList()
            dtos.map { it.toRoute() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun RouteDto.toRoute(): Route {
        val routeSteps = steps.map { stepDto ->
            mx.ipn.escom.buscadoraulas.data.model.RouteStep(
                type = mx.ipn.escom.buscadoraulas.data.model.RouteStepType.valueOf(stepDto.type),
                description = stepDto.description,
                distanceMeters = stepDto.distanceMeters
            )
        }
        return Route(
            fromId = fromId,
            toId = toId,
            steps = routeSteps,
            estimatedMinutes = estimatedMinutes
        )
    }
}
