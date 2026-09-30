package app.kano

import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import app.kano.ui.KanoThemeMode
import app.kano.ui.ThemeManager
import app.kano.ui.KanoVisualTheme
import app.kano.ui.KanoGlassMode
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class AppearanceTest {
    @Test fun choicesPersistAndInvalidThemeFallsBackToSystem() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val name = "appearance-test-" + UUID.randomUUID()
        val context = object : ContextWrapper(base) {
            override fun getSharedPreferences(ignored: String, mode: Int) = base.getSharedPreferences(name, mode)
        }
        try {
            ThemeManager(context).apply { setThemeMode(KanoThemeMode.DARK); setGlass(false); setReducedMotion(true) }
            ThemeManager(context).apply { assertEquals(KanoThemeMode.DARK, themeMode.value); assertFalse(glass.value); assertTrue(reducedMotion.value) }
            context.getSharedPreferences("", 0).edit().putString("theme_mode", "invalid").commit()
            assertEquals(KanoThemeMode.SYSTEM, ThemeManager(context).themeMode.value)
            KanoVisualTheme.entries.forEach { theme ->
                ThemeManager(context).setVisualTheme(theme)
                assertEquals(theme, ThemeManager(context).visualTheme.value)
            }
            KanoGlassMode.entries.forEach { mode ->
                ThemeManager(context).setGlassMode(mode)
                assertEquals(if (mode == KanoGlassMode.OFF) KanoGlassMode.OFF else KanoGlassMode.ON, ThemeManager(context).glassMode.value)
            }
            context.getSharedPreferences("", 0).edit().remove("glass_mode").putBoolean("glass", false).commit()
            assertEquals(KanoGlassMode.OFF, ThemeManager(context).glassMode.value)
            context.getSharedPreferences("", 0).edit().putString("visual_theme", "invalid").putString("glass_mode", "invalid").commit()
            assertEquals(KanoVisualTheme.KANO_GLASS, ThemeManager(context).visualTheme.value)
            assertEquals(KanoGlassMode.ON, ThemeManager(context).glassMode.value)
        } finally { base.deleteSharedPreferences(name) }
    }
}
