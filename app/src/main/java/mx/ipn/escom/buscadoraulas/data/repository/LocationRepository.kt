package mx.ipn.escom.buscadoraulas.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.ipn.escom.buscadoraulas.data.model.EscomLocation
import mx.ipn.escom.buscadoraulas.routing.RoomLocation
import java.io.InputStreamReader

/**
 * Repositorio que lee el catálogo de ubicaciones desde assets/locations.json.
 * También reconoce códigos de salón ESCOM de 4 dígitos (1012, 2110, etc.)
 * sintetizando EscomLocation en tiempo de ejecución, sin necesidad de
 * listar los 108 salones posibles en el JSON.
 */
class LocationRepository(private val context: Context) {

    private val gson = Gson()
    private var cachedLocations: List<EscomLocation>? = null

    /**
     * Obtiene todas las ubicaciones del catálogo JSON.
     * La primera llamada lee el archivo; las posteriores usan la caché.
     */
    fun getAllLocations(): List<EscomLocation> {
        return cachedLocations ?: loadLocationsFromAssets().also {
            cachedLocations = it
        }
    }

    /**
     * Busca una ubicación por su ID.
     * Soporta IDs de salón ESCOM (p. ej., "1012") además de IDs del JSON.
     */
    fun getLocationById(id: String): EscomLocation? {
        val room = RoomLocation.parse(id)
        if (room != null) {
            return EscomLocation(
                id = room.id,
                name = room.id,
                displayName = room.displayName,
                description = "Edificio ${room.building}, ${floorLabel(room.floor)}, Aula ${room.roomNumber}",
                floor = room.floor,
                building = "Edificio ${room.building}",
                keywords = listOf(room.id)
            )
        }
        return getAllLocations().find { it.id == id }
    }

    /**
     * Normaliza texto eliminando acentos y pasando a mayúsculas.
     */
    fun normalizeText(text: String): String {
        return text.uppercase()
            .replace('Á', 'A').replace('É', 'E').replace('Í', 'I')
            .replace('Ó', 'O').replace('Ú', 'U').replace('Ü', 'U')
            .replace('Ñ', 'N')
            .trim()
    }

    /**
     * Intenta encontrar una ubicación que coincida con el texto OCR.
     * Primero busca códigos de salón ESCOM de 4 dígitos (p. ej., "1012"),
     * después compara contra las keywords del catálogo JSON.
     */
    fun matchLocationFromOcr(rawText: String): EscomLocation? {
        // 1. Intentar reconocer un código de salón ESCOM (ABCD)
        val roomLocation = RoomLocation.findInText(rawText)
        if (roomLocation != null) {
            return EscomLocation(
                id = roomLocation.id,
                name = roomLocation.id,
                displayName = roomLocation.displayName,
                description = "Edificio ${roomLocation.building}, ${floorLabel(roomLocation.floor)}, Aula ${roomLocation.roomNumber}",
                floor = roomLocation.floor,
                building = "Edificio ${roomLocation.building}",
                keywords = listOf(roomLocation.id)
            )
        }

        // 2. Buscar en el catálogo JSON por keywords
        val normalized = normalizeText(rawText)
        return getAllLocations().firstOrNull { location ->
            location.keywords.any { keyword ->
                normalized.contains(normalizeText(keyword))
            }
        }
    }

    private fun floorLabel(floor: Int) = when (floor) {
        0 -> "planta baja"
        1 -> "primer piso"
        2 -> "segundo piso"
        else -> "piso $floor"
    }

    private fun loadLocationsFromAssets(): List<EscomLocation> {
        return try {
            val inputStream = context.assets.open("locations.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<EscomLocation>>() {}.type
            gson.fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
