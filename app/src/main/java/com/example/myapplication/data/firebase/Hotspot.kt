package com.example.myapplication.data.firebase

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Data class representing a user-reported hotspot.
 * All fields have defaults so Firebase can deserialize via the no-arg constructor.
 */
@IgnoreExtraProperties
data class Hotspot(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val report: String = "",
    val timestamp: Long = 0L,
    val userId: String = ""
)
