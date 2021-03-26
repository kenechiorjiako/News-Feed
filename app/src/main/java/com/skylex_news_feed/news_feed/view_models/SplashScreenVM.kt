package com.skylex_news_feed.news_feed.view_models

import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.util.MviViewModel
import com.skylex_news_feed.news_feed.view_models.SplashScreenVM.*
import com.skylex_news_feed.news_feed.view_models.SplashScreenVM.Event.*
import com.skylex_news_feed.news_feed.view_models.SplashScreenVM.ViewEffect.*
import com.skylex_news_feed.news_feed.view_models.SplashScreenVM.ViewNavigation.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class SplashScreenVM : MviViewModel<Any, ViewEffect, ViewNavigation, Event, Any>() {



    override fun reduceToViewState(stateChange: Any) {
        TODO("Not yet implemented")
    }


    override fun process(viewEvent: Event) {
        when(viewEvent) {
            is PageActive -> handlePageActiveEvent()
        }
    }
    private fun handlePageActiveEvent() {
        firstLoadOccurred = true
        disposables.add(
            Observable.timer(7, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewEffect = ShowLogo
                }
                .subscribe {
                    viewNavigation = NavigateToFragment(R.id.homeFragment)
                }
        )
    }




    sealed class Event {
        object PageActive: Event()
    }
    sealed class ViewEffect {
        object ShowLogo: ViewEffect()
    }
    sealed class ViewNavigation {
        data class NavigateToFragment(val pageId: Int): ViewNavigation()
    }
}