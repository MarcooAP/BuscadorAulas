package mx.ipn.escom.buscadoraulas.data.repository

import kotlinx.coroutines.flow.Flow
import mx.ipn.escom.buscadoraulas.data.local.AppDatabase
import mx.ipn.escom.buscadoraulas.data.local.HistoryEntity

/**
 * Repositorio para el historial de navegación (CRUD sobre Room).
 */
class HistoryRepository(private val database: AppDatabase) {

    fun getAllHistory(): Flow<List<HistoryEntity>> =
        database.historyDao().getAllHistory()

    fun getRecentHistory(limit: Int = 20): Flow<List<HistoryEntity>> =
        database.historyDao().getRecentHistory(limit)

    suspend fun saveNavigation(
        fromId: String,
        toId: String,
        fromName: String,
        toName: String
    ): Long {
        val entity = HistoryEntity(
            fromLocationId = fromId,
            toLocationId = toId,
            fromLocationName = fromName,
            toLocationName = toName
        )
        return database.historyDao().insertHistory(entity)
    }

    suspend fun markCompleted(id: Long) {
        database.historyDao().markAsCompleted(id)
    }

    suspend fun deleteEntry(id: Long) {
        database.historyDao().deleteHistory(id)
    }

    suspend fun clearHistory() {
        database.historyDao().clearAll()
    }
}
