package com.skylex_news_feed.news_feed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.util.DataConverter


/**
 * Local data source for the application
 */
@TypeConverters(DataConverter::class)
@Database(entities = [News::class], version = 1)
abstract class LocalDataSource : RoomDatabase() {

    abstract fun newsDao(): NewsDao

    companion object {
        fun getInstance(context: Context) : LocalDataSource {
            return Room.databaseBuilder(
                context.applicationContext,
                LocalDataSource::class.java, "app-database"
            ).build()
        }
    }
}