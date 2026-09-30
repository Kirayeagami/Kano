package app.kano.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** Metadata only. Unknown sizes/times remain nullable; this index never owns the source. */
@Entity(tableName = "storage_entries", indices = [Index("scope"), Index("category"), Index("sha256"), Index(value = ["sizeBytes", "mime"])])
data class StorageEntry(
    val uri: String,
    @PrimaryKey val identity: String,
    val scope: String,
    val sourceId: Long = 0,
    val name: String,
    val mime: String? = null,
    val sizeBytes: Long? = null,
    val addedAt: Long? = null,
    val modifiedAt: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null,
    val location: String? = null,
    val category: String = "UNKNOWN",
    val classification: String = "UNKNOWN",
    val hashState: String = "NOT_NEEDED",
    val sha256: String? = null,
    val scanState: String = "INDEXED",
    val accessState: String = "AUTHORIZED",
    val sensitivity: String = "UNKNOWN",
    val generation: Long? = null,
    val seenRun: Long = 0,
    val indexedAt: Long = 0,
)

@Entity(tableName = "storage_scan")
data class StorageScan(
    @PrimaryKey val id: Int = 1,
    val status: String = "FIRST_RUN",
    val phase: String = "METADATA",
    val runId: Long = 0,
    val scopeSignature: String = "",
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val scanned: Long = 0,
    val added: Long = 0,
    val updated: Long = 0,
    val removed: Long = 0,
    val errors: Long = 0,
    val hashed: Long = 0,
    val hashCandidates: Long = 0,
    val detail: String = "No scan has run.",
)

@Entity(tableName = "storage_checkpoints")
data class StorageCheckpoint(
    @PrimaryKey val scope: String,
    val version: String? = null,
    val generation: Long? = null,
    val cursor: Long = 0,
    val targetGeneration: Long? = null,
    val fullScan: Boolean = true,
    val runId: Long = 0,
    val completed: Boolean = false,
)
data class StorageCategoryTotal(val category: String, val count: Long, val bytes: Long?, val unknownSizes: Long)
data class StorageDuplicateTotal(val sha256: String, val sizeBytes: Long, val copies: Long, val localIdentityVerified: Boolean = false)
data class StorageCandidate(val sizeBytes: Long, val mime: String?)
data class StorageShapeTotal(val width: Int?, val height: Int?, val durationMillis: Long?, val copies: Long)
data class StorageTotals(val count: Long, val bytes: Long?, val unknownSizes: Long, val mediaBytes: Long? = null, val fileBytes: Long? = null)

