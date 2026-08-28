package mx.ipn.escom.buscadoraulas.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.ipn.escom.buscadoraulas.data.model.Route
import mx.ipn.escom.buscadoraulas.data.model.RouteStep
import mx.ipn.escom.buscadoraulas.data.model.RouteStepType

/**
 * Estado de la pantalla de navegación AR.
 */
sealed class NavigationState {
    object Initializing : NavigationState()
    object DetectingPlane : NavigationState()
    object PlaneDetected : NavigationState()
    data class Navigating(val currentStepIndex: Int, val totalSteps: Int) : NavigationState()
    object Arrived : NavigationState()
    data class ArNotAvailable(val reason: String) : NavigationState()
    data class Error(val message: String) : NavigationState()
}

/**
 * ViewModel para ARNavigationScreen.
 *
 * Expone [currentStep] como StateFlow para que Compose reaccione
 * automáticamente al avanzar de paso y actualice texto, icono y flecha 3D.
 */
class NavigationViewModel : ViewModel() {

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Initializing)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    /** Paso actual expuesto reactivamente — se actualiza en la misma transacción que navigationState */
    private val _currentStep = MutableStateFlow<RouteStep?>(null)
    val currentStep: StateFlow<RouteStep?> = _currentStep.asStateFlow()

    private var route: Route? = null
    private var currentStepIndex = 0

    fun setRoute(r: Route) {
        route = r
        currentStepIndex = 0
        _currentStep.value = r.steps.getOrNull(0)
        _navigationState.value = NavigationState.Initializing
    }

    fun onArInitialized() {
        _navigationState.value = NavigationState.DetectingPlane
    }

    fun onPlaneDetected() {
        val r = route ?: return
        _navigationState.value = NavigationState.Navigating(
            currentStepIndex = currentStepIndex,
            totalSteps = r.steps.size
        )
    }

    fun onArNotAvailable(reason: String) {
        _navigationState.value = NavigationState.ArNotAvailable(reason)
    }

    /**
     * Avanza al siguiente paso.
     * Actualiza [_currentStep] antes de [_navigationState]
     * para que la recomposición reciba el paso correcto.
     */
    fun nextStep() {
        val r = route ?: return
        val nextIndex = currentStepIndex + 1
        if (nextIndex >= r.steps.size) {
            _currentStep.value = null
            _navigationState.value = NavigationState.Arrived
        } else {
            currentStepIndex = nextIndex
            val step = r.steps[nextIndex]
            _currentStep.value = step
            if (step.type == RouteStepType.DESTINATION) {
                _navigationState.value = NavigationState.Arrived
            } else {
                _navigationState.value = NavigationState.Navigating(
                    currentStepIndex = nextIndex,
                    totalSteps = r.steps.size
                )
            }
        }
    }

    fun reset() {
        currentStepIndex = 0
        route = null
        _currentStep.value = null
        _navigationState.value = NavigationState.Initializing
    }
}
