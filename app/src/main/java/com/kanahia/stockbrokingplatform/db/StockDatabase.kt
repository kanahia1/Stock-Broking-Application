package com.kanahia.stockbrokingplatform.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kanahia.stockbrokingplatform.data.local.dao.RecentStockDao
import com.kanahia.stockbrokingplatform.data.local.dao.StockDao
import com.kanahia.stockbrokingplatform.data.local.dao.WishlistDao
import com.kanahia.stockbrokingplatform.data.local.entity.RecentStock
import com.kanahia.stockbrokingplatform.data.local.entity.StockEntity
import com.kanahia.stockbrokingplatform.data.local.entity.WishlistedStock

@Database(entities = [StockEntity::class, RecentStock::class, WishlistedStock::class], version = 3, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun recentStockDao(): RecentStockDao
    abstract fun wishlistDao(): WishlistDao
}