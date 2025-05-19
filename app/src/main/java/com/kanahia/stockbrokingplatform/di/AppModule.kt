package com.kanahia.stockbrokingplatform.di

import android.content.Context
import androidx.room.Room
import com.kanahia.stockbrokingplatform.data.local.dao.RecentStockDao
import com.kanahia.stockbrokingplatform.data.local.dao.StockDao
import com.kanahia.stockbrokingplatform.data.local.dao.WishlistDao
import com.kanahia.stockbrokingplatform.data.remote.StockApiService
import com.kanahia.stockbrokingplatform.db.StockDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideStockDatabase(@ApplicationContext context: Context): StockDatabase {
        return Room.databaseBuilder(
            context,
            StockDatabase::class.java,
            "stock_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStockDao(database: StockDatabase): StockDao {
        return database.stockDao()
    }

    @Provides
    @Singleton
    fun provideRecentStockDao(database: StockDatabase): RecentStockDao {
        return database.recentStockDao()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://alphavantage.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): StockApiService {
        return retrofit.create(StockApiService::class.java)
    }

    @Provides
    fun provideWishlistDao(database: StockDatabase): WishlistDao {
        return database.wishlistDao()
    }

}