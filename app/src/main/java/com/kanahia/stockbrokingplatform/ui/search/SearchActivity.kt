package com.kanahia.stockbrokingplatform.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kanahia.stockbrokingplatform.databinding.ActivitySearchBinding
import com.kanahia.stockbrokingplatform.ui.details.DetailsActivity
import com.kanahia.stockbrokingplatform.ui.search.adapter.SearchResultAdapter
import com.kanahia.stockbrokingplatform.ui.search.viewmodel.SearchViewModel
import com.kanahia.stockbrokingplatform.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: SearchResultAdapter
    private var searchJob: Job? = null
    private val typeList = listOf("All", "Equity", "Mutual Fund", "ETF")
    private var selectedType = "All"
    private var lastSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchAdapter()
        setupSearchInput()
        observeViewModel()
        setupClickListeners()

        // Initial chip text
        binding.chipType.text = selectedType
    }

    private fun setupSearchAdapter() {
        searchAdapter = SearchResultAdapter { result ->
            Log.e("PRINT", result.toString())
            val intent = Intent(this@SearchActivity, DetailsActivity::class.java)
            intent.putExtra(Constants.STOCK, result.symbol)
            startActivity(intent)
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { query ->
                    lastSearchQuery = query
                    if (query.isNotEmpty()) {
                        searchJob?.cancel()
                        searchJob = lifecycleScope.launch {
                            // Debounce for better UX and to avoid too many API calls
                            delay(300)
                            viewModel.searchStocks(query)
                        }
                    } else {
                        binding.progressSearch.visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set focus to search EditText when activity opens
        binding.etSearch.requestFocus()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.searchResultsState.collectLatest { state ->
                when (state) {
                    is SearchViewModel.UiState.Loading -> {
                        if (binding.etSearch.text.isNotEmpty())
                            binding.progressSearch.visibility = View.VISIBLE
                    }
                    is SearchViewModel.UiState.Success -> {
                        binding.progressSearch.visibility = View.GONE

                        // Filter results by selected type
                        val filteredResults = if (selectedType == "All") {
                            state.data
                        } else {
                            state.data.filter { it.type == selectedType }
                        }

                        searchAdapter.submitList(filteredResults)
                    }
                    is SearchViewModel.UiState.Error -> {
                        binding.progressSearch.visibility = View.GONE
                        Toast.makeText(this@SearchActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.chipType.setOnClickListener {
            showTypeSelectionDialog()
        }
    }

    private fun showTypeSelectionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Select Type")
            .setSingleChoiceItems(typeList.toTypedArray(), typeList.indexOf(selectedType)) { dialog, which ->
                selectedType = typeList[which]
                binding.chipType.text = selectedType

                // Re-apply search with new filter if there's an existing query
                if (lastSearchQuery.isNotEmpty()) {
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        viewModel.searchStocks(lastSearchQuery)
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}