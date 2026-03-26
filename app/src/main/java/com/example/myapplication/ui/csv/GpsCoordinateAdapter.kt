package com.example.myapplication.ui.csv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.csv.GpsCoordinate

/**
 * RecyclerView-Adapter für GPS-Koordinaten.
 * Nutzt ListAdapter + DiffUtil für effizientes Rendering bei Datenänderungen.
 *
 * Click-Handling: Der Adapter ist "dumm" – er leitet Klicks per Lambda
 * nach außen weiter. Die Activity entscheidet, was beim Klick passiert.
 */
class GpsCoordinateAdapter(
    private val onItemClick: (GpsCoordinate) -> Unit
) : ListAdapter<GpsCoordinate, GpsCoordinateAdapter.ViewHolder>(
    GpsCoordinateDiffCallback()
) {

    // Debounce: verhindert Doppelklicks (schnelle Mehrfachklicks)
    private var lastClickTime = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gps_coordinate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position + 1) // +1 für 1-basierte Nummerierung

        // Click-Listener auf dem gesamten Item
        holder.itemView.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > DEBOUNCE_INTERVAL_MS) {
                lastClickTime = now
                onItemClick(item)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIndex: TextView = itemView.findViewById(R.id.tvIndex)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvCoordinates: TextView = itemView.findViewById(R.id.tvCoordinates)
        private val tvAltitude: TextView = itemView.findViewById(R.id.tvAltitude)

        fun bind(coordinate: GpsCoordinate, index: Int) {
            tvIndex.text = "#$index"
            tvTimestamp.text = coordinate.timestamp
            tvCoordinates.text = itemView.context.getString(
                R.string.location_text,
                coordinate.latitude,
                coordinate.longitude
            )
            tvAltitude.text = itemView.context.getString(
                R.string.csv_altitude_text,
                coordinate.altitude
            )

            // Accessibility: gesamten Eintrag als zusammenhängend beschreiben
            itemView.contentDescription = itemView.context.getString(
                R.string.csv_item_accessibility,
                index,
                coordinate.timestamp,
                coordinate.latitude,
                coordinate.longitude,
                coordinate.altitude
            )
        }
    }

    companion object {
        /** Minimaler Abstand zwischen zwei Klicks in Millisekunden */
        private const val DEBOUNCE_INTERVAL_MS = 400L
    }
}

/**
 * DiffUtil-Callback für effiziente Listenaktualisierungen.
 * Vergleicht Einträge anhand des Zeitstempels (eindeutig pro GPS-Messung).
 */
private class GpsCoordinateDiffCallback : DiffUtil.ItemCallback<GpsCoordinate>() {
    override fun areItemsTheSame(oldItem: GpsCoordinate, newItem: GpsCoordinate): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: GpsCoordinate, newItem: GpsCoordinate): Boolean {
        return oldItem == newItem
    }
}
