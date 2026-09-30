package app.kano

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import app.kano.ui.*
import kotlinx.coroutines.delay

data class NavDestination(val route: String, val label: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: KanoViewModel = viewModel(factory = KanoViewModel.factory((application as KanoApplication).graph))
            val mode by model.themeMode.collectAsStateWithLifecycle()
            val visual by model.visualTheme.collectAsStateWithLifecycle()
            val glassMode by model.glassMode.collectAsStateWithLifecycle()
            val reduced by model.reducedMotion.collectAsStateWithLifecycle()
            val device by model.device.collectAsStateWithLifecycle()
            val owner = LocalLifecycleOwner.current
            val lifecycle by owner.lifecycle.currentStateFlow.collectAsState()
            DisposableEffect(owner, model) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) model.setForeground(true)
                    if (event == Lifecycle.Event.ON_STOP) model.setForeground(false)
                }
                owner.lifecycle.addObserver(observer)
                if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) model.setForeground(true)
                onDispose { owner.lifecycle.removeObserver(observer); model.setForeground(false) }
            }
            val constrained = (device as? DeviceState.Ready)?.snapshot?.memoryLow != false
            val motion = !reduced && !constrained && lifecycle.isAtLeast(Lifecycle.State.RESUMED) && ValueAnimator.areAnimatorsEnabled()
            val glass = glassMode != KanoGlassMode.OFF
            KanoTheme(mode, visual) {
                val dark = MaterialTheme.colorScheme.background.luminance() < .5f
                SideEffect {
                    val bars = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                    enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                }
                var showSplash by rememberSaveable { mutableStateOf(true) }
                CompositionLocalProvider(
                    LocalGlassEnabled provides glass,
                    LocalMotionEnabled provides motion,
                    LocalBlurEnabled provides (glass && !reduced && !constrained && Build.VERSION.SDK_INT >= 31)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        KanoApp(model)
                        if (showSplash) {
                            KanoSplashScreen(onDismiss = { showSplash = false })
                        }
                    }
                }
            }
        }
    }
}

/**
 * Premium Kano native splash screen featuring the official liquid-glass mascot artwork.
 * Short, non-blocking, and gentle reveal transitioning directly into Home.
 */
