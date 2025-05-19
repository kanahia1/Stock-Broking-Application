package com.kanahia.stockbrokingplatform.domain.repository

import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.data.local.entity.WishlistedStock
import com.kanahia.stockbrokingplatform.data.model.StockSearchResponse
import com.kanahia.stockbrokingplatform.domain.model.StockChartData
import com.kanahia.stockbrokingplatform.domain.model.StockDetailModel
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun refreshStocks()
    fun getAllTopGainers(): Flow<List<StockModel>>
    fun getAllTopLosers(): Flow<List<StockModel>>
    fun getFourTopGainers(): Flow<List<StockModel>>
    fun getFourTopLosers(): Flow<List<StockModel>>
    suspend fun searchStocks(query: String): StockSearchResponse
    fun getRecentStocks(): Flow<List<RecentStock>>
    fun getThreeRecentStocks(): Flow<List<RecentStock>>
    suspend fun saveRecentStock(stock: RecentStock)
    suspend fun getStockDetails(symbol: String): StockDetailModel
    suspend fun getIntradayChartData(symbol: String, interval: String, outputSize: String = "compact"): StockChartData
    suspend fun getDailyChartData(symbol: String, outputSize: String = "compact"): StockChartData
    suspend fun addToWishlist(symbol: String)
    suspend fun removeFromWishlist(symbol: String)
    suspend fun isStockWishlisted(symbol: String): Boolean
    fun getWishlistedStocks(): Flow<List<WishlistedStock>>
}