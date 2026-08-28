package mx.ipn.escom.buscadoraulas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de historial de búsquedas y ubicaciones visitadas.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** ID de la ubicación de origen */
    val fromLocationId: String,
    /** ID de la ubicación destino */
    val toLocationId: String,
    /** Nombre visible del origen */
    val fromLocationName: String,
    /** Nombre visible del destino */
    val toLocationName: String,
    /** Timestamp de cuando se inició la navegación (millis) */
    val timestamp: Long = System.currentTimeMillis(),
    /** Si el usuario llegó al destino */
    val completed: Boolean = false
)
