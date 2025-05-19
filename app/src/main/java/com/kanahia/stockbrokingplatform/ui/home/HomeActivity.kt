package com.kanahia.stockbrokingplatform.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kanahia.stockbrokingplatform.databinding.ActivityHomeBinding
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import com.kanahia.stockbrokingplatform.ui.common.StockCardAdapter
import com.kanahia.stockbrokingplatform.ui.details.DetailsActivity
import com.kanahia.stockbrokingplatform.ui.common.RecentlySearchedAdapter
import com.kanahia.stockbrokingplatform.ui.home.viewmodel.HomeViewModel
import com.kanahia.stockbrokingplatform.ui.list.ListActivity
import com.kanahia.stockbrokingplatform.ui.recent.RecentlySearchedActivity
import com.kanahia.stockbrokingplatform.ui.search.SearchActivity
import com.kanahia.stockbrokingplatform.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var recentlySearchedAdapter: RecentlySearchedAdapter
    private val viewModel: HomeViewModel by viewModels()

    private val navigateToDetailsScreen: (StockModel) -> Unit = {
        val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
        intent.putExtra(Constants.STOCK, it.ticker)
        startActivity(intent)
    }

    // Updated adapters with chart functionality
    private val gainersAdapter by lazy {
        StockCardAdapter(
            onItemClick = navigateToDetailsScreen,
            loadChartData = { ticker -> viewModel.loadChartData(ticker) },
            showChart = true,
            getChartData = { ticker -> viewModel.chartData.value?.takeIf { it.symbol == ticker } }
        )
    }

    private val losersAdapter by lazy {
        StockCardAdapter(
            onItemClick = navigateToDetailsScreen,
            loadChartData = { ticker -> viewModel.loadChartData(ticker) },
            showChart = true,
            getChartData = { ticker -> viewModel.chartData.value?.takeIf { it.symbol == ticker } }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecentlySearched()
        setupClickListeners()
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {

        binding.rvTopGainers.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = gainersAdapter
        }

        binding.rvTopLosers.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = losersAdapter
        }
    }

    private fun setupRecentlySearched() {
        recentlySearchedAdapter = RecentlySearchedAdapter { recentStock ->
            val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
            intent.putExtra(Constants.STOCK, recentStock.symbol)
            startActivity(intent)
        }

        binding.rvRecentlySearched.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = recentlySearchedAdapter
        }

        // Observe recent stocks from ViewModel
        lifecycleScope.launch {
            viewModel.recentStocksState.collectLatest { recentStocks ->
                if (recentStocks.isEmpty()){
                    binding.lLrecentlySearched.visibility = View.GONE
                }else{
                    binding.lLrecentlySearched.visibility = View.VISIBLE
                    recentlySearchedAdapter.submitList(recentStocks)
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.fourTopGainersState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.UiState.Loading -> {
                        // Show loading if needed
                    }
                    is HomeViewModel.UiState.Success -> {
                        gainersAdapter.submitList(state.data)

                        // Load chart data for first gainer item
                        state.data.firstOrNull()?.let { stock ->
                            viewModel.loadChartData(stock.ticker)
                        }
                    }
                    is HomeViewModel.UiState.Error -> {
                        Toast.makeText(this@HomeActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.fourTopLosersState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.UiState.Loading -> {
                        // Show loading if needed
                    }
                    is HomeViewModel.UiState.Success -> {
                        losersAdapter.submitList(state.data)

                        // Load chart data for first loser item after gainers are processed
                        state.data.firstOrNull()?.let { stock ->
                            viewModel.loadChartData(stock.ticker)
                        }
                    }
                    is HomeViewModel.UiState.Error -> {
                        Toast.makeText(this@HomeActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.chartData.collectLatest { chartData ->
                if (chartData != null) {
                    gainersAdapter.notifyDataSetChanged()
                    losersAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.tvViewAllRecent.setOnClickListener {
            val intent = Intent(this@HomeActivity, RecentlySearchedActivity::class.java)
            startActivity(intent)
        }

        binding.tvViewAllGainers.setOnClickListener {
            val intent = Intent(this@HomeActivity, ListActivity::class.java)
            intent.putExtra(Constants.TYPE, Constants.GAINERS)
            startActivity(intent)
        }

        binding.tvViewAllLosers.setOnClickListener {
            val intent = Intent(this@HomeActivity, ListActivity::class.java)
            intent.putExtra(Constants.TYPE, Constants.LOSERS)
            startActivity(intent)
        }

        binding.searchCardView.setOnClickListener {
            val intent = Intent(this@HomeActivity, SearchActivity::class.java)
            startActivity(intent)
        }
    }
}