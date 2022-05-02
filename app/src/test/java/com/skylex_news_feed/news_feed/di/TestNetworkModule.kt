package com.skylex_news_feed.news_feed.di

import com.skylex_news_feed.news_feed.data.remote.services.NewsService
import com.skylex_news_feed.news_feed.data.remote.services.ServiceBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestNetworkModule {

    @Singleton
    @Provides
    fun providesNewsService(): NewsService {
        return ServiceBuilder.buildService(NewsService::class.java)
    }

}