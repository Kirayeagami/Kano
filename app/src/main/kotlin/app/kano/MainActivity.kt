package app.kano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbars) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                        destinations.chunked(navigationColumns).forEach { rowDestinations ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                rowDestinations.forEach { dest ->
                                    val active = entry?.destination?.route == dest.route
                                    Surface(
                                        onClick = { navigate(dest.route) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp)
                                            .semantics { selected = active; role = Role.Tab },
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                        ) {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = dest.label,
                                                tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = dest.label,
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
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
