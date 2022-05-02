package com.skylex_news_feed.news_feed.util

import com.skylex_news_feed.news_feed.data.entities.News

val mockNewsItem = News(
    id = "1",
    title = "title",
    description = "description",
    url = "url",
    author = "author",
    image = "image",
    language = "English",
    category = listOf("finance", "education"),
    published = "today"
)

val mockNewsItems = listOf(
    mockNewsItem.copy("1"),
    mockNewsItem.copy("2"),
    mockNewsItem.copy("3"),
    mockNewsItem.copy("4"),
)