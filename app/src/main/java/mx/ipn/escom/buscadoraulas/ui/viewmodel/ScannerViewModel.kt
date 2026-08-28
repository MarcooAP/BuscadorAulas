package mx.ipn.escom.buscadoraulas.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.ipn.escom.buscadoraulas.data.model.EscomLocation
import mx.ipn.escom.buscadoraulas.data.repository.LocationRepository

/**
 * Estado del escáner de texto OCR.
 */
sealed class ScannerState {
    object Idle : ScannerState()
    object Scanning : ScannerState()
    data class LocationDetected(val location: EscomLocation, val confidence: Int) : ScannerState()
    object NotRecognized : ScannerState()
    object CameraPermissionDenied : ScannerState()
}

/**
 * ViewModel para ScannerScreen.
 * Gestiona el estado del OCR y la confirmación de ubicación por frames consecutivos.
 */
class ScannerViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    // Número de frames consecutivos que deben confirmar la misma ubicación
    private val CONFIRMATION_FRAMES = 3

    // Conteo de frames consecutivos detectando la misma ubicación
    private var consecutiveFrames = 0
    private var lastDetectedId: String? = null

    // Flag para evitar múltiples reconocimientos simultáneos
    @Volatile
    var isProcessing = false
        private set

    fun startScanning() {
        _scannerState.value = ScannerState.Scanning
    }

    fun onCameraPermissionDenied() {
        _scannerState.value = ScannerState.CameraPermissionDenied
    }

    /**
     * Procesa el texto bruto del OCR.
     * Confirma la ubicación solo cuando se detecta en CONFIRMATION_FRAMES consecutivos.
     */
    fun processOcrText(rawText: String) {
        if (isProcessing) return
        isProcessing = true

        viewModelScope.launch {
            try {
                val location = locationRepository.matchLocationFromOcr(rawText)

                if (location != null) {
                    if (location.id == lastDetectedId) {
                        consecutiveFrames++
                    } else {
                        consecutiveFrames = 1
                        lastDetectedId = location.id
                    }

                    if (consecutiveFrames >= CONFIRMATION_FRAMES) {
                        _scannerState.value = ScannerState.LocationDetected(
                            location = location,
                            confidence = consecutiveFrames
                        )
                    }
                } else {
                    // Texto no coincide con ninguna ubicación conocida
                    if (rawText.isNotBlank() && _scannerState.value !is ScannerState.LocationDetected) {
                        consecutiveFrames = 0
                        lastDetectedId = null
                        _scannerState.value = ScannerState.NotRecognized
                    }
                }
            } finally {
                isProcessing = false
            }
        }
    }

    fun resetScanner() {
        consecutiveFrames = 0
        lastDetectedId = null
        isProcessing = false
        _scannerState.value = ScannerState.Scanning
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScannerViewModel(LocationRepository(context)) as T
        }
    }
}
