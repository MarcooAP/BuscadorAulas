package mx.ipn.escom.buscadoraulas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.ipn.escom.buscadoraulas.ui.theme.ColorTheme
import mx.ipn.escom.buscadoraulas.ui.theme.DarkMode
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val colorTheme by viewModel.colorTheme.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Tema de color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Card {
                Column {
                    ThemeOptionRow(
                        title = "Guinda",
                        subtitle = "Colores institucionales del IPN",
                        selected = colorTheme == ColorTheme.GUINDA,
                        icon = Icons.Default.ColorLens,
                        onClick = { viewModel.setColorTheme(ColorTheme.GUINDA) }
                    )
                    HorizontalDivider()
                    ThemeOptionRow(
                        title = "Azul",
                        subtitle = "Tema azul tecnológico",
                        selected = colorTheme == ColorTheme.AZUL,
                        icon = Icons.Default.ColorLens,
                        onClick = { viewModel.setColorTheme(ColorTheme.AZUL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Modo de pantalla",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Card {
                Column {
                    DarkModeRow(
                        title = "Sistema",
                        subtitle = "Seguir la configuración del dispositivo",
                        selected = darkMode == DarkMode.SYSTEM,
                        icon = Icons.Default.PhoneAndroid,
                        onClick = { viewModel.setDarkMode(DarkMode.SYSTEM) }
                    )
                    HorizontalDivider()
                    DarkModeRow(
                        title = "Claro",
                        subtitle = "Siempre en modo claro",
                        selected = darkMode == DarkMode.LIGHT,
                        icon = Icons.Default.LightMode,
                        onClick = { viewModel.setDarkMode(DarkMode.LIGHT) }
                    )
                    HorizontalDivider()
                    DarkModeRow(
                        title = "Oscuro",
                        subtitle = "Siempre en modo oscuro",
                        selected = darkMode == DarkMode.DARK,
                        icon = Icons.Default.DarkMode,
                        onClick = { viewModel.setDarkMode(DarkMode.DARK) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Acerca de", style = MaterialTheme.typography.titleSmall)
                    Text("Buscador de Aulas — ESCOM IPN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Desarrollado por Marco Antonio Anaya", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Navegación indoor con AR para el campus ESCOM.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun DarkModeRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}
