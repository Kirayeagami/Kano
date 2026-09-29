package app.kano

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.kano.ui.LocalGlassEnabled
import app.kano.ui.LocalMotionEnabled
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import app.kano.ui.KnowledgeVaultScreen
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
            val model: KanoViewModel = viewModel(factory = KanoViewModel.factory((application as KanoApplication).graph))
            val themeMode by model.themeMode.collectAsStateWithLifecycle()
            val glass by model.glass.collectAsStateWithLifecycle()
            val reducedMotion by model.reducedMotion.collectAsStateWithLifecycle()
            val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
            KanoTheme(themeMode = themeMode) {
                val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                SideEffect {
                    val bars = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                    enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                }
                CompositionLocalProvider(LocalGlassEnabled provides glass,
                    LocalMotionEnabled provides (!reducedMotion && lifecycleState.isAtLeast(Lifecycle.State.RESUMED) && ValueAnimator.areAnimatorsEnabled())) {
                    KanoApp(model)
                }
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
        NavDestination("vault", "Vault", Icons.Outlined.Lightbulb),
        NavDestination("style", "Style", Icons.Outlined.Checkroom),
        NavDestination("personal_care", "Care", Icons.Outlined.Sanitizer),
        NavDestination("settings", "Privacy", Icons.Outlined.Shield),
    )

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
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        destinations.forEach { dest ->
                            val active = entry?.destination?.route == dest.route
                            Surface(
                                onClick = { navigate(dest.route) },
                                shape = CircleShape,
                                color = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .semantics { selected = active; role = Role.Tab },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    if (active) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = dest.label,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
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
                    openVault = { navigate("vault") },
                )
            }
            composable("device") { DeviceScreen(device, model::refreshDevice) }
            composable("media") { MediaScreen(model) }
            composable("vault") { KnowledgeVaultScreen(model) }
            composable("style") { StyleScreen() }
            composable("personal_care") { PersonalCareScreen(model) }
            composable("settings") { SettingsScreen(model) }
        }
    }
}
