package com.kanahia.stockbrokingplatform.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlisted_stocks")
data class WishlistedStock(
    @PrimaryKey
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis()
)