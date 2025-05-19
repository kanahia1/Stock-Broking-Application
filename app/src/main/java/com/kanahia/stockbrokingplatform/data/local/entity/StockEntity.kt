package com.kanahia.stockbrokingplatform.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kanahia.stockbrokingplatform.domain.model.StockCategory
import com.kanahia.stockbrokingplatform.domain.model.StockModel

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey
    val ticker: String,
    val price: String,
    val changeAmount: String,
    val changePercentage: String,
    val volume: String,
    val category: String,
    val timestamp: Long
)

fun StockEntity.toDomainModel(): StockModel {
    return StockModel(
        ticker = ticker,
        price = price,
        changeAmount = changeAmount,
        changePercentage = changePercentage,
        volume = volume,
        category = StockCategory.valueOf(category),
        timestamp = timestamp
    )
}

fun StockModel.toEntity(): StockEntity {
    return StockEntity(
        ticker = ticker,
        price = price,
        changeAmount = changeAmount,
        changePercentage = changePercentage,
        volume = volume,
        category = category.name,
        timestamp = timestamp
    )
}