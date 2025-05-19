package com.kanahia.stockbrokingplatform.data.model

import com.google.gson.annotations.SerializedName

data class StockResponse(
    val metadata: String,
    @SerializedName("last_updated") val lastUpdated: String,
    @SerializedName("top_gainers") val topGainers: List<StockItem>,
    @SerializedName("top_losers") val topLosers: List<StockItem>,
    @SerializedName("most_actively_traded") val mostActivelyTraded: List<StockItem>
)

data class StockItem(
    val ticker: String,
    val price: String,
    @SerializedName("change_amount") val changeAmount: String,
    @SerializedName("change_percentage") val changePercentage: String,
    val volume: String
)