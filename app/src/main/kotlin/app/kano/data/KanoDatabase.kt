package app.kano.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "media")
data class MediaRecord(
    @PrimaryKey val uri: String,
    val name: String = "Selected document",
    val mime: String? = null,
    val sizeBytes: Long? = null,
    val sourceModifiedAt: Long? = null,
    val state: String = "QUEUED",
    val indexedAt: Long? = null,
    val sha256: String? = null,
    val errorCode: String? = null,
)

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(row: MediaRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(row: MediaRecord)

    @Query("SELECT * FROM media ORDER BY uri")
    suspend fun all(): List<MediaRecord>

    @Query("""SELECT * FROM media WHERE name LIKE :query ESCAPE '\' ORDER BY name, uri LIMIT :limit OFFSET :offset""")
    fun observe(query: String, limit: Int, offset: Int): Flow<List<MediaRecord>>

    @Query("SELECT COUNT(*) FROM media")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media")
    suspend fun count(): Int

    @Query("DELETE FROM media")
    suspend fun clear()
}

@Database(entities = [MediaRecord::class, CareRecord::class], version = 2, exportSchema = true)
abstract class KanoDatabase : RoomDatabase() {
    abstract fun media(): MediaDao
    abstract fun care(): CareDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS care_items (id TEXT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, status TEXT NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }
    }
}