@Composable
fun KanoSplashScreen(onDismiss: () -> Unit) {
    val motion = LocalMotionEnabled.current
    val dark = KanoThemeColors.isDark
    val colors = KanoThemeColors

    if (!motion) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var started by remember { mutableStateOf(false) }
    var phase2 by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = true
        delay(320)
        phase2 = true
        delay(400)
        onDismiss()
    }

    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.90f,
        animationSpec = KanoMotionTokens.SpringGentle,
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(KanoMotionTokens.STANDARD, easing = KanoMotionTokens.EmphasizedDecelerate),
        label = "splashAlpha"
    )
    val glintShift by animateFloatAsState(
        targetValue = if (phase2) 1.25f else -0.25f,
        animationSpec = tween(KanoMotionTokens.MAJOR_FAST, easing = KanoMotionTokens.FluidEasing),
        label = "splashGlint"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF141210) else KanoBrandTokens.CreamCanvas)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width * 0.5f, size.height * 0.48f)
            val glowColor = if (dark) KanoBrandTokens.MintDark.copy(alpha = 0.22f) else KanoBrandTokens.MintSoft.copy(alpha = 0.45f)
            val sakuraColor = if (dark) KanoBrandTokens.SakuraPink.copy(alpha = 0.10f) else KanoBrandTokens.SakuraSoft.copy(alpha = 0.35f)
            drawCircle(Brush.radialGradient(listOf(glowColor, Color.Transparent), center = centerOffset, radius = size.width * 0.70f))
            drawCircle(Brush.radialGradient(listOf(sakuraColor, Color.Transparent), center = Offset(centerOffset.x * 1.15f, centerOffset.y * 0.85f), radius = size.width * 0.50f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (dark) 0.22f else 0.55f)),
                modifier = Modifier.size(160.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.kano_logo),
                        contentDescription = "Kano",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))
                    )
                    Canvas(Modifier.matchParentSize()) {
                        drawRect(
                            Brush.linearGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = if (dark) 0.20f else 0.45f), Color.Transparent),
                                start = Offset(size.width * glintShift, 0f),
                                end = Offset(size.width * (glintShift + 0.35f), size.height)
                            )
                        )
                    }
                }
            }

            Text(
                text = "Kano",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun KanoApp(model: KanoViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val snacks = remember { SnackbarHostState() }
    val message by model.message.collectAsStateWithLifecycle()
    val device by model.device.collectAsStateWithLifecycle()
    val motion = LocalMotionEnabled.current
    val dark = KanoThemeColors.isDark
    val backgroundLayer = rememberGraphicsLayer()
    val contentLayer = rememberGraphicsLayer()
    val backdrop = remember(backgroundLayer, contentLayer) { FooterBackdrop(backgroundLayer, contentLayer) }
    val destinations = listOf(
        NavDestination("home", "Home", Icons.Outlined.Home),
        NavDestination("device", "Device", Icons.Outlined.Smartphone),
        NavDestination("media", "Media", Icons.Outlined.PermMedia),
        NavDestination("vault", "Vault", Icons.Outlined.Bookmarks),
        NavDestination("style", "Style", Icons.Outlined.Checkroom),
        NavDestination("personal_care", "Care", Icons.Outlined.SelfImprovement)
    )
    var more by remember { mutableStateOf(false) }
    val navigate: (String) -> Unit = { target ->
        nav.navigate(target) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    LaunchedEffect(message) {
        message?.let {
            snacks.showSnackbar(it)
            model.message.value = null
        }
    }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .matchParentSize()
                .drawWithContent {
                    backgroundLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(backgroundLayer)
                }
                .background(MaterialTheme.colorScheme.background)
        )
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snacks) },
            topBar = {
                if (route != "camera") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        KanoMenuButton(onClick = { more = true })
                    }
                }
            },
            bottomBar = {
                if (route != "camera") {
                    BoxWithConstraints(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.padding(horizontal = if (maxWidth < 360.dp) 12.dp else 24.dp, vertical = 6.dp)) {
                            LiquidFooter(destinations, route, navigate, backdrop, Modifier.widthIn(max = 420.dp).fillMaxWidth())
                        }
                    }
                }
            }
        ) { insets ->
            val footerInset = insets.calculateBottomPadding()
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = insets.calculateTopPadding(), bottom = 0.dp)
                    .onGloballyPositioned { backdrop.contentOrigin = it.positionInRoot() }
                    .drawWithContent {
                        contentLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(contentLayer)
                    }
            ) {
                KanoWaveBackground(
                    Modifier.padding(bottom = 0.dp),
                    waveColor = KanoBrandTokens.sectionWaveColor(route, dark),
                    secondaryColor = if (dark) KanoBrandTokens.MintDark else KanoBrandTokens.MintSoft
                )
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    NavHost(
                        navController = nav,
                        startDestination = "home",
                        modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                        enterTransition = {
                            if (motion) fadeIn(tween(KanoMotionTokens.MAJOR_FAST, easing = KanoMotionTokens.EmphasizedDecelerate)) +
                                    slideInHorizontally(tween(KanoMotionTokens.MAJOR_FAST, easing = KanoMotionTokens.EmphasizedDecelerate)) { (it * 0.04f).toInt() }
                            else EnterTransition.None
                        },
                        exitTransition = {
                            if (motion) fadeOut(tween(KanoMotionTokens.MICRO, easing = KanoMotionTokens.EmphasizedAccelerate))
                            else ExitTransition.None
                        },
                        popEnterTransition = {
                            if (motion) fadeIn(tween(KanoMotionTokens.STANDARD, easing = KanoMotionTokens.EmphasizedDecelerate)) +
                                    slideInHorizontally(tween(KanoMotionTokens.STANDARD, easing = KanoMotionTokens.EmphasizedDecelerate)) { -(it * 0.04f).toInt() }
                            else EnterTransition.None
                        },
                        popExitTransition = {
                            if (motion) fadeOut(tween(KanoMotionTokens.MICRO, easing = KanoMotionTokens.EmphasizedAccelerate))
                            else ExitTransition.None
                        }
                    ) {
                        composable("home") {
                            HomeScreen(
                                { navigate("device") },
                                { navigate("media") },
                                { navigate("style") },
                                { navigate("personal_care") },
                                { navigate("vault") },
                                { nav.navigate("camera") },
                                model,
                                { navigate("shopping") },
                                { navigate("saved_media") },
                                footerInset = footerInset
                            )
                        }
                        composable("device") { DeviceScreen(device, model::refreshDevice, model = model, openMedia = { navigate("media") }, footerInset = footerInset, openStorage = { navigate("storage") }) }
                        composable("storage") { StorageScreen(model, footerInset) }
                        composable("media") { GalleryScreen(model, { nav.navigate("camera") }, { navigate("saved_media") }, footerInset = footerInset) }
                        composable("saved_media") { MediaScreen(model, footerInset = footerInset) }
                        composable("vault") { KnowledgeVaultScreen(model, footerInset = footerInset) }
                        composable("style") { StyleScreen({ nav.navigate("camera") }, { navigate("media") }, footerInset = footerInset) }
                        composable("personal_care") { PersonalCareScreen(model, { nav.navigate("camera") }, footerInset = footerInset) }
                        composable("settings") { SettingsScreen(model, footerInset = footerInset) }
                        composable("appearance") { SettingsScreen(model, "Appearance", footerInset = footerInset) }
                        composable("privacy") { SettingsScreen(model, "Privacy", footerInset = footerInset) }
                        composable("permissions") { SettingsScreen(model, "Permissions", footerInset = footerInset) }
                        composable("providers") { SettingsScreen(model, "AI providers", footerInset = footerInset) }
                        composable("about") { SettingsScreen(model, "About", footerInset = footerInset) }
                        composable("diagnostics") { DeviceScreen(device, model::refreshDevice, model, initialPanel = "Diagnostics", footerInset = footerInset, openStorage = { navigate("storage") }) }
                        composable("shopping") { ShoppingScreen(model, { navigate("personal_care") }, { nav.navigate("camera") }, footerInset = footerInset) }
                        composable("life") { DailyLifeScreen(footerInset = footerInset) }
                        composable("camera") { CameraCaptureScreen({ uri -> model.runLocalVision(uri); nav.popBackStack() }, { nav.popBackStack() }) }
                    }
                }
            }
        }
    }
    if (more) ModalBottomSheet(
        onDismissRequest = { more = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KanoThemeColors.surface.copy(alpha = if (LocalGlassEnabled.current) .95f else 1f)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("kano-menu")
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Kano menu",
                style = MaterialTheme.typography.headlineMedium,
                color = KanoThemeColors.textPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
            KanoCardGroup {
                val menuItems = listOf(
                    "Settings" to "settings",
                    "Storage intelligence" to "storage",
                    "Appearance & themes" to "appearance",
                    "Privacy" to "privacy",
                    "Permissions" to "permissions",
                    "AI providers" to "providers",
                    "Diagnostics" to "diagnostics",
                    "Shopping" to "shopping",
                    "Daily Life" to "life",
                    "About" to "about"
                )
                menuItems.forEachIndexed { index, (label, target) ->
                    KanoGroupItem(
                        title = label,
                        detail = "",
                        onClick = { more = false; navigate(target) },
                        showDivider = index < menuItems.lastIndex
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    VisionReviewDialog(model)
}
