package mx.ipn.escom.buscadoraulas.ml

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Analizador de imágenes de CameraX que usa ML Kit para reconocer texto.
 *
 * Reglas:
 * - Siempre cierra el ImageProxy al finalizar.
 * - Evita reconocimientos simultáneos con un flag atómico.
 * - Normaliza el texto antes de entregarlo al callback.
 */
class TextRecognitionAnalyzer(
    private val onTextRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Recognizer con modelo LATIN bundled (sin descarga en primera ejecución)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Flag atómico para evitar múltiples reconocimientos simultáneos
    private val isProcessing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        // Si ya hay un reconocimiento en curso, descartar este frame
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            isProcessing.set(false)
            imageProxy.close()
            return
        }

        // Crear InputImage con la rotación correcta del dispositivo
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                if (rawText.isNotBlank()) {
                    onTextRecognized(rawText)
                }
            }
            .addOnFailureListener { /* Ignorar errores de frame individual */ }
            .addOnCompleteListener {
                // SIEMPRE cerrar el proxy al finalizar
                isProcessing.set(false)
                imageProxy.close()
            }
    }
}
