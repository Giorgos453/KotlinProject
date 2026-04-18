package com.example.myapplication.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.database.entity.GpsCoordinateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for GPS coordinates — full CRUD operations.
 * Read queries return Flow for automatic UI updates.
 * Write operations are suspend functions (never on the main thread).
 */
@Dao
interface GpsCoordinateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coordinate: GpsCoordinateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coordinates: List<GpsCoordinateEntity>)

    /** All coordinates ordered by creation time descending — newest first */
    @Query("SELECT * FROM gps_coordinates ORDER BY created_at DESC")
    fun getAll(): Flow<List<GpsCoordinateEntity>>

    @Query("SELECT * FROM gps_coordinates WHERE id = :id")
    suspend fun getById(id: Int): GpsCoordinateEntity?

    @Query("SELECT COUNT(*) FROM gps_coordinates")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(coordinate: GpsCoordinateEntity)

    @Query("DELETE FROM gps_coordinates WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM gps_coordinates")
    suspend fun deleteAll()
}
