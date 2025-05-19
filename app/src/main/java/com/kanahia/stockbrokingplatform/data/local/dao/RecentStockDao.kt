package com.kanahia.stockbrokingplatform.data.local.dao

import androidx.room.*
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentStockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentStock(stock: RecentStock)

    @Query("SELECT * FROM recent_stocks ORDER BY timestamp DESC LIMIT 10")
    fun getRecentStocks(): Flow<List<RecentStock>>

    @Query("SELECT * FROM recent_stocks ORDER BY timestamp DESC LIMIT 3")
    fun getThreeRecentStocks(): Flow<List<RecentStock>>

    @Query("DELETE FROM recent_stocks WHERE symbol = :symbol")
    suspend fun deleteRecentStock(symbol: String)
}