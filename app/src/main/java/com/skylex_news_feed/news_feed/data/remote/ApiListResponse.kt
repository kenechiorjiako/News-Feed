package com.skylex_news_feed.news_feed.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Represents the response gotten from the News Api.
 */
data class ApiListResponse<T> (
    val status : String,
    @SerializedName("news")
    val items : List<T>
) {
}