package app.kano.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

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

    @Query("DELETE FROM media WHERE uri = :uri")
    suspend fun delete(uri: String): Int

    @Query("DELETE FROM media")
    suspend fun clear()
}

@Entity(tableName = "knowledge_entities")
data class KnowledgeRecord(
    @PrimaryKey val id: String,
    val entityType: String,
    val title: String,
    val detail: String,
    val urlOrPayload: String? = null,
    val sourceUri: String,
    val extractionType: String,
    val confidence: String,
    val createdAt: Long,
)

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_entities WHERE id = :id")
    suspend fun find(id: String): KnowledgeRecord?
    @Query("SELECT * FROM knowledge_entities ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<KnowledgeRecord>>

    @Query("SELECT * FROM knowledge_entities WHERE entityType = :type ORDER BY createdAt DESC")
    fun observeByType(type: String): Flow<List<KnowledgeRecord>>

    @Query("SELECT COUNT(*) FROM knowledge_entities")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM knowledge_entities")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: KnowledgeRecord)

    @Query("DELETE FROM knowledge_entities WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("DELETE FROM knowledge_entities")
    suspend fun clear()
}

@Database(entities = [MediaRecord::class, CareRecord::class, KnowledgeRecord::class, StorageEntry::class, StorageScan::class, StorageCheckpoint::class], version = 4, exportSchema = true)
abstract class KanoDatabase : RoomDatabase() {
    abstract fun media(): MediaDao
    abstract fun care(): CareDao
    abstract fun knowledge(): KnowledgeDao
    abstract fun storage(): StorageDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS storage_entries (uri TEXT NOT NULL, identity TEXT NOT NULL, scope TEXT NOT NULL, sourceId INTEGER NOT NULL, name TEXT NOT NULL, mime TEXT, sizeBytes INTEGER, addedAt INTEGER, modifiedAt INTEGER, width INTEGER, height INTEGER, durationMillis INTEGER, location TEXT, category TEXT NOT NULL, classification TEXT NOT NULL, hashState TEXT NOT NULL, sha256 TEXT, scanState TEXT NOT NULL, accessState TEXT NOT NULL, sensitivity TEXT NOT NULL, generation INTEGER, seenRun INTEGER NOT NULL, indexedAt INTEGER NOT NULL, PRIMARY KEY(identity))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_storage_entries_scope ON storage_entries (scope)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_storage_entries_category ON storage_entries (category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_storage_entries_sha256 ON storage_entries (sha256)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_storage_entries_sizeBytes_mime ON storage_entries (sizeBytes, mime)")
                db.execSQL("CREATE TABLE IF NOT EXISTS storage_scan (id INTEGER NOT NULL, status TEXT NOT NULL, phase TEXT NOT NULL, runId INTEGER NOT NULL, scopeSignature TEXT NOT NULL, startedAt INTEGER, finishedAt INTEGER, scanned INTEGER NOT NULL, added INTEGER NOT NULL, updated INTEGER NOT NULL, removed INTEGER NOT NULL, errors INTEGER NOT NULL, hashed INTEGER NOT NULL, hashCandidates INTEGER NOT NULL, detail TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE IF NOT EXISTS storage_checkpoints (scope TEXT NOT NULL, version TEXT, generation INTEGER, cursor INTEGER NOT NULL, targetGeneration INTEGER, fullScan INTEGER NOT NULL, runId INTEGER NOT NULL, completed INTEGER NOT NULL, PRIMARY KEY(scope))")
            }
        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS care_items (id TEXT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, status TEXT NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS knowledge_entities (id TEXT NOT NULL, entityType TEXT NOT NULL, title TEXT NOT NULL, detail TEXT NOT NULL, urlOrPayload TEXT, sourceUri TEXT NOT NULL, extractionType TEXT NOT NULL, confidence TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }
    }
}
