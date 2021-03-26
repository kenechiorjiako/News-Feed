package com.skylex_news_feed.news_feed.view_models

import android.content.Intent
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import com.skylex_news_feed.news_feed.MyApplication
import com.skylex_news_feed.news_feed.WebViewActivity
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.util.MviViewModel
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.Event.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.PartialStateChange.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.ViewNavigation.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers

class NewsDetailFragmentVM @ViewModelInject constructor(
    private val newsRepo: NewsRepo,
    @Assisted savedStateHandle: SavedStateHandle
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

    private fun handleViewMoreClicked() {
        val intent = Intent(MyApplication.instance.applicationContext, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.URL_KEY, viewState.newsItem?.url)
        viewNavigation = NavigateToActivity(intent)
    }

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


    private fun getNewsItem(newsId: String): Maybe<News> {
        return newsRepo.getNewsInfo(newsId)
    }

    data class ViewState(
        val pageLoading: Boolean = false,
        val pageLoadError: Throwable? = null,
        val pageRefreshing: Boolean = false,
        val newsItem: News? = null
    ) {}

    sealed class ViewEffect {
        data class ShowToast(val message: String, val length: Int): ViewEffect()
    }

    sealed class ViewNavigation {
        data class NavigateToActivity(val intent: Intent): ViewNavigation()
    }

    sealed class PartialStateChange {
        object LoadingPage: PartialStateChange()
        data class PageLoaded(val newsItem: News): PartialStateChange()
        data class PageLoadError(val error: Throwable): PartialStateChange()
    }

    sealed class Event {
        data class LoadPageEvent(val newsId: String) : Event()
        object ViewMoreClicked: Event()
    }
}