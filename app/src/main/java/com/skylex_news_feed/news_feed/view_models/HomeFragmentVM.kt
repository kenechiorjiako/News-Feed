package com.skylex_news_feed.news_feed.view_models

import android.util.Log
import android.widget.Toast
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.data.remote.ResponseKt
import com.skylex_news_feed.news_feed.repos.NewsRepo
import com.skylex_news_feed.news_feed.ui.HomeFragmentDirections
import com.skylex_news_feed.news_feed.util.MviViewModel
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.Event.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.PartialStateChange.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.ViewEffect.ShowToast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class HomeFragmentVM @ViewModelInject constructor(
    private val newsRepo: NewsRepo,
    @Assisted savedStateHandle: SavedStateHandle
) : MviViewModel<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange>() {

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
                    newsItems = stateChange.newsItems
                )
            }
            is PageLoadError -> {
                firstLoadOccurred = false
                viewState.copy(pageLoading = false, pageLoadError = stateChange.error)
            }
            is PageRefreshing -> {
                viewState.copy(pageRefreshing = stateChange.refreshing)
            }
        }
    }



    override fun process(viewEvent: Event) {
        when (viewEvent) {
            is LoadPageEvent -> handleLoadPageEvent()
            is NewsItemSelected -> handleItemSelected(viewEvent.news)
            is RefreshPage -> handleRefreshPageEvent()
        }
    }

    private fun handleLoadPageEvent() {
        reduceToViewState(LoadingPage)
        getNewsItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { reduceToViewState(PageLoaded(it)) },
                { reduceToViewState(PageLoadError(Throwable("Could not fetch data. Please try again."))) }
            )
    }
    private fun handleItemSelected(news: News) {
        val direction = HomeFragmentDirections.actionHomeFragmentToNewsDetailFragment(news.id)
        viewNavigation = ViewNavigation.NavigateToFragment(direction)
    }
    private fun handleRefreshPageEvent() {
        reduceToViewState(PageRefreshing(true))
        getNewsItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    reduceToViewState(PageRefreshing(false))
                    reduceToViewState(PageLoaded(it))
                },
                {
                    reduceToViewState(PageRefreshing(false))
                    viewEffect = ShowToast("Error refreshing page.", Toast.LENGTH_SHORT)
                }
            )
    }


    private fun getNewsItems(): Observable<List<News>> {
        val subject: BehaviorSubject<List<News>> = BehaviorSubject.create()

        val newsResponse: Observable<List<News>?> =
            newsRepo.getLatestNews()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { disposable1 -> disposables.add(disposable1) }
                .doOnNext { response ->
                    Log.d(
                        Companion.TAG,
                        "getNewsItems: got response with state ${response.detailedState}"
                    )
                    val error: Throwable? = response.error
                    val detailedState: ResponseKt.DetailedState? = response.detailedState
                    when {
                        detailedState === ResponseKt.DetailedState.ERROR_WITH_NO_DATA -> {
                            subject.onError(error)
                        }
                        detailedState === ResponseKt.DetailedState.EMPTY_RESPONSE -> {
                            subject.onError(Throwable("Error loading latest news, please try again."))
                        }
                        detailedState === ResponseKt.DetailedState.ERROR_WITH_DATA -> {
                            // TODO;
                        }
                    }
                }
                .filter { response -> response.data != null }
                .map { response -> response.data }
                .takeLast(1)
                .doOnNext { latestNews -> subject.onNext(latestNews) }
                .doOnComplete { subject.onComplete() }

        return subject.doOnSubscribe{ newsResponse.subscribe() }
    }



    data class ViewState(
        val pageLoading: Boolean = false,
        val pageLoadError: Throwable? = null,
        val pageRefreshing: Boolean = false,
        val newsItems: List<News> = emptyList()
    ) {}

    sealed class ViewEffect {
        data class ShowToast(val message: String, val length: Int): ViewEffect()
    }

    sealed class ViewNavigation {
        data class NavigateToFragment(val direction: NavDirections): ViewNavigation()
    }

    sealed class PartialStateChange {
        object LoadingPage: PartialStateChange()
        data class PageLoaded(val newsItems: List<News>): PartialStateChange()
        data class PageLoadError(val error: Throwable): PartialStateChange()
        data class PageRefreshing(val refreshing: Boolean): PartialStateChange()
    }

    sealed class Event {
        object LoadPageEvent : Event()
        data class NewsItemSelected(val news: News) : Event()
        object RefreshPage : Event()
    }

    companion object {
        private const val TAG = "HomeFragmentVM"
    }
}