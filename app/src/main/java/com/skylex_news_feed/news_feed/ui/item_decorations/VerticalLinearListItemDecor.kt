package com.skylex_news_feed.news_feed.ui.item_decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalLinearListItemDecor(
    private val top: Int,
    private val bottom: Int,
    private val middle: Int,
    private val start: Int,
    private val end: Int,
): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        outRect.left = start
        outRect.right = end

        outRect.top = if (parent.getChildLayoutPosition(view) == 0) {
            top
        } else {
            middle/2
        }

        outRect.bottom = if (parent.getChildLayoutPosition(view) == (parent.adapter!!.itemCount.minus(1))) {
            bottom
        } else {
            middle/2
        }
    }
}