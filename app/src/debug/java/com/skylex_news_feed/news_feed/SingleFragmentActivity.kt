package com.skylex_news_feed.news_feed

import android.os.Bundle
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@RestrictTo(RestrictTo.Scope.TESTS)
class SingleFragmentActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = RelativeLayout(this)
        content.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        content.id = R.id.root_view
        setContentView(content)
    }

    fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.root_view, fragment, "TEST")
            .commitNow()
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.root_view, fragment).commit()
    }
}