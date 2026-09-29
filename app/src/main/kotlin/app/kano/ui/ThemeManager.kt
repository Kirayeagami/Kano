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
