package com.skylex_news_feed.news_feed.repos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.data.local.LocalDataSource
import com.skylex_news_feed.news_feed.data.local.NewsDao
import com.skylex_news_feed.news_feed.data.remote.ApiListResponse
import com.skylex_news_feed.news_feed.data.remote.RemoteDataSource
import com.skylex_news_feed.news_feed.data.remote.Response
import com.skylex_news_feed.news_feed.data.remote.services.NewsService
import com.skylex_news_feed.news_feed.util.RxImmediateSchedulerRule
import com.skylex_news_feed.news_feed.util.mockNewsItem
import com.skylex_news_feed.news_feed.util.mockNewsItems
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsRepoTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private val mockRemoteDataSource = mockk<RemoteDataSource>()
    private val mockLocalDataSource = mockk<LocalDataSource>()

    private val mockNewsService = mockk<NewsService>()
    private val mockNewsDao = mockk<NewsDao>()

    private lateinit var sut: NewsRepo

    @Before
    fun setup() {
        every { mockRemoteDataSource.newsService } returns mockNewsService
        every { mockLocalDataSource.newsDao() } returns mockNewsDao

        sut = NewsRepo(mockRemoteDataSource, mockLocalDataSource)
    }

    @Test
    fun testGetNewsInfo() {
        val newsId = "id"
        every { mockNewsDao.getNewsItem(newsId) } returns Maybe.just(mockNewsItem)

        sut.getNewsInfo(newsId)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(mockNewsItem)

        verify { mockNewsDao.getNewsItem(newsId) }
    }

    @Test
    fun testLatestNews() {
        every { mockNewsDao.getAll() } returns Maybe.just(mockNewsItems)
        every { mockNewsDao.deleteAll() } returns Completable.complete()
        every { mockNewsDao.insertAll(mockNewsItems) } returns Completable.complete()

        every { mockNewsService.getLatestNews(any()) } returns Maybe.just(ApiListResponse("new", mockNewsItems))

        sut.getLatestNews()
            .test()
            .await()
            .assertValueSequence(mutableListOf(
                Response.LOADING(null),
                Response.LOADING(mockNewsItems),
                Response.SUCCESS(mockNewsItems)
            ))
            .assertNoErrors()
            .assertComplete()
    }
}