package com.skylex_news_feed.news_feed.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.SingleFragmentActivity
import org.junit.Before
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.Shadows

/**
 * Base class to be extended in order to reduce boilerplate for Robolectric tests of [Activity] using [ActivityScenario].
 * Uses DaggerMock rule to enable injecting of mocks with Dagger.
 */
@Ignore
@RunWith(AndroidJUnit4::class)
open class RobolectricActivityTestScenario<T : Activity>(private val type: Class<T>) {

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
        if(type.superclass == SingleFragmentActivity::class.java) {
            return ActivityScenario.launch(type).onActivity {
                it as SingleFragmentActivity
                it.setFragment(fragment)
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        } else {
            throw IllegalArgumentException(String.format("Host activity must be of type SingleFragmentActivity, but is of type %s", type.toString()))
        }

    }
}
