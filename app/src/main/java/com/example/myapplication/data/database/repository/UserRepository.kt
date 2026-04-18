package com.example.myapplication.data.database.repository

import com.example.myapplication.data.database.dao.UserDao
import com.example.myapplication.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for users — wraps all DAO access.
 */
class UserRepository(private val dao: UserDao) {

    /** All users as a Flow — UI updates automatically */
    val allUsers: Flow<List<UserEntity>> = dao.getAll()

    suspend fun insert(user: UserEntity) = dao.insert(user)

    suspend fun getById(id: Int): UserEntity? = dao.getById(id)

    suspend fun getByName(name: String): UserEntity? = dao.getByName(name)

    suspend fun delete(user: UserEntity) = dao.delete(user)

    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
