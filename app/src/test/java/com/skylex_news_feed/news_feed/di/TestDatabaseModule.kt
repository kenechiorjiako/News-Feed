package com.skylex_news_feed.news_feed.di

import android.content.Context
import com.skylex_news_feed.news_feed.data.local.LocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestDatabaseModule {

    @Singleton
    @Provides
    fun providesLocalDatabase(@ApplicationContext context: Context): LocalDataSource {
        return LocalDataSource.getInstance(context)
    }

}