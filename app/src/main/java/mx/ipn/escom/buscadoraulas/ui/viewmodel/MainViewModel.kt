package mx.ipn.escom.buscadoraulas.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.ipn.escom.buscadoraulas.data.local.AppDatabase
import mx.ipn.escom.buscadoraulas.data.local.HistoryEntity
import mx.ipn.escom.buscadoraulas.data.model.EscomLocation
import mx.ipn.escom.buscadoraulas.data.model.Route
import mx.ipn.escom.buscadoraulas.data.repository.HistoryRepository
import mx.ipn.escom.buscadoraulas.data.repository.LocationRepository
import mx.ipn.escom.buscadoraulas.data.repository.RouteRepository
import mx.ipn.escom.buscadoraulas.routing.RouteEngine
import mx.ipn.escom.buscadoraulas.ui.theme.ColorTheme
import mx.ipn.escom.buscadoraulas.ui.theme.DarkMode
import mx.ipn.escom.buscadoraulas.ui.theme.ThemePreferences

/**
 * ViewModel principal que gestiona el estado global de la app:
 * - Ubicación actual detectada por OCR
 * - Destino seleccionado
 * - Ruta calculada
 * - Preferencias de tema
 * - Historial
 */
class MainViewModel(
    private val locationRepository: LocationRepository,
    private val routeRepository: RouteRepository,
    private val historyRepository: HistoryRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    // ─── Tema ───────────────────────────────────────────────
    val colorTheme: StateFlow<ColorTheme> = themePreferences.colorTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ColorTheme.GUINDA)

    val darkMode: StateFlow<DarkMode> = themePreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DarkMode.SYSTEM)

    fun setColorTheme(theme: ColorTheme) {
        viewModelScope.launch { themePreferences.setColorTheme(theme) }
    }

    fun setDarkMode(mode: DarkMode) {
        viewModelScope.launch { themePreferences.setDarkMode(mode) }
    }

    // ─── Ubicación actual ────────────────────────────────────
    private val _currentLocation = MutableStateFlow<EscomLocation?>(null)
    val currentLocation: StateFlow<EscomLocation?> = _currentLocation.asStateFlow()

    fun setCurrentLocation(location: EscomLocation) {
        _currentLocation.value = location
    }

    // ─── Destino seleccionado ────────────────────────────────
    private val _selectedDestination = MutableStateFlow<EscomLocation?>(null)
    val selectedDestination: StateFlow<EscomLocation?> = _selectedDestination.asStateFlow()

    fun setDestination(location: EscomLocation) {
        _selectedDestination.value = location
    }

    // ─── Ruta calculada ──────────────────────────────────────
    private val _currentRoute = MutableStateFlow<Route?>(null)
    val currentRoute: StateFlow<Route?> = _currentRoute.asStateFlow()

    private val _routeError = MutableStateFlow<String?>(null)
    val routeError: StateFlow<String?> = _routeError.asStateFlow()

    fun calculateRoute() {
        val from = _currentLocation.value ?: return
        val to = _selectedDestination.value ?: return

        val allRoutes = routeRepository.getAllRoutes()
        val engine = RouteEngine(allRoutes)
        val route = engine.findRoute(from.id, to.id)

        if (route != null) {
            _currentRoute.value = route
            _routeError.value = null
        } else {
            _routeError.value = "No hay ruta disponible entre ${from.displayName} y ${to.displayName}"
        }
    }

    // ─── Historial de navegación ─────────────────────────────
    private var currentNavigationId: Long = -1L

    val history: StateFlow<List<HistoryEntity>> = historyRepository
        .getRecentHistory(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNavigation() {
        val from = _currentLocation.value ?: return
        val to = _selectedDestination.value ?: return
        viewModelScope.launch {
            currentNavigationId = historyRepository.saveNavigation(
                fromId = from.id,
                toId = to.id,
                fromName = from.displayName,
                toName = to.displayName
            )
        }
    }

    fun completeNavigation() {
        if (currentNavigationId >= 0) {
            viewModelScope.launch {
                historyRepository.markCompleted(currentNavigationId)
            }
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch { historyRepository.deleteEntry(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clearHistory() }
    }

    // ─── Catálogo de ubicaciones ─────────────────────────────
    fun getAllLocations(): List<EscomLocation> = locationRepository.getAllLocations()

    fun getLocationById(id: String): EscomLocation? = locationRepository.getLocationById(id)

    /**
     * Devuelve los 108 salones ESCOM generados algorítmicamente
     * como [EscomLocation] para poder mostrarlos en la UI de selección de destino.
     */
    fun getAllRooms(): List<EscomLocation> {
        return mx.ipn.escom.buscadoraulas.routing.EscomRouteGenerator.allRooms().map { room ->
            EscomLocation(
                id = room.id,
                name = room.id,
                displayName = room.displayName,
                description = "Edificio ${room.building}, ${floorLabel(room.floor)}",
                floor = room.floor,
                building = "Edificio ${room.building}",
                keywords = listOf(room.id)
            )
        }
    }

    private fun floorLabel(floor: Int) = when (floor) {
        0    -> "planta baja"
        1    -> "primer piso"
        2    -> "segundo piso"
        else -> "piso $floor"
    }

    // ─── Reset de flujo ──────────────────────────────────────
    fun resetFlow() {
        _currentLocation.value = null
        _selectedDestination.value = null
        _currentRoute.value = null
        _routeError.value = null
        currentNavigationId = -1L
    }

    // ─── Factory ─────────────────────────────────────────────
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return MainViewModel(
                locationRepository = LocationRepository(context),
                routeRepository = RouteRepository(context),
                historyRepository = HistoryRepository(db),
                themePreferences = ThemePreferences(context)
            ) as T
        }
    }
}
