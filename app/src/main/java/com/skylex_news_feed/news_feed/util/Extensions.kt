package com.skylex_news_feed.news_feed.util

import android.util.TypedValue
import com.skylex_news_feed.news_feed.MyApplication

// Int extension functions
fun Int.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        MyApplication.instance.resources.displayMetrics
    ).toInt()
}
fun Float.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        MyApplication.instance.resources.displayMetrics
    ).toInt()
}

fun Double.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        MyApplication.instance.resources.displayMetrics
    ).toInt()
}