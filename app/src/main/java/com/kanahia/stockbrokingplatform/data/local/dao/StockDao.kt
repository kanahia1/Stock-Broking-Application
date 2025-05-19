package com.kanahia.stockbrokingplatform.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanahia.stockbrokingplatform.data.local.entity.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    @Query("SELECT * FROM stocks WHERE category = 'GAINER' ORDER BY timestamp DESC LIMIT 20")
    fun getAllTopGainers(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE category = 'LOSER' ORDER BY timestamp DESC LIMIT 20")
    fun getAllTopLosers(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE category = 'GAINER' ORDER BY timestamp DESC LIMIT 4")
    fun getTopFourGainers(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE category = 'LOSER' ORDER BY timestamp DESC LIMIT 4")
    fun getTopFourLosers(): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Query("DELETE FROM stocks WHERE category = :category")
    suspend fun deleteStocksByCategory(category: String)

    @Query("SELECT MAX(timestamp) FROM stocks WHERE category = :category")
    suspend fun getLastUpdateTime(category: String): Long?
}