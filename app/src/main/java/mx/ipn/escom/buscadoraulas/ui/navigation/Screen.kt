package mx.ipn.escom.buscadoraulas.ui.navigation

/**
 * Rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object LocationDetected : Screen("location_detected/{locationId}") {
        fun createRoute(locationId: String) = "location_detected/$locationId"
    }
    object Destination : Screen("destination/{locationId}") {
        fun createRoute(locationId: String) = "destination/$locationId"
    }
    object RouteSummary : Screen("route_summary/{fromId}/{toId}") {
        fun createRoute(fromId: String, toId: String) = "route_summary/$fromId/$toId"
    }
    object ARNavigation : Screen("ar_navigation/{fromId}/{toId}") {
        fun createRoute(fromId: String, toId: String) = "ar_navigation/$fromId/$toId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
    object CampusMap : Screen("campus_map")
}
