package com.skylex_news_feed.news_feed.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.util.RxImmediateSchedulerRule
import com.skylex_news_feed.news_feed.util.getOrAwaitValue
import com.skylex_news_feed.news_feed.view_models.SplashScreenVM
import com.skylex_news_feed.news_feed.view_models.SplashScreenVM.ViewNavigation.NavigateToFragment
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SplashScreenViewModelTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private val sut = SplashScreenVM()

    @Test
    fun `Given viewModel, when PageActive event called, then handle pageActive event properly`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler}

        sut.process(SplashScreenVM.Event.PageActive)
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        val navigationValue = sut.viewNavigations().getOrAwaitValue()
        val viewEffect = sut.viewEffects().getOrAwaitValue()

        assertTrue(sut.firstLoadOccurred)
        assertEquals(viewEffect, SplashScreenVM.ViewEffect.ShowLogo)
        assertEquals(navigationValue, NavigateToFragment(R.id.homeFragment))

    }
}