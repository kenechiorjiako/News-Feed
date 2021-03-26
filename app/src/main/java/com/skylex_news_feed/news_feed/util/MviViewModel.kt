package com.skylex_news_feed.news_feed.util


import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class MviViewModel<STATE, EFFECT, NAVIGATION, EVENT, STATE_CHANGE> : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    var firstLoadOccurred: Boolean = false
    protected set



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



    private val _viewEffects: SingleLiveEvent<EFFECT> = SingleLiveEvent()
    fun viewEffects(): SingleLiveEvent<EFFECT> = _viewEffects

    protected var viewEffect: EFFECT? = null
        set(value) {
            Log.d(TAG, "setting viewEffect : $value")
            _viewEffects.value = value
            field = value
        }



    private val _viewNavigations: SingleLiveEvent<NAVIGATION> = SingleLiveEvent()
    fun viewNavigations(): SingleLiveEvent<NAVIGATION> = _viewNavigations

    protected var viewNavigation: NAVIGATION? = null
        set(value) {
            Log.d(TAG, "setting viewEffect : $value")
            field = value
            _viewNavigations.value = value
        }







    abstract fun reduceToViewState(stateChange: STATE_CHANGE)

    abstract fun process(viewEvent: EVENT)


    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    companion object {
        private const val TAG = "MviViewModel"
    }
}