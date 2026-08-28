package mx.ipn.escom.buscadoraulas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val GuindaLightColorScheme = lightColorScheme(
    primary = GuindaPrimary,
    onPrimary = GuindaOnPrimary,
    primaryContainer = GuindaPrimaryContainer,
    onPrimaryContainer = GuindaOnPrimaryContainer,
    secondary = GuindaSecondary,
    onSecondary = GuindaOnSecondary,
    secondaryContainer = GuindaSecondaryContainer,
    onSecondaryContainer = GuindaOnSecondaryContainer,
    tertiary = GuindaTertiary,
    onTertiary = GuindaOnTertiary,
    tertiaryContainer = GuindaTertiaryContainer,
    onTertiaryContainer = GuindaOnTertiaryContainer,
    background = GuindaBackground,
    onBackground = GuindaOnBackground,
    surface = GuindaSurface,
    onSurface = GuindaOnSurface,
    surfaceVariant = GuindaSurfaceVariant,
    onSurfaceVariant = GuindaOnSurfaceVariant,
)

private val GuindaDarkColorScheme = darkColorScheme(
    primary = GuindaPrimaryDark,
    onPrimary = GuindaOnPrimaryDark,
    primaryContainer = GuindaPrimaryContainerDark,
    onPrimaryContainer = GuindaOnPrimaryContainerDark,
    secondary = GuindaSecondaryDark,
    onSecondary = GuindaOnSecondaryDark,
    secondaryContainer = GuindaSecondaryContainerDark,
    onSecondaryContainer = GuindaOnSecondaryContainerDark,
    tertiary = GuindaTertiaryDark,
    onTertiary = GuindaOnTertiaryDark,
    tertiaryContainer = GuindaTertiaryContainerDark,
    onTertiaryContainer = GuindaOnTertiaryContainerDark,
    background = GuindaBackgroundDark,
    onBackground = GuindaOnBackgroundDark,
    surface = GuindaSurfaceDark,
    onSurface = GuindaOnSurfaceDark,
    surfaceVariant = GuindaSurfaceVariantDark,
    onSurfaceVariant = GuindaOnSurfaceVariantDark,
)

private val AzulLightColorScheme = lightColorScheme(
    primary = AzulPrimary,
    onPrimary = AzulOnPrimary,
    primaryContainer = AzulPrimaryContainer,
    onPrimaryContainer = AzulOnPrimaryContainer,
    secondary = AzulSecondary,
    onSecondary = AzulOnSecondary,
    secondaryContainer = AzulSecondaryContainer,
    onSecondaryContainer = AzulOnSecondaryContainer,
    tertiary = AzulTertiary,
    onTertiary = AzulOnTertiary,
    tertiaryContainer = AzulTertiaryContainer,
    onTertiaryContainer = AzulOnTertiaryContainer,
    background = AzulBackground,
    onBackground = AzulOnBackground,
    surface = AzulSurface,
    onSurface = AzulOnSurface,
    surfaceVariant = AzulSurfaceVariant,
    onSurfaceVariant = AzulOnSurfaceVariant,
)

private val AzulDarkColorScheme = darkColorScheme(
    primary = AzulPrimaryDark,
    onPrimary = AzulOnPrimaryDark,
    primaryContainer = AzulPrimaryContainerDark,
    onPrimaryContainer = AzulOnPrimaryContainerDark,
    secondary = AzulSecondaryDark,
    onSecondary = AzulOnSecondaryDark,
    secondaryContainer = AzulSecondaryContainerDark,
    onSecondaryContainer = AzulOnSecondaryContainerDark,
    tertiary = AzulTertiaryDark,
    onTertiary = AzulOnTertiaryDark,
    tertiaryContainer = AzulTertiaryContainerDark,
    onTertiaryContainer = AzulOnTertiaryContainerDark,
    background = AzulBackgroundDark,
    onBackground = AzulOnBackgroundDark,
    surface = AzulSurfaceDark,
    onSurface = AzulOnSurfaceDark,
    surfaceVariant = AzulSurfaceVariantDark,
    onSurfaceVariant = AzulOnSurfaceVariantDark,
)

data class AppThemeState(
    val colorTheme: ColorTheme = ColorTheme.GUINDA,
    val darkMode: DarkMode = DarkMode.SYSTEM
)

val LocalAppTheme = staticCompositionLocalOf { AppThemeState() }

@Composable
fun BuscadorAulasTheme(
    colorTheme: ColorTheme = ColorTheme.GUINDA,
    darkMode: DarkMode = DarkMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (darkMode) {
        DarkMode.DARK -> true
        DarkMode.LIGHT -> false
        DarkMode.SYSTEM -> systemDark
    }

    val colorScheme = when (colorTheme) {
        ColorTheme.GUINDA -> if (isDark) GuindaDarkColorScheme else GuindaLightColorScheme
        ColorTheme.AZUL -> if (isDark) AzulDarkColorScheme else AzulLightColorScheme
    }

    CompositionLocalProvider(
        LocalAppTheme provides AppThemeState(colorTheme, darkMode)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
