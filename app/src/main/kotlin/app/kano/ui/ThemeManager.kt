package app.kano.ui

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KanoThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kano_theme_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(readSavedTheme())
    val themeMode: StateFlow<KanoThemeMode> = _themeMode.asStateFlow()
    private val _glass = MutableStateFlow(prefs.getBoolean("glass", true))
    val glass: StateFlow<Boolean> = _glass.asStateFlow()
    // AUTO is accepted only as a legacy saved value. Normal appearance is glass-enabled.
    private val _glassMode = MutableStateFlow(if (prefs.getString("glass_mode", null) == "OFF" ||
        (!prefs.contains("glass_mode") && !_glass.value)) KanoGlassMode.OFF else KanoGlassMode.ON)
    val glassMode: StateFlow<KanoGlassMode> = _glassMode.asStateFlow()
    private val _visualTheme = MutableStateFlow(runCatching { KanoVisualTheme.valueOf(prefs.getString("visual_theme", "KANO_GLASS")!!) }.getOrDefault(KanoVisualTheme.KANO_GLASS))
    val visualTheme: StateFlow<KanoVisualTheme> = _visualTheme.asStateFlow()
    private val _reducedMotion = MutableStateFlow(prefs.getBoolean("reduced_motion", false))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    fun setGlass(enabled: Boolean) {
        setGlassMode(if (enabled) KanoGlassMode.ON else KanoGlassMode.OFF)
    }
    fun setGlassMode(mode: KanoGlassMode) {
        val normalized = if (mode == KanoGlassMode.OFF) KanoGlassMode.OFF else KanoGlassMode.ON
        prefs.edit().putString("glass_mode", normalized.name).putBoolean("glass", normalized != KanoGlassMode.OFF).apply()
        _glassMode.value = normalized; _glass.value = normalized != KanoGlassMode.OFF
    }
    fun setVisualTheme(theme: KanoVisualTheme) { prefs.edit().putString("visual_theme", theme.name).apply(); _visualTheme.value = theme }
    fun setReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean("reduced_motion", enabled).apply(); _reducedMotion.value = enabled
    }

    fun setThemeMode(mode: KanoThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun readSavedTheme(): KanoThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, KanoThemeMode.SYSTEM.name)
        return try {
            KanoThemeMode.valueOf(name ?: KanoThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            KanoThemeMode.SYSTEM
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
