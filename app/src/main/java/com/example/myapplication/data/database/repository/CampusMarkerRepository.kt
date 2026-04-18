package com.example.myapplication.data.database.repository

import com.example.myapplication.data.database.dao.CampusMarkerDao
import com.example.myapplication.data.database.entity.CampusMarkerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for campus markers — wraps all DAO access.
 */
class CampusMarkerRepository(private val dao: CampusMarkerDao) {

    /** All campus markers as a Flow — UI updates automatically */
    val allMarkers: Flow<List<CampusMarkerEntity>> = dao.getAll()

    suspend fun insert(marker: CampusMarkerEntity) = dao.insert(marker)

    suspend fun getById(id: Int): CampusMarkerEntity? = dao.getById(id)

    suspend fun delete(marker: CampusMarkerEntity) = dao.delete(marker)

    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
