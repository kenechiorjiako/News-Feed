package com.skylex_news_feed.news_feed.data.local

import androidx.room.*
import com.skylex_news_feed.news_feed.data.entities.News
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe


@Dao
interface NewsDao {

    @Query("SELECT * FROM news")
    fun getAll(): Maybe<List<News>>

    @Query("SELECT * FROM news WHERE id = :id")
    fun getNewsItem(id: String): Maybe<News>

    @Query("SELECT * FROM news WHERE id IN (:newsIds)")
    fun loadAllByIds(newsIds: IntArray): List<News>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg news: News)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(news: List<News>): Completable

    @Delete
    fun delete(news: News)

    @Query("DELETE FROM news")
    fun deleteAll(): Completable

}