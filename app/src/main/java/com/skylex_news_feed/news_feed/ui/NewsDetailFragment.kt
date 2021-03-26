package com.skylex_news_feed.news_feed.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.databinding.FragmentNewsDetailBinding
import com.skylex_news_feed.news_feed.util.MviFragment
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.Event.*
import com.skylex_news_feed.news_feed.view_models.NewsDetailFragmentVM.ViewNavigation.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsDetailFragment() : MviFragment<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange, NewsDetailFragmentVM>() {

    override val viewModel: NewsDetailFragmentVM by viewModels()
    lateinit var binding: FragmentNewsDetailBinding
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupViewHelperObjects() {
        navController = NavHostFragment.findNavController(this)
    }
    override fun setupViews() {
        binding.apply {
            toolbar.setContentInsetsAbsolute(0, 0)
            loadingErrorLayoutToolbar.setContentInsetsAbsolute(0, 0)

            val onOffsetChangedListener = object : OnOffsetChangedListener {
                var isShow = false
                var scrollRange = -1

                override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.totalScrollRange
                    }
                    if (Math.abs(scrollRange + verticalOffset) == 0) {
                        pageTitle.text = resources.getText(R.string.app_name)
                        pageTitle.setTextColor(resources.getColor(R.color.colorWhite))
                        toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                        isShow = true
                    } else if (isShow) {
                        pageTitle.text = ""
                        toolbar.setBackgroundColor(resources.getColor(R.color.transparent))
                        isShow = false
                    }
                }
            }

            appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener)

        }
    }

    override fun setupViewListeners() {
        binding.apply {
            backButton.setOnClickListener {
                navigateUp()
            }
            loadingErrorLayoutBackButton.setOnClickListener {
                navigateUp()
            }
            viewMoreButton.setOnClickListener {
                mEvents.onNext(ViewMoreClicked)
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
        binding.apply {
            loadingErrorLayout.visibility = GONE
            appBarLayout.visibility = VISIBLE
            dataLayout.visibility = VISIBLE

            val news = viewState.newsItem
            if (news != null) {
                newsTitle.text = news.title
                authorName.text = news.author

                newsDescription.text = news.description

                newsImage.viewTreeObserver
                    .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            newsImage.viewTreeObserver
                                .removeOnPreDrawListener(this)
                            try {
                                Picasso.get()
                                    .load(news.image)
                                    .placeholder(R.color.color_image_placeholder)
                                    .centerCrop()
                                    .resize(
                                        binding.newsImage.measuredWidth,
                                        binding.newsImage.measuredHeight
                                    )
                                    .into(binding.newsImage, object : Callback {
                                        override fun onSuccess() {
                                            Log.d(TAG, "onSuccess: image loaded successfully")
                                        }

                                        override fun onError(e: java.lang.Exception?) {
                                            Log.d(TAG, "onError: error loading image")
                                        }

                                    })
                            } catch (e: Exception) {
                                Log.d(
                                    Companion.TAG,
                                    "onPreDraw: error occurred loading image"
                                )
                            }
                            return true
                        }
                    })

            }
        }
    }
    private fun renderPageLoading() {
        binding.apply {
            loadingErrorLayout.visibility = VISIBLE
            appBarLayout.visibility = GONE
            dataLayout.visibility = GONE

            loadingIndicator.visibility = VISIBLE
            errorTextView.visibility = GONE
            refreshButton.visibility = GONE
        }
    }
    private fun renderPageError(pageLoadError: Throwable) {
        binding.apply {
            loadingErrorLayout.visibility = VISIBLE
            appBarLayout.visibility = GONE
            dataLayout.visibility = GONE

            loadingIndicator.visibility = GONE
            errorTextView.visibility = VISIBLE
            refreshButton.visibility = VISIBLE

            errorTextView.text = pageLoadError.message
        }
    }

    override fun renderViewEffect(viewEffect: ViewEffect) {
        TODO("Not yet implemented")
    }

    override fun handleViewNavigation(viewNavigation: ViewNavigation) {
        when (viewNavigation) {
            is NavigateToActivity -> navigateToActivity(viewNavigation.intent)
        }
    }
    private fun navigateToActivity(intent: Intent) {
        startActivity(intent)
    }
    private fun navigateUp() = navController.navigateUp()


    override fun dispatchLoadPageEvent() {
        val newsId = arguments?.let { NewsDetailFragmentArgs.fromBundle(it).newsId }
        mEvents.onNext(newsId?.let { LoadPageEvent(it) })
    }

    companion object {
        private const val TAG = "NewsDetailFragment"
    }
}