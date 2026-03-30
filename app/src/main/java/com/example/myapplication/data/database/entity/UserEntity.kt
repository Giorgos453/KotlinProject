package com.example.myapplication.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-Entity fuer Benutzerprofile.
 * Ergaenzt SharedPreferences – strukturierte User-Daten werden in Room gespeichert.
 * Index auf name fuer schnelle Namenssuchen.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["name"])]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
