package mx.ipn.escom.buscadoraulas.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ColorTheme {
    GUINDA,
    AZUL
}

enum class DarkMode {
    SYSTEM,
    LIGHT,
    DARK
}

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {

    companion object {
        private val KEY_COLOR_THEME = stringPreferencesKey("color_theme")
        private val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
    }

    val colorTheme: Flow<ColorTheme> = context.themeDataStore.data.map { prefs ->
        val value = prefs[KEY_COLOR_THEME] ?: ColorTheme.GUINDA.name
        runCatching { ColorTheme.valueOf(value) }.getOrDefault(ColorTheme.GUINDA)
    }

    val darkMode: Flow<DarkMode> = context.themeDataStore.data.map { prefs ->
        val value = prefs[KEY_DARK_MODE] ?: DarkMode.SYSTEM.name
        runCatching { DarkMode.valueOf(value) }.getOrDefault(DarkMode.SYSTEM)
    }

    suspend fun setColorTheme(theme: ColorTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_COLOR_THEME] = theme.name
        }
    }

    suspend fun setDarkMode(mode: DarkMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = mode.name
        }
    }
}
