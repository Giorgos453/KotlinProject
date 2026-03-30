package com.example.myapplication.data.database.repository

import com.example.myapplication.data.database.dao.UserDao
import com.example.myapplication.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository fuer Benutzer – kapselt alle DAO-Zugriffe.
 */
class UserRepository(private val dao: UserDao) {

    /** Alle Benutzer als Flow – UI aktualisiert sich automatisch */
    val allUsers: Flow<List<UserEntity>> = dao.getAll()

    suspend fun insert(user: UserEntity) = dao.insert(user)

    suspend fun getById(id: Int): UserEntity? = dao.getById(id)

    suspend fun getByName(name: String): UserEntity? = dao.getByName(name)

    suspend fun delete(user: UserEntity) = dao.delete(user)

    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
