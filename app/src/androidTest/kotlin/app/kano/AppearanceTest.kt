package app.kano

import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import app.kano.ui.KanoThemeMode
import app.kano.ui.ThemeManager
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
        } finally { base.deleteSharedPreferences(name) }
    }
}
