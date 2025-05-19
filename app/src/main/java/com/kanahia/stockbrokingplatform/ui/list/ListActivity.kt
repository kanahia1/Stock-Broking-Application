package com.kanahia.stockbrokingplatform.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kanahia.stockbrokingplatform.R
import com.kanahia.stockbrokingplatform.databinding.ActivityListBinding
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import com.kanahia.stockbrokingplatform.ui.common.StockCardAdapter
import com.kanahia.stockbrokingplatform.ui.details.DetailsActivity
import com.kanahia.stockbrokingplatform.ui.list.viewmodel.ListViewModel
import com.kanahia.stockbrokingplatform.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private val viewModel: ListViewModel by viewModels()
    private val navigateToDetailsScreen :  (StockModel) -> Unit = {
        val intent = Intent(this@ListActivity, DetailsActivity::class.java)
        intent.putExtra(Constants.STOCK, it.ticker)
        startActivity(intent)
    }
    private var cardAdapter: StockCardAdapter = StockCardAdapter(navigateToDetailsScreen)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val type = intent.getStringExtra(Constants.TYPE).toString()
        if (type == Constants.GAINERS){
            binding.topBarTitle.text = "Top Gainers"
        }else{
            binding.topBarTitle.text = "Top Losers"
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        setupRecyclerView()
        observeViewModel(type)
    }

    fun setupRecyclerView(){
        binding.rvList.apply {
            layoutManager = GridLayoutManager(this@ListActivity, 2)
            adapter = cardAdapter
        }
    }

    fun observeViewModel(type: String){
        if (type == Constants.GAINERS){
            lifecycleScope.launch {
                viewModel.allTopGainersState.collectLatest { state ->
                    when (state) {
                        is ListViewModel.UiState.Loading -> {
//                        binding.progressGainers.visibility = View.VISIBLE
                        }
                        is ListViewModel.UiState.Success -> {
//                        binding.progressGainers.visibility = View.GONE
//                        var list = mutableListOf<StockModel>()
//                        for (i in 0..3){
//                            list.add(state.data[i])
//                        }
                            cardAdapter.submitList(state.data)
                        }
                        is ListViewModel.UiState.Error -> {
//                        binding.progressGainers.visibility = View.GONE
                            Toast.makeText(this@ListActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }else{
            lifecycleScope.launch {
                viewModel.allTopLosersState.collectLatest { state ->
                    when (state) {
                        is ListViewModel.UiState.Loading -> {
//                        binding.progressLosers.visibility = View.VISIBLE
                        }
                        is ListViewModel.UiState.Success -> {
//                        binding.progressLosers.visibility = View.GONE
                            cardAdapter.submitList(state.data)
                        }
                        is ListViewModel.UiState.Error -> {
//                        binding.progressLosers.visibility = View.GONE
                            Toast.makeText(this@ListActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }

//                binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }



    }
}