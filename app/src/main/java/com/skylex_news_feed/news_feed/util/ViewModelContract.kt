package com.skylex_news_feed.news_feed.util

interface ViewModelContract<EVENT, STATE_CHANGE> {

    fun process(viewEvent: EVENT)
    fun reduceToViewState(stateChange: STATE_CHANGE)
}