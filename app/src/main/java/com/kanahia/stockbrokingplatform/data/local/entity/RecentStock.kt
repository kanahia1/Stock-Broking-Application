package com.kanahia.stockbrokingplatform.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_stocks")
data class RecentStock(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val price: String,
    val priceChange: String = "0.0",
    val changePercent: String = "0.0",
    val isNegativeChange: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)