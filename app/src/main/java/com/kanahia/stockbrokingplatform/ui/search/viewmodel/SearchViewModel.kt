package com.kanahia.stockbrokingplatform.ui.search.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.data.model.StockSearchResult
import com.kanahia.stockbrokingplatform.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _searchResultsState = MutableStateFlow<UiState<List<StockSearchResult>>>(UiState.Loading)
    val searchResultsState = _searchResultsState.asStateFlow()

    fun searchStocks(query: String) {
        viewModelScope.launch {
            _searchResultsState.value = UiState.Loading
            try {
                val response = repository.searchStocks(query)
                _searchResultsState.value = UiState.Success(response.bestMatches)
            } catch (e: Exception) {
                _searchResultsState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }
}