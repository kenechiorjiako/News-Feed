package com.skylex_news_feed.news_feed.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.annotation.NavigationRes
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.SingleFragmentActivity
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Shadows

/**
 * Base class to be extended in order to reduce boilerplate for Robolectric tests of [Activity] using [ActivityScenario].
 * Uses DaggerMock rule to enable injecting of mocks with Dagger.
 */
@Ignore
@RunWith(AndroidJUnit4::class)
open class RobolectricActivityTestScenario<T : Activity>(private val type: Class<T>) {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val rxJavaRule = RxImmediateSchedulerRule()

    protected lateinit var intent: Intent

    @Before
    open fun setUp() {
        this.intent = Intent(ApplicationProvider.getApplicationContext<Context>(), type)
    }

    fun launch(): ActivityScenario<T> {
        return ActivityScenario.launch(type)
    }

    fun launchWithIntent(): ActivityScenario<T> {
        return ActivityScenario.launch(intent)
    }

    fun launchWithFragment(fragment: Fragment): ActivityScenario<T> {
        return ActivityScenario.launch(type).onActivity {
            it as SingleFragmentActivity
            it.setFragment(fragment)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }

    fun launchFragmentWithNavController(fragment: Fragment, @NavigationRes graph: Int, startDestination: Int): ActivityScenario<T> {
        return ActivityScenario.launch(type).onActivity {
            it as SingleFragmentActivity
            val testNavHostController = TestNavHostController(it)
            testNavHostController.setGraph(graph)
            testNavHostController.setCurrentDestination(startDestination)

            fragment.viewLifecycleOwnerLiveData.observeForever {
                Navigation.setViewNavController(fragment.requireView(), testNavHostController)
            }

            it.setFragment(fragment)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
