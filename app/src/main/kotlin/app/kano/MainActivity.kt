package app.kano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.kano.ui.DeviceScreen
import app.kano.ui.HomeScreen
import app.kano.ui.KanoTheme
import app.kano.ui.KanoViewModel
import app.kano.ui.MediaScreen
import app.kano.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanoTheme {
                val model: KanoViewModel = viewModel(factory = KanoViewModel.factory((application as KanoApplication).graph))
                KanoApp(model)
            }
        }
    }
}

@Composable fun KanoApp(model: KanoViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val snackbars = remember { SnackbarHostState() }
    val message by model.message.collectAsStateWithLifecycle()
    val device by model.device.collectAsStateWithLifecycle()
    val navigationColumns = if (LocalDensity.current.fontScale >= 1.5f) 2 else 4
    LaunchedEffect(message) {
        message?.let { snackbars.showSnackbar(it); model.message.value = null }
    }
    val navigate: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbars) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
                listOf("home" to "Home", "device" to "Device", "media" to "Media", "settings" to "Privacy").chunked(navigationColumns).forEach { destinations ->
                    Row(Modifier.fillMaxWidth()) {
                        destinations.forEach { (route, label) ->
                            val active = entry?.destination?.route == route
                            TextButton(onClick = { navigate(route) },
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.textButtonColors(containerColor = if (active) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent),
                                modifier = Modifier.weight(1f).semantics { selected = active; role = Role.Tab }) {
                                Text(label, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen({ navigate("device") }, { navigate("media") }) }
            composable("device") { DeviceScreen(device, model::refreshDevice) }
            composable("media") { MediaScreen(model) }
            composable("settings") { SettingsScreen() }
        }
    }
}
