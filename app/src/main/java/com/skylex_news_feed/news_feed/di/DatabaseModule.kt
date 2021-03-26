package com.skylex_news_feed.news_feed.di

import android.content.Context
import com.skylex_news_feed.news_feed.data.local.LocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    @Singleton @Provides
    fun providesLocalDatabase(@ApplicationContext context: Context): LocalDataSource {
        return LocalDataSource.getInstance(context)
    }
}