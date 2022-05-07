package com.skylex_news_feed.news_feed.util

import android.content.res.Resources
import android.util.TypedValue
import com.skylex_news_feed.news_feed.MyApplication

/**
 *  A set of extension functions that convert an int, float and double to their pixel equivalent.
 */


fun Int.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
fun Float.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt()
}

fun Double.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}