@Dao
interface StorageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveEntries(entries: List<StorageEntry>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveScan(scan: StorageScan)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveCheckpoint(checkpoint: StorageCheckpoint)
    @Query("SELECT * FROM storage_scan WHERE id=1") suspend fun scan(): StorageScan?
    @Query("SELECT * FROM storage_scan WHERE id=1") fun observeScan(): Flow<StorageScan?>
    @Query("SELECT * FROM storage_checkpoints WHERE scope=:scope") suspend fun checkpoint(scope: String): StorageCheckpoint?
    @Query("SELECT * FROM storage_entries WHERE uri=:uri") suspend fun find(uri: String): StorageEntry?
    @Query("SELECT * FROM storage_entries WHERE identity=:identity") suspend fun findIdentity(identity: String): StorageEntry?
    @Query("UPDATE storage_scan SET status=:status, detail=:detail WHERE id=1") suspend fun setStatus(status: String, detail: String)
    @Query("SELECT COUNT(*) AS count, SUM(sizeBytes) AS bytes, SUM(CASE WHEN sizeBytes IS NULL THEN 1 ELSE 0 END) AS unknownSizes, SUM(CASE WHEN scope LIKE 'ms:%' THEN sizeBytes ELSE NULL END) AS mediaBytes, SUM(CASE WHEN scope LIKE 'saf:%' THEN sizeBytes ELSE NULL END) AS fileBytes FROM storage_entries WHERE accessState='AUTHORIZED'") fun totals(): Flow<StorageTotals>
    @Query("SELECT category, COUNT(*) AS count, SUM(sizeBytes) AS bytes, SUM(CASE WHEN sizeBytes IS NULL THEN 1 ELSE 0 END) AS unknownSizes FROM storage_entries WHERE accessState='AUTHORIZED' GROUP BY category ORDER BY SUM(sizeBytes) DESC") fun categories(): Flow<List<StorageCategoryTotal>>
    @Query("SELECT sha256, sizeBytes, COUNT(DISTINCT identity) AS copies, MIN(CASE WHEN identity LIKE 'media:%' THEN 1 ELSE 0 END) AS localIdentityVerified FROM storage_entries WHERE accessState='AUTHORIZED' AND scanState='INDEXED' AND hashState='VERIFIED' AND sha256 IS NOT NULL AND sizeBytes>0 GROUP BY sha256, sizeBytes HAVING COUNT(DISTINCT identity)>1 ORDER BY sizeBytes*(COUNT(DISTINCT identity)-1) DESC LIMIT 40") fun duplicates(): Flow<List<StorageDuplicateTotal>>
    @Query("SELECT * FROM storage_entries WHERE accessState='AUTHORIZED' AND (:filter='ALL' OR (:filter='LARGE' AND sizeBytes>=:threshold) OR (:filter='DOWNLOADS' AND (location LIKE '%Download%' OR scope LIKE 'saf:%')) OR category=:filter) ORDER BY sizeBytes DESC, uri LIMIT :limit OFFSET :offset") fun page(filter: String, threshold: Long, limit: Int, offset: Int): Flow<List<StorageEntry>>
    @Query("SELECT * FROM storage_entries WHERE accessState='AUTHORIZED' AND (:filter='ALL' OR (:filter='LARGE' AND sizeBytes>=:threshold) OR (:filter='DOWNLOADS' AND (location LIKE '%Download%' OR scope LIKE 'saf:%')) OR category=:filter) ORDER BY CASE WHEN :sort = 'SIZE_DESC' THEN sizeBytes END DESC, CASE WHEN :sort = 'SIZE_ASC' THEN sizeBytes END ASC, CASE WHEN :sort = 'DATE_DESC' THEN modifiedAt END DESC, CASE WHEN :sort = 'DATE_ASC' THEN modifiedAt END ASC, CASE WHEN :sort = 'NAME_ASC' THEN LOWER(name) END ASC, sizeBytes DESC, uri LIMIT :limit OFFSET :offset") fun pageSorted(filter: String, threshold: Long, sort: String, limit: Int, offset: Int): Flow<List<StorageEntry>>
    @Query("SELECT * FROM storage_entries WHERE accessState='AUTHORIZED' AND sha256=:hash AND hashState='VERIFIED' ORDER BY modifiedAt DESC, uri LIMIT 100") suspend fun duplicateMembers(hash: String): List<StorageEntry>
    @Query("SELECT sizeBytes, mime FROM storage_entries WHERE accessState='AUTHORIZED' AND scanState='INDEXED' AND sizeBytes>0 AND (sizeBytes>:afterSize OR (sizeBytes=:afterSize AND COALESCE(mime,'')>:afterMime)) GROUP BY sizeBytes, mime HAVING COUNT(DISTINCT identity)>1 ORDER BY sizeBytes, COALESCE(mime,'') LIMIT :limit") suspend fun candidates(limit: Int, afterSize: Long, afterMime: String): List<StorageCandidate>
    @Query("SELECT width,height,durationMillis,COUNT(*) AS copies FROM storage_entries WHERE accessState='AUTHORIZED' AND scanState='INDEXED' AND sizeBytes=:size AND (mime=:mime OR (mime IS NULL AND :mime IS NULL)) GROUP BY width,height,durationMillis") suspend fun shapes(size: Long, mime: String?): List<StorageShapeTotal>
    @Query("SELECT * FROM storage_entries WHERE accessState='AUTHORIZED' AND scanState='INDEXED' AND sizeBytes=:size AND (mime=:mime OR (mime IS NULL AND :mime IS NULL)) AND (:broad OR (width=:width AND height=:height AND (:duration IS NULL OR durationMillis=:duration))) AND identity>:afterIdentity ORDER BY identity LIMIT :limit") suspend fun candidatePage(size: Long, mime: String?, broad: Boolean, width: Int?, height: Int?, duration: Long?, limit: Int, afterIdentity: String): List<StorageEntry>
    @Query("UPDATE storage_entries SET classification=CASE WHEN hashState='VERIFIED' AND sizeBytes>0 AND (sha256,sizeBytes) IN (SELECT sha256,sizeBytes FROM storage_entries WHERE accessState='AUTHORIZED' AND scanState='INDEXED' AND hashState='VERIFIED' AND sha256 IS NOT NULL AND sizeBytes>0 GROUP BY sha256,sizeBytes HAVING COUNT(DISTINCT identity)>1) THEN 'DUPLICATE' ELSE 'UNKNOWN' END WHERE accessState='AUTHORIZED'") suspend fun classifyDuplicates()
    @Query("SELECT * FROM storage_entries WHERE scope=:scope ORDER BY sourceId LIMIT :limit OFFSET :offset") suspend fun scopePage(scope: String, limit: Int, offset: Int): List<StorageEntry>
    @Query("UPDATE storage_entries SET accessState='REMOVED', sha256=NULL, hashState='STALE' WHERE scope=:scope AND seenRun!=:run AND accessState='AUTHORIZED'") suspend fun markUnseen(scope: String, run: Long): Int
    @Query("UPDATE storage_entries SET seenRun=:run WHERE scope=:scope AND sourceId=:sourceId") suspend fun markSeen(scope: String, sourceId: Long, run: Long)
    @Query("UPDATE storage_entries SET accessState='REVOKED', sha256=NULL, hashState='STALE' WHERE scope NOT IN (:scopes) AND accessState='AUTHORIZED'") suspend fun revokeOtherScopes(scopes: List<String>)
    @Query("UPDATE storage_entries SET accessState='REVOKED', sha256=NULL, hashState='STALE' WHERE scope=:scope") suspend fun revokeScope(scope: String)
    @Query("UPDATE storage_entries SET accessState='REVOKED', sha256=NULL, hashState='STALE'") suspend fun revokeAll()
    @Query("UPDATE storage_checkpoints SET completed=0, cursor=0, generation=NULL") suspend fun invalidateCheckpoints()
    @Query("UPDATE storage_entries SET seenRun=0 WHERE scope=:scope") suspend fun resetSeen(scope: String)
    @Query("UPDATE storage_entries SET scanState='STALE', hashState='STALE', sha256=NULL WHERE scope=:scope") suspend fun staleScope(scope: String)
    @Query("SELECT COUNT(*) FROM storage_entries WHERE accessState='AUTHORIZED'") suspend fun count(): Long
}

