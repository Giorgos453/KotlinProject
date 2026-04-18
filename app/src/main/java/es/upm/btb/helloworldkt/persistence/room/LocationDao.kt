package es.upm.btb.helloworldkt.persistence.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for GPS coordinates — full CRUD operations.
 * Read queries return Flow for automatic UI updates.
 * Write operations are suspend functions (never on the main thread).
 */
@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Update
    suspend fun update(location: LocationEntity)

    /** All coordinates ordered by timestamp descending — newest first */
    @Query("SELECT * FROM coordinates ORDER BY timestamp DESC")
    fun getAll(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM coordinates WHERE id = :id")
    suspend fun getById(id: Int): LocationEntity?

    @Query("SELECT COUNT(*) FROM coordinates")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("DELETE FROM coordinates WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM coordinates")
    suspend fun deleteAll()
}
