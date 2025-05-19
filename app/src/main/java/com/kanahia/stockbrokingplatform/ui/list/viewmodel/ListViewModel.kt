package com.kanahia.stockbrokingplatform.ui.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import com.kanahia.stockbrokingplatform.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _allTopGainersState = MutableStateFlow<UiState<List<StockModel>>>(UiState.Loading)
    val allTopGainersState: StateFlow<UiState<List<StockModel>>> = _allTopGainersState

    private val _allTopLosersState = MutableStateFlow<UiState<List<StockModel>>>(UiState.Loading)
    val allTopLosersState: StateFlow<UiState<List<StockModel>>> = _allTopLosersState

    init {
        loadStocks()
    }

    private fun loadStocks() {
        viewModelScope.launch {
            try {
                // Refresh data from API
                stockRepository.refreshStocks()

                // Collect top gainers
                viewModelScope.launch {
                    stockRepository.getAllTopGainers()
                        .catch { e -> _allTopGainersState.value =
                            UiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { gainers -> _allTopGainersState.value = UiState.Success(gainers) }
                }

                // Collect top losers
                viewModelScope.launch {
                    stockRepository.getAllTopLosers()
                        .catch { e -> _allTopLosersState.value =
                            UiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { losers -> _allTopLosersState.value = UiState.Success(losers) }
                }

            } catch (e: Exception) {
                _allTopGainersState.value = UiState.Error(e.message ?: "Unknown error")
                _allTopLosersState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }
}