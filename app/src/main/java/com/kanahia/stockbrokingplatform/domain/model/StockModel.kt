package com.kanahia.stockbrokingplatform.domain.model

enum class StockCategory {
    GAINER,
    LOSER
}

data class StockModel(
    val ticker: String,
    val price: String,
    val changeAmount: String,
    val changePercentage: String,
    val volume: String,
    val category: StockCategory,
    val timestamp: Long = System.currentTimeMillis()
)