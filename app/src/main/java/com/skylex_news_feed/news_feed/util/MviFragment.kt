package com.skylex_news_feed.news_feed.util

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 * An MVI Fragment representation. Built upon the foundation of MVVM.
 *
 * @param STATE the ViewState type of the the Fragment..
 * @param EFFECT the ViewEffect type of the fragment.
 * @param EVENT The ViewEvent type of the Fragment.
 */
abstract class MviFragment<STATE, EFFECT, NAVIGATION, EVENT, STATE_CHANGE, ViewModel : MviViewModel<STATE, EFFECT, NAVIGATION, EVENT, STATE_CHANGE>>
    : Fragment() {


    abstract val viewModel : ViewModel
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * An observable stream of events that are emitted from the UI such as user interactions.
     */
    protected val mEvents: PublishSubject<EVENT> = PublishSubject.create()

    /**
     * observes the current view state of the view model
     */
    private val viewStateObserver = Observer<STATE> {
        Log.d(Companion.TAG, "observed viewState : $it")
        renderViewState(it)
    }

    /**
     * observes view effects dispatched from the view model
     */
    private val viewEffectObserver = Observer<EFFECT> {
        Log.d(Companion.TAG, "observed viewEffect : $it")
        renderViewEffect(it)
    }

    /**
     * observes view navigation single events from the view model.
     */
    private val viewNavigationObserver = Observer<NAVIGATION> {
        Log.d(Companion.TAG, "observed viewEffect : $it")
        handleViewNavigation(it)
    }


    /**
     * Method call where view events are observed as well as view states, view effects and view
     * navigation changes.
     */
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
            // tell the view model to perform startup logic
            dispatchLoadPageEvent()
        }
    }

    /**
     * observe events coming from the UI that will then be dispatched to the viewModel for processing
     */
    private fun observeEvents() {
        disposables.add(mEvents.subscribe { event: EVENT ->
            viewModel.process(
                event
            )
        })
    }


    /**
     * setup helper objects that the view must interact with to function properly
     */
    abstract fun setupViewHelperObjects()

    /**
     * sets up all views such as recycler view and view pagers
     */
    abstract fun setupViews()

    /**
     * attaches listeners to views
     */
    abstract fun setupViewListeners()

    /**
     * called to render views based on the view State of this fragment.
     *
     * @param viewState The view state of the application.
     */
    abstract fun renderViewState(viewState: STATE)

    /**
     * called to render view effects based on the current view effect of the application.
     *
     * @param viewEffect of the fragment.
     */
    abstract fun renderViewEffect(viewEffect: EFFECT)

    /**
     * called to navigate to different screens based on the current view effect of the application.
     *
     * @param viewNavigation of the fragment.
     */
    abstract fun handleViewNavigation(viewNavigation: NAVIGATION)


    abstract fun dispatchLoadPageEvent()

    /**
     * cleans up the fragment when destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "MviActivity"
    }
}