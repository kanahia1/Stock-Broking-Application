package com.kanahia.stockbrokingplatform.data.repository

import com.kanahia.stockbrokingplatform.data.local.dao.RecentStockDao
import com.kanahia.stockbrokingplatform.data.local.dao.StockDao
import com.kanahia.stockbrokingplatform.data.local.dao.WishlistDao
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.data.local.entity.WishlistedStock
import com.kanahia.stockbrokingplatform.data.local.entity.toDomainModel
import com.kanahia.stockbrokingplatform.data.local.entity.toEntity
import com.kanahia.stockbrokingplatform.data.model.StockOverviewResponse
import com.kanahia.stockbrokingplatform.data.model.StockSearchResponse
import com.kanahia.stockbrokingplatform.data.remote.StockApiService
import com.kanahia.stockbrokingplatform.domain.model.StockCategory
import com.kanahia.stockbrokingplatform.domain.model.StockChartData
import com.kanahia.stockbrokingplatform.domain.model.StockDetailModel
import com.kanahia.stockbrokingplatform.domain.model.StockModel
import com.kanahia.stockbrokingplatform.domain.repository.StockRepository
import com.kanahia.stockbrokingplatform.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val apiService: StockApiService,
    private val stockDao: StockDao,
    private val recentStockDao: RecentStockDao,
    private val wishlistDao: WishlistDao
) : StockRepository {

    companion object {
        private val CACHE_DURATION = TimeUnit.MINUTES.toMillis(15) // 15 minutes cache
    }

    override suspend fun refreshStocks() {
        withContext(Dispatchers.IO) {
            val shouldRefreshGainers = shouldRefreshCache(StockCategory.GAINER)
            val shouldRefreshLosers = shouldRefreshCache(StockCategory.LOSER)

            if (shouldRefreshGainers || shouldRefreshLosers) {
                try {
                    val response = apiService.getTopGainersLosers()
                    val currentTime = System.currentTimeMillis()

                    if (shouldRefreshGainers) {
                        val gainers = response.topGainers.map { stockItem ->
                            StockModel(
                                ticker = stockItem.ticker,
                                price = stockItem.price,
                                volume = stockItem.volume,
                                changeAmount = stockItem.changeAmount,
                                changePercentage = stockItem.changePercentage,
                                category = StockCategory.GAINER,
                                timestamp = System.currentTimeMillis(),
                            ).toEntity()
                        }
                        stockDao.deleteStocksByCategory(StockCategory.GAINER.name)
                        stockDao.insertStocks(gainers)
                    }

                    if (shouldRefreshLosers) {
                        val losers = response.topLosers.map { stockItem ->
                            StockModel(
                                ticker = stockItem.ticker,
                                price = stockItem.price,
                                volume = stockItem.volume,
                                changeAmount = stockItem.changeAmount,
                                changePercentage = stockItem.changePercentage,
                                category = StockCategory.LOSER,
                                timestamp = System.currentTimeMillis(),
                            ).toEntity()
                        }
                        stockDao.deleteStocksByCategory(StockCategory.LOSER.name)
                        stockDao.insertStocks(losers)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getAllTopGainers(): Flow<List<StockModel>> {
        return stockDao.getAllTopGainers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllTopLosers(): Flow<List<StockModel>> {
        return stockDao.getAllTopLosers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFourTopGainers(): Flow<List<StockModel>> {
        return stockDao.getTopFourGainers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFourTopLosers(): Flow<List<StockModel>> {
        return stockDao.getTopFourLosers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    private suspend fun shouldRefreshCache(category: StockCategory): Boolean {
        val lastUpdateTime = stockDao.getLastUpdateTime(category.name) ?: 0L
        return System.currentTimeMillis() - lastUpdateTime > CACHE_DURATION
    }

    override suspend fun searchStocks(query: String): StockSearchResponse {
        return apiService.searchStocks(
            keywords = query,
            apiKey = Constants.API_KEY
        )
    }

    override fun getRecentStocks(): Flow<List<RecentStock>> {
        return recentStockDao.getRecentStocks()
    }

    override fun getThreeRecentStocks(): Flow<List<RecentStock>> {
        return recentStockDao.getThreeRecentStocks()
    }

    override suspend fun saveRecentStock(stock: RecentStock) {
        recentStockDao.insertRecentStock(stock)
    }

    override suspend fun getStockDetails(symbol: String): StockDetailModel {
        return withContext(Dispatchers.IO) {
            val overviewResponse = apiService.getStockOverview(
                symbol = symbol,
                apiKey = Constants.API_KEY
            )

            val quoteResponse = apiService.getStockQuote(
                symbol = symbol,
                apiKey = Constants.API_KEY
            )

            val globalQuote = quoteResponse["Global Quote"] as? Map<*, *> ?: emptyMap<String, Any>()
            val currentPrice = (globalQuote["05. price"] as? String)?.toDoubleOrNull() ?: 0.0
            val changePercent = (globalQuote["10. change percent"] as? String) ?: "0%"
            val previousClose = (globalQuote["08. previous close"] as? String)?.toDoubleOrNull() ?: 0.0
            val priceChange = (globalQuote["09. change"] as? String)?.toDoubleOrNull() ?: 0.0;
            val open = (globalQuote["02. open"] as? String)?.toDoubleOrNull() ?: 0.0
            val dayHigh = (globalQuote["03. high"] as? String)?.toDoubleOrNull() ?: 0.0
            val dayLow = (globalQuote["04. low"] as? String)?.toDoubleOrNull() ?: 0.0

            mapToStockDetailModel(overviewResponse, currentPrice, changePercent, (previousClose < currentPrice), priceChange, open, dayLow, dayHigh)
        }
    }

    private fun mapToStockDetailModel(
        response: StockOverviewResponse,
        currentPrice: Double,
        changePercent: String,
        isPositive: Boolean,
        priceChange: Double,
        open: Double,
        dayLow: Double,
        dayHigh: Double
    ): StockDetailModel {
        return StockDetailModel(
            symbol = response.symbol,
            assetType = response.assetType,
            name = response.name,
            description = response.description,
            exchange = response.exchange,
            currency = response.currency,
            country = response.country,
            sector = response.sector,
            industry = response.industry,
            address = response.address,
            officialSite = response.officialSite,
            fiscalYearEnd = response.fiscalYearEnd,
            latestQuarter = response.latestQuarter,
            marketCap = response.marketCap.toDoubleOrNull() ?: 0.0,
            ebitda = response.ebitda.toDoubleOrNull() ?: 0.0,
            peRatio = response.peRatio.toDoubleOrNull() ?: 0.0,
            pegRatio = response.pegRatio.toDoubleOrNull() ?: 0.0,
            bookValue = response.bookValue.toDoubleOrNull() ?: 0.0,
            dividendPerShare = response.dividendPerShare.toDoubleOrNull() ?: 0.0,
            dividendYield = response.dividendYield.toDoubleOrNull() ?: 0.0,
            eps = response.eps.toDoubleOrNull() ?: 0.0,
            revenuePerShareTTM = response.revenuePerShareTTM.toDoubleOrNull() ?: 0.0,
            profitMargin = response.profitMargin.toDoubleOrNull() ?: 0.0,
            operatingMarginTTM = response.operatingMarginTTM.toDoubleOrNull() ?: 0.0,
            returnOnAssetsTTM = response.returnOnAssetsTTM.toDoubleOrNull() ?: 0.0,
            returnOnEquityTTM = response.returnOnEquityTTM.toDoubleOrNull() ?: 0.0,
            revenueTTM = response.revenueTTM.toDoubleOrNull() ?: 0.0,
            grossProfitTTM = response.grossProfitTTM.toDoubleOrNull() ?: 0.0,
            dilutedEPSTTM = response.dilutedEPSTTM.toDoubleOrNull() ?: 0.0,
            quarterlyEarningsGrowthYOY = response.quarterlyEarningsGrowthYOY.toDoubleOrNull() ?: 0.0,
            quarterlyRevenueGrowthYOY = response.quarterlyRevenueGrowthYOY.toDoubleOrNull() ?: 0.0,
            analystTargetPrice = response.analystTargetPrice.toDoubleOrNull() ?: 0.0,
            trailingPE = response.trailingPE.toDoubleOrNull() ?: 0.0,
            forwardPE = response.forwardPE.toDoubleOrNull() ?: 0.0,
            priceToSalesRatioTTM = response.priceToSalesRatioTTM.toDoubleOrNull() ?: 0.0,
            priceToBookRatio = response.priceToBookRatio.toDoubleOrNull() ?: 0.0,
            evToRevenue = response.evToRevenue.toDoubleOrNull() ?: 0.0,
            evToEBITDA = response.evToEBITDA.toDoubleOrNull() ?: 0.0,
            beta = response.beta.toDoubleOrNull() ?: 0.0,
            weekHigh52 = response.weekHigh52.toDoubleOrNull() ?: 0.0,
            weekLow52 = response.weekLow52.toDoubleOrNull() ?: 0.0,
            day50MovingAverage = response.day50MovingAverage.toDoubleOrNull() ?: 0.0,
            day200MovingAverage = response.day200MovingAverage.toDoubleOrNull() ?: 0.0,
            sharesOutstanding = response.sharesOutstanding.toDoubleOrNull() ?: 0.0,
            dividendDate = response.dividendDate,
            exDividendDate = response.exDividendDate,
            price = currentPrice,
            priceChangePercent = changePercent,
            isPositive = isPositive,
            priceChange = priceChange,
            open = open,
            dayLow = dayLow,
            dayHigh = dayHigh
        )
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getIntradayChartData(symbol: String, interval: String, outputSize: String): StockChartData {
        val response = apiService.getIntradayData(symbol = symbol, interval = interval, outputSize = outputSize)

        val metaData = response["Meta Data"] as Map<String, String>
        val timeSeriesKey = "Time Series ($interval)"
        val timeSeries = response[timeSeriesKey] as Map<String, Map<String, String>>

        val timePoints = mutableListOf<String>()
        val closeValues = mutableListOf<Float>()
        val openValues = mutableListOf<Float>()
        val highValues = mutableListOf<Float>()
        val lowValues = mutableListOf<Float>()
        val volumes = mutableListOf<Int>()

        val sortedEntries = timeSeries.entries.sortedBy { it.key }

        sortedEntries.forEach { (timestamp, data) ->
            timePoints.add(timestamp)
            closeValues.add(data["4. close"]?.toFloat() ?: 0f)
            openValues.add(data["1. open"]?.toFloat() ?: 0f)
            highValues.add(data["2. high"]?.toFloat() ?: 0f)
            lowValues.add(data["3. low"]?.toFloat() ?: 0f)
            volumes.add(data["5. volume"]?.toInt() ?: 0)
        }

        return StockChartData(
            timePoints = timePoints,
            closeValues = closeValues,
            openValues = openValues,
            highValues = highValues,
            lowValues = lowValues,
            volumes = volumes,
            interval = interval,
            symbol = symbol
        )
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getDailyChartData(symbol: String, outputSize: String): StockChartData {
        val response = apiService.getDailyData(symbol = symbol, outputSize = outputSize)

        val metaData = response["Meta Data"] as Map<String, String>
        val timeSeries = response["Time Series (Daily)"] as Map<String, Map<String, String>>

        val timePoints = mutableListOf<String>()
        val closeValues = mutableListOf<Float>()
        val openValues = mutableListOf<Float>()
        val highValues = mutableListOf<Float>()
        val lowValues = mutableListOf<Float>()
        val volumes = mutableListOf<Int>()

        val sortedEntries = timeSeries.entries.sortedBy { it.key }

        sortedEntries.forEach { (timestamp, data) ->
            timePoints.add(timestamp)
            closeValues.add(data["4. close"]?.toFloat() ?: 0f)
            openValues.add(data["1. open"]?.toFloat() ?: 0f)
            highValues.add(data["2. high"]?.toFloat() ?: 0f)
            lowValues.add(data["3. low"]?.toFloat() ?: 0f)
            volumes.add(data["5. volume"]?.toInt() ?: 0)
        }

        return StockChartData(
            timePoints = timePoints,
            closeValues = closeValues,
            openValues = openValues,
            highValues = highValues,
            lowValues = lowValues,
            volumes = volumes,
            interval = "daily",
            symbol = symbol
        )
    }

    override suspend fun addToWishlist(symbol: String) {
        val wishlistedStock = WishlistedStock(symbol = symbol)
        wishlistDao.addToWishlist(wishlistedStock)
    }

    override suspend fun removeFromWishlist(symbol: String) {
        wishlistDao.removeFromWishlist(symbol)
    }

    override suspend fun isStockWishlisted(symbol: String): Boolean {
        return wishlistDao.isStockWishlisted(symbol) > 0
    }

    override fun getWishlistedStocks(): Flow<List<WishlistedStock>> {
        return wishlistDao.getAllWishlistedStocks()
    }
}