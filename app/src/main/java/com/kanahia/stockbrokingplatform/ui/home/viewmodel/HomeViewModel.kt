package com.kanahia.stockbrokingplatform.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.domain.model.StockChartData
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import com.kanahia.stockbrokingplatform.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _fourTopGainersState = MutableStateFlow<UiState<List<StockModel>>>(UiState.Loading)
    val fourTopGainersState: StateFlow<UiState<List<StockModel>>> = _fourTopGainersState

    private val _fourTopLosersState = MutableStateFlow<UiState<List<StockModel>>>(UiState.Loading)
    val fourTopLosersState: StateFlow<UiState<List<StockModel>>> = _fourTopLosersState

    private val _recentStocksState = MutableStateFlow<List<RecentStock>>(emptyList())
    val recentStocksState = _recentStocksState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _chartData = MutableStateFlow<StockChartData?>(null)
    val chartData: StateFlow<StockChartData?> = _chartData

    // Cache of chart data by ticker symbol
    private val chartDataCache = mutableMapOf<String, StockChartData>()

    init {
        loadStocks()
        loadRecentStocks()
    }

    private fun loadRecentStocks() {
        viewModelScope.launch {
            stockRepository.getThreeRecentStocks().collectLatest { recentStocks ->
                _recentStocksState.value = recentStocks
            }
        }
    }

    fun loadStocks() {
        viewModelScope.launch {
            try {
                stockRepository.refreshStocks()

                viewModelScope.launch {
                    stockRepository.getFourTopGainers()
                        .catch { e -> _fourTopGainersState.value =
                            UiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { gainers ->
                            _fourTopGainersState.value = UiState.Success(gainers)
                        }
                }

                viewModelScope.launch {
                    stockRepository.getFourTopLosers()
                        .catch { e -> _fourTopLosersState.value =
                            UiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { losers ->
                            _fourTopLosersState.value = UiState.Success(losers)
                        }
                }

            } catch (e: Exception) {
                _fourTopGainersState.value = UiState.Error(e.message ?: "Unknown error")
                _fourTopLosersState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadChartData(symbol: String) {
        // Check if data is already in cache
        chartDataCache[symbol]?.let {
            _chartData.value = it
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val chartData = stockRepository.getIntradayChartData(symbol, "5min")

                // Cache the chart data
                chartDataCache[symbol] = chartData

                _chartData.value = chartData
            } catch (e: Exception) {
                _error.value = "Failed to load chart data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }
}