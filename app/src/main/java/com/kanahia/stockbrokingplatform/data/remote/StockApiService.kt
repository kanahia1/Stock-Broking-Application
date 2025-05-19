package com.kanahia.stockbrokingplatform.data.remote

import com.kanahia.stockbrokingplatform.data.model.StockOverviewResponse
import com.kanahia.stockbrokingplatform.data.model.StockResponse
import com.kanahia.stockbrokingplatform.data.model.StockSearchResponse
import com.yabu.livechart.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApiService {

    @GET("query")
    suspend fun getTopGainersLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): StockResponse

    @GET("query")
    suspend fun searchStocks(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): StockSearchResponse

    @GET("query")
    suspend fun getStockOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): StockOverviewResponse

    @GET("query")
    suspend fun getStockQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): Map<String, Any>

    @GET("query")
    suspend fun getIntradayData(
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("outputsize") outputSize: String = "compact",
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): Map<String, Any>

    @GET("query")
    suspend fun getDailyData(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("outputsize") outputSize: String = "compact",
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): Map<String, Any>

}