package es.upm.btb.helloworldkt.persistence.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.database.dao.CampusMarkerDao
import com.example.myapplication.data.database.dao.UserDao
import com.example.myapplication.data.database.dao.WeatherCacheDao
import com.example.myapplication.data.database.entity.CampusMarkerEntity
import com.example.myapplication.data.database.entity.UserEntity
import com.example.myapplication.data.database.entity.WeatherCacheEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Zentrale Room-Datenbank der App – Singleton-Pattern.
 * Datenbankname: "coordinates"
 *
 * TROUBLESHOOTING:
 * Bei Schema-Aenderungen (z.B. neue Spalte, Entity hinzugefuegt/entfernt):
 * - Entweder: DB-Version erhoehen (version = 3, 4, ...) + Migration schreiben
 * - Oder: App vom Geraet deinstallieren (loescht die DB komplett)
 * - Sonst: IllegalStateException "Room cannot verify the data integrity"
 *
 * DEBUGGING:
 * - DB im Android Studio einsehen: View -> Tool Windows -> App Inspection -> Database Inspector
 * - Alternativ im Device Explorer: /data/data/es.upm.btb.helloworldkt/databases/
 * - Live Updates im Database Inspector aktivieren fuer Echtzeit-Ansicht
 */
@Database(
    entities = [
        LocationEntity::class,
        CampusMarkerEntity::class,
        UserEntity::class,
        WeatherCacheEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun campusMarkerDao(): CampusMarkerDao
    abstract fun userDao(): UserDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton-Zugriff – erstellt die Datenbank beim ersten Aufruf.
         * Beim ersten Start werden die Standard-Campus-Marker eingefuegt (Callback).
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "coordinates"
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(SeedDatabaseCallback())
                .build()

        /**
         * Callback zum Befuellen der Datenbank mit Initialdaten.
         * Wird nur bei onCreate aufgerufen (= erste Installation).
         */
        private class SeedDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.campusMarkerDao().insertAll(defaultCampusMarkers())
                    }
                }
            }
        }

        /** Standard-Campus-Marker (vorher statisch in CampusTourData) */
        private fun defaultCampusMarkers(): List<CampusMarkerEntity> = listOf(
            CampusMarkerEntity(
                id = 1,
                title = "ETSISI",
                description = "Escuela Técnica Superior de Ingeniería de Sistemas Informáticos.",
                latitude = 40.38967,
                longitude = -3.62872
            ),
            CampusMarkerEntity(
                id = 2,
                title = "Futsal Outdoor Courts",
                description = "Outdoor futsal and sports courts on Campus Sur.",
                latitude = 40.38870,
                longitude = -3.62835
            ),
            CampusMarkerEntity(
                id = 3,
                title = "ETSIST",
                description = "Escuela Técnica Superior de Ingeniería y Sistemas de Telecomunicación.",
                latitude = 40.38950,
                longitude = -3.62680
            )
        )
    }
}
