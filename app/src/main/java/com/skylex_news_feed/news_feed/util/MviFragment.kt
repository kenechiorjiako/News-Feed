package com.skylex_news_feed.news_feed.util

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class MviFragment<STATE, EFFECT, NAVIGATION, EVENT, STATE_CHANGE, ViewModel : MviViewModel<STATE, EFFECT, NAVIGATION, EVENT, STATE_CHANGE>>
    : Fragment() {

    abstract val viewModel : ViewModel
    protected val disposables: CompositeDisposable = CompositeDisposable()
    protected val mEvents: PublishSubject<EVENT> = PublishSubject.create()

    private val viewStateObserver = Observer<STATE> {
        Log.d(Companion.TAG, "observed viewState : $it")
        renderViewState(it)
    }
    private val viewEffectObserver = Observer<EFFECT> {
        Log.d(Companion.TAG, "observed viewEffect : $it")
        renderViewEffect(it)
    }
    private val viewNavigationObserver = Observer<NAVIGATION> {
        Log.d(Companion.TAG, "observed viewEffect : $it")
        handleViewNavigation(it)
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeEvents()

        viewModel.viewStates().observe(this, viewStateObserver)
        viewModel.viewEffects().observe(this, viewEffectObserver)
        viewModel.viewNavigations().observe(this, viewNavigationObserver)
    }


    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewHelperObjects()
        setupViews()
        setupViewListeners()

        if (!viewModel.firstLoadOccurred) {
            dispatchLoadPageEvent()
        }
    }

    private fun observeEvents() {
        disposables.add(mEvents.subscribe { event: EVENT ->
            viewModel.process(
                event
            )
        })
    }


    abstract fun setupViewHelperObjects()
    abstract fun setupViews()
    abstract fun setupViewListeners()

    abstract fun renderViewState(viewState: STATE)
    abstract fun renderViewEffect(viewEffect: EFFECT)
    abstract fun handleViewNavigation(viewNavigation: NAVIGATION)


    abstract fun dispatchLoadPageEvent()

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "MviActivity"
    }
}