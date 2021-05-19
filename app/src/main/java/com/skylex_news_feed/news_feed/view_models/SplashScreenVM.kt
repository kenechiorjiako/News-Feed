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

    }


    override fun process(viewEvent: Event) {
        when(viewEvent) {
            is PageActive -> handlePageActiveEvent()
        }
    }

    /**
     * handles a {@code PageActive} event coming from a view.
     * starts a seven second delay, after which the a view navigation object is
     * sent to the view to observe and handle.
     */
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




    /**
     * Various events that can be dispatched from a view and handled by this viewModel
     */
    sealed class Event {
        object PageActive: Event()
    }

    /**
     * View Effects for the view to observe and handle
     */
    sealed class ViewEffect {
        object ShowLogo: ViewEffect()
    }

    /**
     * View navigations for the view to observe and handle
     */
    sealed class ViewNavigation {
        data class NavigateToFragment(val pageId: Int): ViewNavigation()
    }
}