package com.example.myapplication.data.firebase

import com.example.myapplication.util.AppLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Repository that encapsulates all Firebase Realtime Database operations
 * for the "hotspots" node.  Each hotspot gets a unique key via push().
 */
class HotspotRepository {

    private val database = FirebaseDatabase.getInstance()
    private val hotspotsRef = database.getReference("hotspots")

    // ── Write ─────────────────────────────────��─────────────────────

    /**
     * Writes a new [Hotspot] to the database.
     * The [Hotspot.userId] is set automatically from the current Firebase user.
     *
     * @param latitude  GPS latitude of the hotspot
     * @param longitude GPS longitude of the hotspot
     * @param report    Free-text description
     * @param onComplete called with (success, errorMessage?)
     */
    fun writeHotspot(
        latitude: Double,
        longitude: Double,
        report: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onComplete(false, "User not authenticated")
            return
        }

        val hotspot = Hotspot(
            latitude = latitude,
            longitude = longitude,
            report = report,
            timestamp = System.currentTimeMillis(),
            userId = uid
        )

        // push() generates a unique key for every new entry
        hotspotsRef.push().setValue(hotspot)
            .addOnSuccessListener {
                AppLogger.i(TAG, "Hotspot written successfully")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                AppLogger.e(TAG, "Failed to write hotspot", e)
                onComplete(false, e.localizedMessage ?: "Write failed")
            }
    }

    // ── Read ────────────────────────────────��───────────────────────

    /**
     * Attaches a realtime listener that emits the full list of hotspots
     * every time the "hotspots" node changes.
     *
     * @param onDataChange called with a list of (key, Hotspot) pairs
     * @param onError      called with an error message on failure
     * @return the [ValueEventListener] so the caller can remove it later
     */
    fun readAllHotspots(
        onDataChange: (List<Pair<String, Hotspot>>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hotspots = mutableListOf<Pair<String, Hotspot>>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val hotspot = child.getValue(Hotspot::class.java) ?: continue
                    hotspots.add(key to hotspot)
                }
                AppLogger.i(TAG, "Read ${hotspots.size} hotspots from database")
                onDataChange(hotspots)
            }

            override fun onCancelled(error: DatabaseError) {
                AppLogger.e(TAG, "Read cancelled: ${error.message}")
                onError(error.message)
            }
        }

        hotspotsRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Removes the previously attached [listener] to stop receiving updates.
     */
    fun removeListener(listener: ValueEventListener) {
        hotspotsRef.removeEventListener(listener)
    }

    // ── Delete ──────────────────────────────────────────────────────

    /**
     * Deletes the hotspot identified by [key].
     *
     * @param key        the push-generated key of the hotspot
     * @param onComplete called with (success, errorMessage?)
     */
    fun deleteHotspot(key: String, onComplete: (Boolean, String?) -> Unit) {
        hotspotsRef.child(key).removeValue()
            .addOnSuccessListener {
                AppLogger.i(TAG, "Hotspot $key deleted")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                AppLogger.e(TAG, "Failed to delete hotspot $key", e)
                onComplete(false, e.localizedMessage ?: "Delete failed")
            }
    }

    companion object {
        private const val TAG = "HotspotRepository"
    }
}
