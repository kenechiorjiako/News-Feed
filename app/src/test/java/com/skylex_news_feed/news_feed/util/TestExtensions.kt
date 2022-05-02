package com.skylex_news_feed.news_feed.util

import android.os.Looper.getMainLooper
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import com.skylex_news_feed.news_feed.SingleFragmentActivity
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertEquals
import org.robolectric.Shadows.shadowOf

fun View.performClickAndWait() {
    this.performClick()
    shadowOf(getMainLooper()).idle()
}

fun SingleFragmentActivity.setFragmentAndWait(fragment: Fragment) {
    setFragment(fragment)
    shadowOf(mainLooper).idle()
}

fun SingleFragmentActivity.replaceFragmentAndWait(fragment: Fragment) {
    replaceFragment(fragment)
    shadowOf(mainLooper).idle()
}

fun launch(testCode: (SingleFragmentActivity) -> Unit) {
    with(ActivityScenario.launch(SingleFragmentActivity::class.java)) {
        onActivity { testCode(it) }
    }
}

fun launch(fragment: Fragment, testCode: (SingleFragmentActivity) -> Unit) {
    launch {
        it.setFragmentAndWait(fragment)
        testCode(it)
    }
}

fun View.getTextForId(@IdRes id: Int) =
    findViewById<TextView>(id).text.toString()

fun View.assertVisible(id: Int) =
    assertEquals(View.VISIBLE, findViewById<View>(id).visibility)

fun View.assertNotVisible(id: Int) =
    assertFalse(findViewById<View>(id).isVisible)

fun View.performClick(id: Int) =
    findViewById<View>(id).performClick()

fun View.assertGone(id: Int) =
    assertEquals(View.GONE, findViewById<View>(id).visibility)