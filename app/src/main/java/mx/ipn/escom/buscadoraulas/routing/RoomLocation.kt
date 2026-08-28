package mx.ipn.escom.buscadoraulas.routing

/**
 * Representa la ubicación decodificada de un salón ESCOM con la nomenclatura ABCD.
 *
 * Formato: ABCD donde:
 *   A = edificio (1..3)
 *   B = nivel (0 = planta baja, 1 = primer piso, 2 = segundo piso)
 *   CD = número de aula (01..12)
 *
 * Ejemplos válidos: 1012, 1105, 1201, 2010, 2111, 2204, 3002, 3112, 3201
 */
data class RoomLocation(
    val building: Int,       // 1..3
    val floor: Int,          // 0..2
    val roomNumber: Int      // 1..12
) {
    /** ID canónico para usar como locationId en el grafo de rutas */
    val id: String get() = "${building}${floor}${roomNumber.toString().padStart(2, '0')}"

    /** Nombre para mostrar al usuario */
    val displayName: String
        get() = "Salón ${building}${floor}${roomNumber.toString().padStart(2, '0')}"

    /** Hub lógico de planta baja del edificio (para travesías entre edificios) */
    val groundFloorHubId: String get() = "hub_e${building}_pb"

    companion object {
        /**
         * Intenta parsear un código de salón ESCOM de 4 dígitos.
         * Retorna null si el texto no corresponde a un salón ESCOM válido.
         */
        fun parse(code: String): RoomLocation? {
            val trimmed = code.trim()
            // Solo acepta exactamente 4 dígitos
            if (!trimmed.matches(Regex("\\d{4}"))) return null

            val building = trimmed[0].digitToInt()
            val floor = trimmed[1].digitToInt()
            val roomNumber = trimmed.substring(2).toInt()

            if (building !in 1..3) return null
            if (floor !in 0..2) return null
            if (roomNumber !in 1..12) return null

            return RoomLocation(building, floor, roomNumber)
        }

        /**
         * Busca un código de salón de 4 dígitos dentro de un texto más largo (p. ej., output del OCR).
         * Retorna el primer match válido encontrado.
         */
        fun findInText(text: String): RoomLocation? {
            return Regex("\\b\\d{4}\\b")
                .findAll(text)
                .mapNotNull { parse(it.value) }
                .firstOrNull()
        }
    }
}
