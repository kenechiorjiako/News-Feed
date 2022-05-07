package com.skylex_news_feed.news_feed.viewmodels

import android.os.Looper
import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.data.remote.Response
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.ui.HomeFragmentDirections
import com.skylex_news_feed.news_feed.util.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.Event.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.ViewEffect.ShowToast
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.ViewNavigation.NavigateToFragment
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.ViewState
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class HomeFragmentViewModelTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private val mockNewsRepo = mockk<NewsRepo>()

    private var sut: HomeFragmentVM = HomeFragmentVM(mockNewsRepo)

    @Before
    fun setup() {
//        sut = HomeFragmentVM(mockNewsRepo)
    }


    @Test
    fun `Given viewModel, when process LoadPageEvent, then viewModel should handle event properly`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.SUCCESS(mockNewsItems))

        sut.process(LoadPage)

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val viewState = sut.viewStates().blockingObserve()

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(sut.firstLoadOccurred)
        assertEquals(
            viewState,
            ViewState(
                pageLoading = false,
                pageLoadError = null,
                pageRefreshing = false,
                newsItems = mockNewsItems
            )
        )
    }

    @Test
    fun `Given viewModel, when process LoadPageEvent and error occurs, then viewModel should handle event properly`() {
        val error = Throwable("Fetch error")
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.ERROR(error, null))

        sut.process(LoadPage)

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val viewState = sut.viewStates().getOrAwaitValue()

        assertFalse(sut.firstLoadOccurred)
        assertNotNull(viewState.pageLoadError)
    }

    @Test
    fun `Given viewModel, when process LoadPageEvent and no data is fetched, then viewModel should handle event properly`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.SUCCESS(null))

        sut.process(LoadPage)

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val viewState = sut.viewStates().blockingObserve()

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertFalse(sut.firstLoadOccurred)
        assertNotNull(viewState?.pageLoadError)
    }

    @Test
    fun `Given viewModel, when process LoadPageEvent and error occurs with data, then viewModel should handle event properly`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.ERROR(Throwable(), mockNewsItems))

        sut.process(LoadPage)

        val viewState = sut.viewStates().getOrAwaitValue()
        val viewEffect = sut.viewEffects().getOrAwaitValue()

        assertTrue(sut.firstLoadOccurred)
        assertEquals(
            viewState,
            ViewState(
                pageLoading = false,
                pageLoadError = null,
                pageRefreshing = false,
                newsItems = mockNewsItems
            )
        )
        assertEquals(
            viewEffect,
            ShowToast(
                "Unable to refresh feed. Please check your network and try again.",
                Toast.LENGTH_SHORT
            )
        )
    }

    @Test
    fun `Given viewModel, when process NewItemSelected, then viewModel should handle event properly`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.SUCCESS(null))

        sut.process(NewsItemSelected(mockNewsItem))

        val viewNavigation = sut.viewNavigations().getOrAwaitValue()

        assertEquals(
            viewNavigation,
            NavigateToFragment(HomeFragmentDirections.actionHomeFragmentToNewsDetailFragment2(mockNewsItem.id))
        )
    }

    @Test
    fun `Given viewModel, when process RefreshPageEvent and data is fetched, then viewModel should handle event properly`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.SUCCESS(mockNewsItems))

        sut.process(RefreshPage)

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val viewState = sut.viewStates().getOrAwaitValue()

        assertEquals(
            viewState,
            ViewState(
                pageLoading = false,
                pageLoadError = null,
                pageRefreshing = false,
                newsItems = mockNewsItems
            )
        )
    }

    @Test
    fun `Given viewModel, when process RefreshPageEvent and error occurs, then viewModel should handle event properly`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.error(Error())

        sut.process(RefreshPage)

        val viewEffect = sut.viewEffects().getOrAwaitValue()

        assertEquals(
            viewEffect,
            ShowToast("Error refreshing page.", Toast.LENGTH_SHORT)
        )
    }
}