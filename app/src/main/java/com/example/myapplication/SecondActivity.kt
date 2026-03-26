package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.csv.CsvUiState
import com.example.myapplication.ui.csv.CsvViewModel
import com.example.myapplication.ui.csv.GpsCoordinateAdapter
import com.example.myapplication.util.AppLogger
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

/**
 * SecondActivity: Zeigt GPS-Koordinaten aus der CSV-Datei als RecyclerView-Liste an.
 *
 * Refactored von einer einfachen TextView-Anzeige zu MVVM-Architektur:
 * - CsvFileRepository: Datei-Lesezugriff (auf IO-Thread)
 * - CsvViewModel: Hält UI-State (überlebt Rotation)
 * - GpsCoordinateAdapter: RecyclerView-Adapter mit DiffUtil
 *
 * Die ursprüngliche Funktion (CSV-Daten anzeigen) bleibt erhalten,
 * aber mit strukturierter Liste statt Fließtext.
 */
class SecondActivity : AppCompatActivity() {

    private lateinit var viewModel: CsvViewModel

    // UI-Referenzen
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView
    private lateinit var adapter: GpsCoordinateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        AppLogger.i(TAG, "onCreate")

        // ViewModel über Factory erstellen (Repository-Dependency)
        viewModel = ViewModelProvider(
            this,
            CsvViewModel.Factory(applicationContext)
        )[CsvViewModel::class.java]

        setupToolbar()
        setupViews()
        setupRecyclerView()
        observeUiState()
    }

    /** Toolbar mit Zurück-Navigation konfigurieren */
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            AppLogger.d(TAG, "Back navigation pressed")
            finish()
        }
    }

    /** UI-Elemente referenzieren – keine Logik hier */
    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvError = findViewById(R.id.tvError)
    }

    /** RecyclerView mit Adapter und LayoutManager initialisieren */
    private fun setupRecyclerView() {
        adapter = GpsCoordinateAdapter { coordinate ->
            // Klick-Handling: Detail-Screen über Factory-Methode öffnen
            AppLogger.d(TAG, "Item clicked: ${coordinate.timestamp}")
            startActivity(ThirdActivity.newIntent(this, coordinate))
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Beobachtet den UI-State des ViewModels und aktualisiert die Anzeige.
     * Nutzt repeatOnLifecycle, damit die Collection nur im STARTED-State läuft.
     */
    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CsvUiState.Loading -> showLoading()
                        is CsvUiState.Success -> showData(state)
                        is CsvUiState.Empty -> showEmpty()
                        is CsvUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    // --- UI-State-Methoden: Jeweils nur einen Zustand sichtbar machen ---

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun showData(state: CsvUiState.Success) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.GONE
        adapter.submitList(state.coordinates)

        // Toolbar-Titel mit Anzahl der Einträge aktualisieren
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.csv_viewer_title_count, state.coordinates.size)
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = getString(R.string.error_reading_csv, message)
    }

    companion object {
        private const val TAG = "SecondActivity"
    }
}
