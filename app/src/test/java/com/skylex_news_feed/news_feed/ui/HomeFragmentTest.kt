package com.skylex_news_feed.news_feed.ui

import android.os.Looper
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.SingleFragmentActivity
import com.skylex_news_feed.news_feed.data.remote.Response
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.util.RobolectricActivityTestScenario
import com.skylex_news_feed.news_feed.util.mockNewsItems
import com.squareup.picasso.Picasso
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.*
import io.reactivex.rxjava3.core.Observable
import junit.framework.Assert
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class HomeFragmentTest: RobolectricActivityTestScenario<SingleFragmentActivity>(SingleFragmentActivity::class.java) {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    val mockNewsRepo = mockk<NewsRepo>()

    private val sut = HomeFragment()

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        mockkStatic(Picasso::class)
        val picassoMock = mockkClass(Picasso::class, relaxed = true)
        coEvery {
            Picasso.get()
        } returns picassoMock
    }

    @Test
    fun `Given fragment, when page loaded correctly, then proper views should show`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.SUCCESS(mockNewsItems))

        launchFragmentWithNavController(sut, R.navigation.nav_graph, R.id.homeFragment).onActivity {
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            verify { mockNewsRepo.getLatestNews() }
            assertTrue(sut.binding.swipeRefreshLayout.isVisible)
            assertFalse(sut.binding.loadingErrorLayout.isVisible)
            assertFalse(sut.binding.swipeRefreshLayout.isRefreshing)
        }
    }

    @Test
    fun `Given fragment, when page load error occurs, then proper views should show`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.ERROR(Throwable(), null))

        launchFragmentWithNavController(sut, R.navigation.nav_graph, R.id.homeFragment).onActivity {
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            verify { mockNewsRepo.getLatestNews() }
            assertFalse(sut.binding.swipeRefreshLayout.isVisible)
            assertTrue(sut.binding.loadingErrorLayout.isVisible)
            assertTrue(sut.binding.loadingIndicator.isGone)
            assertTrue(sut.binding.errorTextView.isVisible)
            assertTrue(sut.binding.refreshButton.isVisible)
        }
    }

    @Test
    fun `Given fragment, when item clicked, then news details fragment should open`() {
        every { mockNewsRepo.getLatestNews() } returns Observable.just(Response.SUCCESS(mockNewsItems))


        launch().onActivity {
            val testNavController = TestNavHostController(it)
            testNavController.setGraph(R.navigation.nav_graph)
            testNavController.setCurrentDestination(R.id.homeFragment)

            sut.viewLifecycleOwnerLiveData.observeForever {
                Navigation.setViewNavController(sut.requireView(), testNavController)
            }

            it.setFragment(sut)

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            sut.binding.recyclerView[0].performClick()

            Assert.assertNotNull(testNavController.currentDestination)
            Assert.assertEquals(R.id.newsDetailFragment, testNavController.currentDestination?.id)
        }
    }
}