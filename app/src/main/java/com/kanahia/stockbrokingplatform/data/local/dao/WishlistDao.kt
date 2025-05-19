package com.kanahia.stockbrokingplatform.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanahia.stockbrokingplatform.data.local.entity.WishlistedStock
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(stock: WishlistedStock)

    @Query("DELETE FROM wishlisted_stocks WHERE symbol = :symbol")
    suspend fun removeFromWishlist(symbol: String)

    @Query("SELECT * FROM wishlisted_stocks ORDER BY timestamp DESC")
    fun getAllWishlistedStocks(): Flow<List<WishlistedStock>>

    @Query("SELECT COUNT(*) FROM wishlisted_stocks WHERE symbol = :symbol")
    suspend fun isStockWishlisted(symbol: String): Int
}