package com.skylex_news_feed.news_feed.ui

import android.os.Bundle
import androidx.core.view.isVisible
import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.SingleFragmentActivity
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.util.RobolectricActivityTestScenario
import com.skylex_news_feed.news_feed.util.mockNewsItem
import com.squareup.picasso.Picasso
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.*
import io.reactivex.rxjava3.core.Maybe
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class NewsDetailsFragmentTest: RobolectricActivityTestScenario<SingleFragmentActivity>(SingleFragmentActivity::class.java) {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    val mockNewsRepo = mockk<NewsRepo>()

    private val mockNewsId = "id"

    private val sut = NewsDetailFragment().apply {
        arguments = Bundle().apply {
            putString("newsId", mockNewsId)
        }
    }

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
    fun `Given fragment, when page loaded, views should show appropriately`() {
        every { mockNewsRepo.getNewsInfo(mockNewsId) } returns Maybe.just(mockNewsItem)

        launchFragmentWithNavController(sut, R.navigation.nav_graph, R.id.newsDetailFragment).onActivity {

            verify { mockNewsRepo.getNewsInfo(mockNewsId) }
            assertFalse(sut.binding.loadingErrorLayout.isVisible)
            assertTrue(sut.binding.appBarLayout.isVisible)
            assertTrue(sut.binding.dataLayout.isVisible)

            assertEquals(sut.binding.newsTitle.text, mockNewsItem.title)
            assertEquals(sut.binding.authorName.text, mockNewsItem.author)
            assertEquals(sut.binding.newsDescription.text, mockNewsItem.description)
        }
    }

    @Test
    fun `Given fragment, when page load error occurs, views should show appropriately`() {
        every { mockNewsRepo.getNewsInfo(mockNewsId) } returns Maybe.error(Error())

        launchFragmentWithNavController(sut, R.navigation.nav_graph, R.id.newsDetailFragment).onActivity {

            verify { mockNewsRepo.getNewsInfo(mockNewsId) }
            assertTrue(sut.binding.loadingErrorLayout.isVisible)
            assertFalse(sut.binding.appBarLayout.isVisible)
            assertFalse(sut.binding.dataLayout.isVisible)
            assertFalse(sut.binding.loadingIndicator.isVisible)
            assertTrue(sut.binding.errorTextView.isVisible)
            assertTrue(sut.binding.refreshButton.isVisible)
        }
    }
}