package com.skylex_news_feed.news_feed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.databinding.FragmentHomeBinding
import com.skylex_news_feed.news_feed.ui.item_decorations.VerticalLinearListItemDecor
import com.skylex_news_feed.news_feed.ui.rv_adapters.NewsAdapter
import com.skylex_news_feed.news_feed.ui.rv_adapters.NewsAdapter.EventHandler
import com.skylex_news_feed.news_feed.util.MviFragment
import com.skylex_news_feed.news_feed.util.toPx
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.Event.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.ViewEffect.*
import com.skylex_news_feed.news_feed.view_models.HomeFragmentVM.ViewNavigation.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : MviFragment<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange, HomeFragmentVM>() {

    lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController

    override val viewModel: HomeFragmentVM by viewModels()
    private val rvAdapter : NewsAdapter = NewsAdapter( object: EventHandler {
        override fun onNewsItemClicked(news: News) {
            mEvents.onNext(NewsItemSelected(news))
        }
    } )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)


        return binding.root
    }


    override fun setupViewHelperObjects() {
        navController = NavHostFragment.findNavController(this)
    }
    override fun setupViews() {
        binding.apply {
            val itemDecoration = VerticalLinearListItemDecor(
                top = 16.toPx(),
                bottom = 24.toPx(),
                start = 16.toPx(),
                end = 16.toPx(),
                middle = 16.toPx()
            )
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.adapter = rvAdapter
            recyclerView.layoutManager = LinearLayoutManager(context).also { it.orientation = LinearLayoutManager.VERTICAL }
        }
    }
    override fun setupViewListeners() {
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                mEvents.onNext(RefreshPage)
            }

            refreshButton.setOnClickListener {
                mEvents.onNext(LoadPage)
            }
        }
    }


    override fun renderViewState(viewState: ViewState) {
        if (!viewState.pageLoading && viewState.pageLoadError == null) {
            renderPageLoaded(viewState)
        } else if (viewState.pageLoading) {
            renderPageLoading()
        } else if (viewState.pageLoadError != null) {
            renderPageError(viewState.pageLoadError)
        }
    }
    private fun renderPageLoaded(viewState: ViewState) {
        rvAdapter.setItems(viewState.newsItems)
        binding.apply {
            loadingErrorLayout.visibility = GONE
            swipeRefreshLayout.visibility = VISIBLE

            swipeRefreshLayout.isRefreshing = viewState.pageRefreshing
        }
    }
    private fun renderPageLoading() {
        binding.apply {
            loadingErrorLayout.visibility = VISIBLE
            swipeRefreshLayout.visibility = GONE

            loadingIndicator.visibility = VISIBLE
            errorTextView.visibility = GONE
            refreshButton.visibility = GONE
        }
    }
    private fun renderPageError(pageLoadError: Throwable) {
        binding.apply {
            loadingErrorLayout.visibility = VISIBLE
            swipeRefreshLayout.visibility = GONE

            loadingIndicator.visibility = GONE
            errorTextView.visibility = VISIBLE
            refreshButton.visibility = VISIBLE

            errorTextView.text = pageLoadError.message
        }
    }

    override fun renderViewEffect(viewEffect: ViewEffect) {
        when(viewEffect) {
            is ShowToast -> showToast(viewEffect.message, viewEffect.length)
        }
    }

    /**
     * Method to show a toast in the UI.
     *  @param message Message to show.
     *  @param length how long to show this toast.
     */
    private fun showToast(message: String, length: Int) {
        Toast.makeText(context, message, length).show()
    }

    override fun handleViewNavigation(viewNavigation: ViewNavigation) {
        when (viewNavigation) {
            is NavigateToFragment -> navigateToFragment(viewNavigation.direction)
        }
    }

    private fun navigateToFragment(direction: NavDirections) {
        navController.navigate(direction)
    }


    override fun dispatchLoadPageEvent() {
        mEvents.onNext(LoadPage)
    }
}