package mx.ipn.escom.buscadoraulas.data.model

/**
 * Representa una ubicación física dentro del campus ESCOM.
 */
data class EscomLocation(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val floor: Int = 0,
    val building: String = "Principal",
    /** Palabras clave que el OCR puede reconocer para esta ubicación */
    val keywords: List<String> = emptyList()
)
