package com.skylex_news_feed.news_feed.ui

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.SingleFragmentActivity
import com.skylex_news_feed.news_feed.util.RobolectricActivityTestScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit


@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class SplashScreenFragmentTest: RobolectricActivityTestScenario<SingleFragmentActivity>(SingleFragmentActivity::class.java) {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    private lateinit var sut: SplashScreenFragment

    @Before
    fun setup() {
        sut = SplashScreenFragment()
    }

    @Test
    fun `Given fragment, when view created, then`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        launch().onActivity {
            val testNavController = TestNavHostController(it)
            testNavController.setGraph(R.navigation.nav_graph)

            sut.viewLifecycleOwnerLiveData.observeForever {
                Navigation.setViewNavController(sut.requireView(), testNavController)
            }

            it.setFragment(sut)

            testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)
            Assert.assertNotNull(testNavController.currentDestination)
            Assert.assertEquals(R.id.homeFragment, testNavController.currentDestination?.id)
        }
    }
}