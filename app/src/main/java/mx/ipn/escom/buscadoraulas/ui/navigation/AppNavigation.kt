package mx.ipn.escom.buscadoraulas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import mx.ipn.escom.buscadoraulas.ui.screens.*
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel
import mx.ipn.escom.buscadoraulas.ui.viewmodel.NavigationViewModel
import mx.ipn.escom.buscadoraulas.ui.viewmodel.ScannerViewModel

/**
 * Grafo de navegación principal de la aplicación.
 */
@Composable
fun AppNavigation(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val scannerViewModel: ScannerViewModel = viewModel(
        factory = ScannerViewModel.Factory(context)
    )
    val navigationViewModel: NavigationViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // ─── Home ───────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = mainViewModel,
                onScanClick = { navController.navigate(Screen.Scanner.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onMapClick = { navController.navigate(Screen.CampusMap.route) }
            )
        }

        // ─── Campus Map ──────────────────────────────────────
        composable(Screen.CampusMap.route) {
            CampusMapScreen(
                viewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Scanner ─────────────────────────────────────────
        composable(Screen.Scanner.route) {
            ScannerScreen(
                viewModel = scannerViewModel,
                onLocationDetected = { locationId ->
                    navController.navigate(Screen.LocationDetected.createRoute(locationId)) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Location Detected ────────────────────────────────
        composable(
            route = Screen.LocationDetected.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
            LocationDetectedScreen(
                locationId = locationId,
                viewModel = mainViewModel,
                onSelectDestination = {
                    navController.navigate(Screen.Destination.createRoute(locationId))
                },
                onRescan = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Destination ──────────────────────────────────────
        composable(
            route = Screen.Destination.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fromId = backStackEntry.arguments?.getString("locationId") ?: ""
            DestinationScreen(
                viewModel = mainViewModel,
                onDestinationSelected = { destinationId ->
                    mainViewModel.calculateRoute()
                    navController.navigate(Screen.RouteSummary.createRoute(fromId, destinationId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Route Summary ────────────────────────────────────
        composable(
            route = Screen.RouteSummary.route,
            arguments = listOf(
                navArgument("fromId") { type = NavType.StringType },
                navArgument("toId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fromId = backStackEntry.arguments?.getString("fromId") ?: ""
            val toId = backStackEntry.arguments?.getString("toId") ?: ""
            RouteSummaryScreen(
                viewModel = mainViewModel,
                onStartAR = {
                    mainViewModel.startNavigation()
                    navController.navigate(Screen.ARNavigation.createRoute(fromId, toId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── AR Navigation ────────────────────────────────────
        composable(
            route = Screen.ARNavigation.route,
            arguments = listOf(
                navArgument("fromId") { type = NavType.StringType },
                navArgument("toId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val currentRoute by mainViewModel.currentRoute.collectAsState()
            currentRoute?.let { navigationViewModel.setRoute(it) }

            ARNavigationScreen(
                mainViewModel = mainViewModel,
                navViewModel = navigationViewModel,
                onArrived = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── History ──────────────────────────────────────────
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Settings ─────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
