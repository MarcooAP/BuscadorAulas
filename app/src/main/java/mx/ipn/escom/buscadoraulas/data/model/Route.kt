package mx.ipn.escom.buscadoraulas.data.model

/**
 * Pasos discretos de navegación para la ruta AR.
 */
enum class RouteStepType {
    FORWARD,
    LEFT,
    RIGHT,
    UPSTAIRS,
    DOWNSTAIRS,
    UTURN,
    DESTINATION
}

/**
 * Un paso individual de una ruta.
 */
data class RouteStep(
    val type: RouteStepType,
    val description: String,
    val distanceMeters: Float = 0f
)

/**
 * Una ruta completa entre dos ubicaciones.
 */
data class Route(
    val fromId: String,
    val toId: String,
    val steps: List<RouteStep>,
    val estimatedMinutes: Int
) {
    val totalSteps: Int get() = steps.size
}
