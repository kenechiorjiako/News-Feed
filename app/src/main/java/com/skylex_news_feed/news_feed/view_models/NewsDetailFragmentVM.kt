package com.skylex_news_feed.news_feed.view_models

import android.content.Intent
import com.skylex_news_feed.news_feed.MyApplication
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.ui.WebViewActivity
import com.skylex_news_feed.news_feed.util.MviViewModel
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.Event.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.PartialStateChange.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.ViewNavigation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class NewsDetailFragmentVM @Inject constructor(
    private val newsRepo: NewsRepo
): MviViewModel<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange>() {


    init {
        viewState = ViewState()
    }


    override fun reduceToViewState(stateChange: PartialStateChange) {
        viewState = when (stateChange) {
            is LoadingPage -> {
                firstLoadOccurred = true
                ViewState(pageLoading = true)
            }
            is PageLoaded -> {
                viewState.copy(
                    pageLoading = false,
                    pageLoadError = null,
                    newsItem = stateChange.newsItem
                )
            }
            is PageLoadError -> {
                firstLoadOccurred = false
                viewState.copy(pageLoading = false, pageLoadError = stateChange.error)
            }
        }
    }

    override fun process(viewEvent: Event) {
        when (viewEvent) {
            is LoadPageEvent -> handleLoadPageEvent(viewEvent.newsId)
            is ViewMoreClicked -> handleViewMoreClicked()
        }
    }


    /**
     * handles a ViewMoreClicked Event coming from a view and dispatches a navigation object
     * to be observed and handled by the view in return.
     */
    private fun handleViewMoreClicked() {
        val intent = Intent(MyApplication.instance.applicationContext, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.URL_KEY, viewState.newsItem?.url)
        viewNavigation = NavigateToActivity(intent)
    }

    /**
     * handle a {@code LoadPageEvent} coming from a view. This method fetches a news Item
     * corresponding with the {@code newsId} and makes changes to the view state in response.
     */
    private fun handleLoadPageEvent(newsId: String) {
        getNewsItem(newsId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                reduceToViewState(LoadingPage)
            }
            .subscribe(
                {
                    reduceToViewState(PageLoaded(it))
                },
                {
                    reduceToViewState(PageLoadError(Throwable("Error loading page.")))
                },
                {
                    reduceToViewState(PageLoadError(Throwable("Error loading page.")))
                }
            )
    }


    /**
     * gets a newsItem corresponding with the {@code newsId} from news repositery.
     */
    private fun getNewsItem(newsId: String): Maybe<News> {
        return newsRepo.getNewsInfo(newsId)
    }



    /**
     * the view state for the home fragment of this application
     *
     * @param pageLoading boolean flag indicating whether this page is loading fresh set of data.
     * @param pageLoadError null if there was no error loading this page
     * @param pageRefreshing boolean flag indicating whether or not this page is fetching new data.
     * @param newsItems news Items for the home fragment to display in a list
     */
    data class ViewState(
        val pageLoading: Boolean = false,
        val pageLoadError: Throwable? = null,
        val pageRefreshing: Boolean = false,
        val newsItem: News? = null
    ) {}

    /**
     * View Effects for the view to observe and handle
     */
    sealed class ViewEffect {
        data class ShowToast(val message: String, val length: Int): ViewEffect()
    }

    /**
     * View navigations for the view to observe and handle
     */
    sealed class ViewNavigation {
        data class NavigateToActivity(val intent: Intent): ViewNavigation()
    }

    /**
     * STATE_CHANGES that occur after the viewModel handles events.
     */
    sealed class PartialStateChange {
        object LoadingPage: PartialStateChange()
        data class PageLoaded(val newsItem: News): PartialStateChange()
        data class PageLoadError(val error: Throwable): PartialStateChange()
    }

    /**
     * Various events that can be dispatched from a view and handled by this viewModel
     */
    sealed class Event {
        data class LoadPageEvent(val newsId: String) : Event()
        object ViewMoreClicked: Event()
    }
}