package es.upm.btb.helloworldkt.persistence.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for GPS coordinates.
 * Table: "coordinates"
 * Columns: id (INTEGER PK), latitude (REAL), longitude (REAL), altitude (REAL), timestamp (INTEGER)
 */
@Entity(tableName = "coordinates")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "altitude") val altitude: Double,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)
