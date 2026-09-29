package app.kano.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class CareStatus(val label: String) {
    UNKNOWN("Unknown"), ACTIVE("Active"), LOW("Low"), NEARLY_EMPTY("Nearly empty"),
    EMPTY("Empty"), EXPIRED("Expired"), NOT_USING("Not using"), DISLIKED("Disliked"), REVIEW("Review"),
}

@Entity(tableName = "care_items")
data class CareRecord(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val status: String,
    val updatedAt: Long,
)

@Dao
interface CareDao {
    @Query("SELECT * FROM care_items ORDER BY updatedAt DESC, id")
    fun observe(): Flow<List<CareRecord>>
    @Query("SELECT COUNT(*) FROM care_items")
    suspend fun count(): Int
    @Query("SELECT * FROM care_items WHERE id = :id")
    suspend fun find(id: String): CareRecord?
    @Insert suspend fun insert(record: CareRecord)
    @Update suspend fun update(record: CareRecord): Int
    @Query("DELETE FROM care_items WHERE id = :id") suspend fun delete(id: String): Int
}

/** Only explicit user entries; no seed inventory, inferred quantities or scanner output. */
class CareRepository(private val database: KanoDatabase) {
    val items = database.care().observe()

    suspend fun save(id: String?, name: String, category: String, status: CareStatus) {
        require(name.trim().isNotEmpty() && name.length <= 120 && category.length <= 60)
        database.withTransaction {
            val dao = database.care()
            val record = CareRecord(id ?: UUID.randomUUID().toString(), name.trim(), category.trim(), status.name, System.currentTimeMillis())
            if (id == null) {
                check(dao.count() < MAX_ITEMS) { "Inventory limit reached" }
                dao.insert(record)
            } else {
                check(dao.update(record) == 1) { "Item no longer exists" }
            }
        }
    }

    suspend fun delete(id: String) { database.care().delete(id) }

    companion object { const val MAX_ITEMS = 200 }
}
