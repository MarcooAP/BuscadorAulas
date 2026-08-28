package mx.ipn.escom.buscadoraulas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.escom.buscadoraulas.ui.navigation.AppNavigation
import mx.ipn.escom.buscadoraulas.ui.theme.BuscadorAulasTheme
import mx.ipn.escom.buscadoraulas.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(applicationContext)
            )
            val colorTheme by mainViewModel.colorTheme.collectAsState()
            val darkMode by mainViewModel.darkMode.collectAsState()

            BuscadorAulasTheme(
                colorTheme = colorTheme,
                darkMode = darkMode
            ) {
                AppNavigation(mainViewModel = mainViewModel)
            }
        }
    }
}