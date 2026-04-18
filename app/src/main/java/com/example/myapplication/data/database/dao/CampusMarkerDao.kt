package com.example.myapplication.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.database.entity.CampusMarkerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for campus markers — CRUD operations on tour stops.
 */
@Dao
interface CampusMarkerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(marker: CampusMarkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(markers: List<CampusMarkerEntity>)

    @Query("SELECT * FROM campus_markers ORDER BY id ASC")
    fun getAll(): Flow<List<CampusMarkerEntity>>

    @Query("SELECT * FROM campus_markers WHERE id = :id")
    suspend fun getById(id: Int): CampusMarkerEntity?

    @Query("SELECT COUNT(*) FROM campus_markers")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(marker: CampusMarkerEntity)

    @Query("DELETE FROM campus_markers WHERE id = :id")
    suspend fun deleteById(id: Int)
}
