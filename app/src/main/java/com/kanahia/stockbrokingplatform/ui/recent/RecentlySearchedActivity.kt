package com.kanahia.stockbrokingplatform.ui.recent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kanahia.stockbrokingplatform.R
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.ui.common.RecentlySearchedAdapter
import com.kanahia.stockbrokingplatform.ui.details.DetailsActivity
import com.kanahia.stockbrokingplatform.ui.recent.viewmodel.RecentSearchViewModel
import com.kanahia.stockbrokingplatform.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecentlySearchedActivity : AppCompatActivity() {

    private val viewModel: RecentSearchViewModel by viewModels()
    private lateinit var adapter: RecentlySearchedAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recently_searched)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup back button
        val backButton = findViewById<ImageView>(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }

        // Setup recycler view
        recyclerView = findViewById(R.id.rvRecentStocks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize empty state view
        emptyStateView = findViewById(R.id.tvEmptyState)

        // Initialize adapter
        adapter = RecentlySearchedAdapter { stock ->
            navigateToDetailScreen(stock)
        }

        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.recentStocksState.collect { state ->
                when (state) {
                    is RecentSearchViewModel.UiState.Loading -> {
                        // Show loading state if needed
                    }
                    is RecentSearchViewModel.UiState.Success -> {
                        val recentStocks = state.data
                        if (recentStocks.isEmpty()) {
                            showEmptyState()
                        } else {
                            hideEmptyState()
                            adapter.submitList(recentStocks)
                        }
                    }
                    is RecentSearchViewModel.UiState.Error -> {
                        // Show error state
                        showEmptyState()
                    }
                }
            }
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        recyclerView.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
    }

    private fun navigateToDetailScreen(stock: RecentStock) {
        val intent = Intent(this, DetailsActivity::class.java).apply {
            putExtra(Constants.STOCK, stock.symbol)
        }
        startActivity(intent)
    }
}