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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.database.repository.GpsCoordinateRepository
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import com.example.myapplication.ui.csv.CsvUiState
import com.example.myapplication.ui.csv.CsvViewModel
import com.example.myapplication.ui.csv.GpsCoordinateAdapter
import com.example.myapplication.util.AppLogger
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * SecondActivity: Shows GPS coordinates from the Room database as a RecyclerView list.
 * Supports swipe-to-delete and Delete-All via the toolbar.
 */
class SecondActivity : AppCompatActivity() {

    private lateinit var viewModel: CsvViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView
    private lateinit var adapter: GpsCoordinateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        AppLogger.i(TAG, "onCreate")

        val database = AppDatabase.getInstance(applicationContext)
        val repository = GpsCoordinateRepository(database.locationDao())

        viewModel = ViewModelProvider(
            this,
            CsvViewModel.Factory(repository)
        )[CsvViewModel::class.java]

        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupSwipeToDelete()
        observeUiState()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            AppLogger.d(TAG, "Back navigation pressed")
            finish()
        }
        // "Delete All" menu item in the toolbar
        toolbar.inflateMenu(R.menu.menu_gps_log)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete_all -> {
                    showDeleteAllConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupRecyclerView() {
        adapter = GpsCoordinateAdapter { coordinate ->
            AppLogger.d(TAG, "Item clicked: ${coordinate.timestamp}")
            startActivity(ThirdActivity.newIntent(this, coordinate))
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /** Swipe-to-delete: swiping left or right removes the entry */
    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem = adapter.currentList[position]
                viewModel.deleteCoordinate(deletedItem)

                // Snackbar with undo option
                Snackbar.make(recyclerView, R.string.entry_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        viewModel.insertCoordinate(deletedItem)
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    /** Confirmation dialog before deleting all entries */
    private fun showDeleteAllConfirmation() {
        val currentState = viewModel.uiState.value
        if (currentState !is CsvUiState.Success) return

        val count = currentState.coordinates.size
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_all_confirm_title)
            .setMessage(getString(R.string.delete_all_confirm_message, count))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteAllCoordinates()
                Snackbar.make(recyclerView, R.string.all_entries_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.csv_viewer_title_count, state.coordinates.size)
        // only show Delete-All when data is present
        toolbar.menu.findItem(R.id.action_delete_all)?.isVisible = true
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        // hide Delete-All when list is empty
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.menu.findItem(R.id.action_delete_all)?.isVisible = false
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = getString(R.string.error_reading_db, message)
    }

    companion object {
        private const val TAG = "SecondActivity"
    }
}
