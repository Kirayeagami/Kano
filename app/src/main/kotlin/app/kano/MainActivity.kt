package app.kano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import app.kano.ui.PersonalCareScreen
import app.kano.ui.SettingsScreen
import app.kano.ui.StyleScreen

data class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

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

@Composable
fun KanoApp(model: KanoViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val snackbars = remember { SnackbarHostState() }
    val message by model.message.collectAsStateWithLifecycle()
    val device by model.device.collectAsStateWithLifecycle()

    val destinations = listOf(
        NavDestination("home", "Home", Icons.Outlined.Home),
        NavDestination("device", "Device", Icons.Outlined.Smartphone),
        NavDestination("media", "Media", Icons.Outlined.PermMedia),
        NavDestination("style", "Style", Icons.Outlined.Checkroom),
        NavDestination("personal_care", "Care", Icons.Outlined.Sanitizer),
        NavDestination("settings", "Privacy", Icons.Outlined.Shield),
    )

    val navigationColumns = if (LocalDensity.current.fontScale >= 1.5f) 3 else 6

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
                destinations.chunked(navigationColumns).forEach { rowDestinations ->
                    Row(Modifier.fillMaxWidth()) {
                        rowDestinations.forEach { dest ->
                            val active = entry?.destination?.route == dest.route
                            TextButton(
                                onClick = { navigate(dest.route) },
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (active) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { selected = active; role = Role.Tab },
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = null,
                                        tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = dest.label,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    openDevice = { navigate("device") },
                    openMedia = { navigate("media") },
                    openStyle = { navigate("style") },
                    openPersonalCare = { navigate("personal_care") },
                )
            }
            composable("device") { DeviceScreen(device, model::refreshDevice) }
            composable("media") { MediaScreen(model) }
            composable("style") { StyleScreen() }
            composable("personal_care") { PersonalCareScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
