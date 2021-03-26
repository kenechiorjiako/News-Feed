package com.skylex_news_feed.news_feed.data.remote.services

import com.skylex_news_feed.news_feed.data.remote.ApiListResponse
import com.skylex_news_feed.news_feed.data.entities.News
import io.reactivex.rxjava3.core.Maybe
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {

    @GET("latest-news")
    fun getLatestNews(@Query("apiKey") apiKey: String) : Maybe<ApiListResponse<News>>
}