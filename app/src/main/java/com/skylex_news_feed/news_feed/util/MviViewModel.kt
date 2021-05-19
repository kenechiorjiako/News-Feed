package com.skylex_news_feed.news_feed.util


import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 *  An MviViewModel is a ViewModel Type that helps dealing with state management in MVVM easier.
 *
 *  @param STATE Type of state associated with this ViewModel.
 *  @param EFFECT Type of effect associated with this ViewModel.
 *  @param NAVIGATION Type of NAVIGATION associated with this ViewModel
 *  @param EVENT Type of events associated with this ViewModel.
 *  @param STATE_CHANGE Type of state changes associated with this ViewModel
 */
abstract class MviViewModel<STATE, EFFECT, NAVIGATION, EVENT, STATE_CHANGE> : ViewModel() {

    /**
     * A composite disposable hosting all subscriptions to observable types within this viewModel.
     * It will be disposed when this view model is disposed to prevent memory leaks.
     */
    protected val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * a boolean flag indicating whether or not this view Model has undergone startup processes.
     */
    var firstLoadOccurred: Boolean = false
    protected set


    /**
     * A Mutable live data containing the latest view state of an activity or fragment.
     */
    private val _viewStates: MutableLiveData<STATE> = MutableLiveData()
    fun viewStates(): LiveData<STATE> = _viewStates

    private var _viewState : STATE? = null
    protected var viewState : STATE
        get() = _viewState ?: throw UninitializedPropertyAccessException("\"viewState\" was queried before being initialized")
        set(value) {
            Log.d(Companion.TAG, "setting viewState : $value")
            _viewState = value
            _viewStates.value = value
        }


    /**
     * A Single Live Event that hosts a viewEffect object that will be observed only once by an
     * activity or fragment
     */
    private val _viewEffects: SingleLiveEvent<EFFECT> = SingleLiveEvent()
    fun viewEffects(): SingleLiveEvent<EFFECT> = _viewEffects

    protected var viewEffect: EFFECT? = null
        set(value) {
            Log.d(TAG, "setting viewEffect : $value")
            _viewEffects.value = value
            field = value
        }



    /**
     * A Single Live Event that hosts a viewNavigation object that will be observed only once by an
     * activity or fragment
     */
    private val _viewNavigations: SingleLiveEvent<NAVIGATION> = SingleLiveEvent()
    fun viewNavigations(): SingleLiveEvent<NAVIGATION> = _viewNavigations

    protected var viewNavigation: NAVIGATION? = null
        set(value) {
            Log.d(TAG, "setting viewEffect : $value")
            field = value
            _viewNavigations.value = value
        }


    /**
     * Accepts a STATE_CHANGE object as an argument, uses it to make changes to the current
     * view state and dispatches this new view state to observers.
     */
    abstract fun reduceToViewState(stateChange: STATE_CHANGE)

    /**
     * Entry point for all events coming from an activity or fragment. Events should be processed
     * an the view model should make relevant changes to the view state
     */
    abstract fun process(viewEvent: EVENT)


    /**
     * disposes the current composite disposables hosted by this view model. This method should
     * be overridden to do further clean up if necessary
     */
    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "MviViewModel"
    }
}