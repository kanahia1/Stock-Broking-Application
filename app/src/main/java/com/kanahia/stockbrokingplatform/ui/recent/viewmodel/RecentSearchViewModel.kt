package com.kanahia.stockbrokingplatform.ui.recent.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentSearchViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _recentStocksState = MutableStateFlow<UiState<List<RecentStock>>>(UiState.Loading)
    val recentStocksState: StateFlow<UiState<List<RecentStock>>> = _recentStocksState

    init {
        loadRecentStocks()
    }

    private fun loadRecentStocks() {
        viewModelScope.launch {
            stockRepository.getRecentStocks()
                .catch { e ->
                    _recentStocksState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { recentStocks ->
                    _recentStocksState.value = UiState.Success(recentStocks)
                }
        }
    }

    fun saveRecentStock(stock: RecentStock) {
        viewModelScope.launch {
            try {
                stockRepository.saveRecentStock(stock)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }
}