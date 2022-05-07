package com.skylex_news_feed.news_feed.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.util.RxImmediateSchedulerRule
import com.skylex_news_feed.news_feed.util.getOrAwaitValue
import com.skylex_news_feed.news_feed.util.mockNewsItem
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import junit.framework.TestCase.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NewsDetailFragmentViewModelTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val rxJavaRule = RxImmediateSchedulerRule()

    private val mockNewsRepo = mockk<NewsRepo>()

    private val sut = NewsDetailFragmentVM(mockNewsRepo)

    @Test
    fun `Given viewModel, when process LoadPage Event, then viewModel should handle event properly`() {
        val newsId = "id"
        every { mockNewsRepo.getNewsInfo(newsId) } returns Maybe.just(mockNewsItem)

        sut.process(Event.LoadPageEvent(newsId))

        val viewState = sut.viewStates().getOrAwaitValue()

        verify { mockNewsRepo.getNewsInfo(newsId) }
        assertTrue(sut.firstLoadOccurred)
        assertEquals(
            viewState,
            ViewState(
                pageLoading = false,
                pageLoadError = null,
                pageRefreshing = false,
                newsItem = mockNewsItem
            )
        )
    }

    @Test
    fun `Given viewModel, when process LoadPage Event and error occurs, then viewModel should handle event properly`() {
        val newsId = "id"
        every { mockNewsRepo.getNewsInfo(newsId) } returns Maybe.error(Error())

        sut.process(Event.LoadPageEvent(newsId))

        val viewState = sut.viewStates().getOrAwaitValue()

        verify { mockNewsRepo.getNewsInfo(newsId) }
        assertFalse(sut.firstLoadOccurred)
        assertNotNull(viewState.pageLoadError)
    }


}