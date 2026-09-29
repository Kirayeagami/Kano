package app.kano

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import app.kano.core.AiRouter
import app.kano.core.PrivacyFirewall
import app.kano.data.KanoDatabase
import app.kano.data.CareRepository
import app.kano.data.MediaRepository
import app.kano.platform.DeviceReader
import app.kano.security.KeystoreCredentialStore
import app.kano.ui.ThemeManager

class KanoApplication : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
}

class AppGraph(context: Context) {
    val database = Room.databaseBuilder(context, KanoDatabase::class.java, "kano.db")
        .addMigrations(KanoDatabase.MIGRATION_1_2).build()
    val care = CareRepository(database)
    val media = MediaRepository(context, database.media())
    val device = DeviceReader(context)
    val work: WorkManager = WorkManager.getInstance(context)
    val ai = AiRouter(emptyList(), PrivacyFirewall())
    val credentials by lazy { KeystoreCredentialStore(context) }
    val themeManager = ThemeManager(context)
}
