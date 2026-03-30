package com.example.myapplication.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.database.entity.GpsCoordinateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO fuer GPS-Koordinaten – vollstaendige CRUD-Operationen.
 * Lesende Queries als Flow fuer automatische UI-Aktualisierung.
 * Schreibende Operationen als suspend fun (niemals auf Main Thread).
 */
@Dao
interface GpsCoordinateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coordinate: GpsCoordinateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coordinates: List<GpsCoordinateEntity>)

    /** Alle Koordinaten absteigend nach Erstellungszeitpunkt – neueste zuerst */
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
