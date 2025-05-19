package com.kanahia.stockbrokingplatform.ui.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.data.local.entity.WishlistedStock
import com.kanahia.stockbrokingplatform.domain.model.StockChartData
import com.kanahia.stockbrokingplatform.domain.model.StockDetailModel
import com.kanahia.stockbrokingplatform.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _stockDetails = MutableStateFlow<StockDetailModel?>(null)
    val stockDetails: StateFlow<StockDetailModel?> = _stockDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _chartData = MutableStateFlow<StockChartData?>(null)
    val chartData: StateFlow<StockChartData?> = _chartData

    private val _chartTimeframe = MutableStateFlow("1D")
    val chartTimeframe: StateFlow<String> = _chartTimeframe

    private val _isWishlisted = MutableStateFlow(false)
    val isWishlisted: StateFlow<Boolean> = _isWishlisted


    fun loadStockDetails(symbol: String) {
        checkWishlistStatus(symbol)
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val stockDetail = stockRepository.getStockDetails(symbol)
                _stockDetails.value = stockDetail

                // Save to recent stocks
                saveToRecentStocks(stockDetail)

                loadChartData(symbol, "1D")

            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveToRecentStocks(stockDetail: StockDetailModel) {
        val recentStock = RecentStock(
            symbol = stockDetail.symbol,
            name = stockDetail.name,
            priceChange = stockDetail.priceChange.toString(),
            price = stockDetail.price.toString(),
            changePercent = stockDetail.priceChangePercent,
            isNegativeChange = !stockDetail.isPositive,
            timestamp = System.currentTimeMillis()
        )
        stockRepository.saveRecentStock(recentStock)
    }

    fun loadChartData(symbol: String, timeframe: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _chartTimeframe.value = timeframe

                _chartData.value = when (timeframe) {
                    "1D" -> stockRepository.getIntradayChartData(symbol, "5min")
                    "1W" -> stockRepository.getIntradayChartData(symbol, "30min", "full")
                    "1M" -> stockRepository.getDailyChartData(symbol)
                    "6M" -> stockRepository.getDailyChartData(symbol)
                    "1Y" -> stockRepository.getDailyChartData(symbol)
                    else -> stockRepository.getIntradayChartData(symbol, "5min")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load chart data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleWishlistStatus() {
        val symbol = _stockDetails.value?.symbol ?: return

        viewModelScope.launch {
            try {
                if (_isWishlisted.value) {
                    stockRepository.removeFromWishlist(symbol)
                } else {
                    stockRepository.addToWishlist(symbol)
                }
                // Update the state after the operation
                _isWishlisted.value = !_isWishlisted.value
            } catch (e: Exception) {
                _error.value = "Failed to update wishlist: ${e.message}"
            }
        }
    }

    fun checkWishlistStatus(symbol: String) {
        viewModelScope.launch {
            try {
                _isWishlisted.value = stockRepository.isStockWishlisted(symbol)
            } catch (e: Exception) {
                _error.value = "Failed to check wishlist status: ${e.message}"
            }
        }
    }

    fun getWishlistedStocks(): Flow<List<WishlistedStock>> {
        return stockRepository.getWishlistedStocks()
    }
}