package com.skylex_news_feed.news_feed.data.remote

import com.skylex_news_feed.news_feed.data.remote.services.NewsService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for the application.
 */
@Singleton
class RemoteDataSource
@Inject constructor(
    val newsService: NewsService
)