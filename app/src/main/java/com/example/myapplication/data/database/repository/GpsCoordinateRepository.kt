package com.example.myapplication.data.database.repository

import es.upm.btb.helloworldkt.persistence.room.LocationDao
import es.upm.btb.helloworldkt.persistence.room.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository fuer GPS-Koordinaten – kapselt alle DAO-Zugriffe.
 * ViewModel greift niemals direkt auf den DAO zu, nur ueber das Repository.
 */
class GpsCoordinateRepository(private val dao: LocationDao) {

    /** Alle GPS-Koordinaten als Flow – UI aktualisiert sich automatisch */
    val allCoordinates: Flow<List<LocationEntity>> = dao.getAll()

    suspend fun insert(coordinate: LocationEntity) = dao.insert(coordinate)

    suspend fun update(coordinate: LocationEntity) = dao.update(coordinate)

    suspend fun getById(id: Int): LocationEntity? = dao.getById(id)

    suspend fun getCount(): Int = dao.getCount()

    suspend fun delete(coordinate: LocationEntity) = dao.delete(coordinate)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
