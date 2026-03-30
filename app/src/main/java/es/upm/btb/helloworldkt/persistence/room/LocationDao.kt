package es.upm.btb.helloworldkt.persistence.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO fuer GPS-Koordinaten – vollstaendige CRUD-Operationen.
 * Lesende Queries als Flow fuer automatische UI-Aktualisierung.
 * Schreibende Operationen als suspend fun (niemals auf Main Thread).
 */
@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Update
    suspend fun update(location: LocationEntity)

    /** Alle Koordinaten absteigend nach Timestamp – neueste zuerst */
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
