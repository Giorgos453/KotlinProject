package com.example.myapplication.ui.csv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import es.upm.btb.helloworldkt.persistence.room.LocationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView-Adapter fuer GPS-Koordinaten aus der Room-Datenbank.
 * Nutzt ListAdapter + DiffUtil fuer effizientes Rendering bei Datenaenderungen.
 */
class GpsCoordinateAdapter(
    private val onItemClick: (LocationEntity) -> Unit
) : ListAdapter<LocationEntity, GpsCoordinateAdapter.ViewHolder>(
    LocationDiffCallback()
) {

    // Debounce: verhindert Doppelklicks
    private var lastClickTime = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gps_coordinate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position + 1)

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

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun bind(coordinate: LocationEntity, index: Int) {
            tvIndex.text = "#$index"
            tvTimestamp.text = dateFormat.format(Date(coordinate.timestamp))
            tvCoordinates.text = itemView.context.getString(
                R.string.location_text,
                coordinate.latitude,
                coordinate.longitude
            )
            tvAltitude.text = itemView.context.getString(
                R.string.csv_altitude_text,
                coordinate.altitude
            )

            itemView.contentDescription = itemView.context.getString(
                R.string.csv_item_accessibility,
                index,
                dateFormat.format(Date(coordinate.timestamp)),
                coordinate.latitude,
                coordinate.longitude,
                coordinate.altitude
            )
        }
    }

    companion object {
        private const val DEBOUNCE_INTERVAL_MS = 400L
    }
}

/**
 * DiffUtil-Callback – vergleicht Eintraege anhand der Room-ID (eindeutig).
 */
private class LocationDiffCallback : DiffUtil.ItemCallback<LocationEntity>() {
    override fun areItemsTheSame(oldItem: LocationEntity, newItem: LocationEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocationEntity, newItem: LocationEntity): Boolean {
        return oldItem == newItem
    }
}
