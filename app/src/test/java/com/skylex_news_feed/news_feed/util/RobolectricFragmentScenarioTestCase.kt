package com.skylex_news_feed.news_feed.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.Serializable

/**
 * Base class to be extended in order to reduce boilerplate for Robolectric tests of [Fragment] using [FragmentScenario].
 * Uses DaggerMock rule to enable injecting of mocks with Dagger.
 */
@Ignore
@RunWith(AndroidJUnit4::class)
open class RobolectricFragmentScenarioTestCase<T : Fragment>(private val type: Class<T>) {

    protected lateinit var bundle: Bundle

    @Before
    open fun setUp() {
        this.bundle = Bundle()
    }

    fun addExtraToBundle(key: String, value: Serializable) {
        bundle.putSerializable(key, value)
    }

    fun launch(): FragmentScenario<T> {
        return FragmentScenario.launch(type)
    }

    fun launchWithBundle(): FragmentScenario<T> {
        return FragmentScenario.launch(type, bundle)
    }

    @After
    fun tearDown() {
        Mockito.validateMockitoUsage();
    }
}