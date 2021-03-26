package com.skylex_news_feed.news_feed.repos

import android.util.Log
import com.skylex_news_feed.news_feed.data.remote.ResponseKt
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject


abstract class NetworkOnlyResourceKt<RequestType> {

    companion object {
        private const val TAG = "NetworkBoundResourceKt"
    }

    private val result: BehaviorSubject<ResponseKt<RequestType>> = BehaviorSubject.create()
    private val disposables: CompositeDisposable = CompositeDisposable()



    private fun start() {
        result.onNext(ResponseKt.LOADING(null))
        fetchFromNetwork()
    }


    private fun fetchFromNetwork() {
        val apiResponse: Maybe<RequestType> = createNetworkRequest()
        disposables.add(
            apiResponse.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { data: RequestType ->
                        Log.d(
                            TAG,
                            "fetchFromNetwork: new data has been fetched and it is valid.... saving to db."
                        )
                        saveCallResult(data)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete(Action {
                                Log.d(
                                    TAG,
                                    "fetchFromNetwork: data successfully saved to db."
                                )
                                Log.d(
                                    TAG,
                                    "fetchFromNetwork: dispatching a successful response to observers with fresh data."
                                )
                                onFetchFromNetworkSuccessSideEffects()
                                result.onNext(
                                    ResponseKt.SUCCESS(data)
                                )
                                result.onComplete()
                                disposables.dispose()
                            })
                            .subscribe()
                    },
                    { throwable: Throwable? ->
                        Log.d(
                            TAG,
                            "fetchFromNetwork: an error occurred while trying to fetch data from our api = " + throwable!!.message
                        )
                        Log.d(
                            TAG,
                            "fetchFromNetwork: calling all side effects of an error occurring while trying to fetch data from our api"
                        )
                        onFetchFromNetworkErrorSideEffects()
                        result.onNext(ResponseKt.ERROR(throwable, null))
                        result.onComplete()
                        disposables.dispose()
                    },
                    {
                        Log.d(
                            TAG,
                            "fetchFromNetwork: we got no data from the api!"
                        )
                        result.onNext(ResponseKt.SUCCESS(null))
                        result.onComplete()
                        disposables.dispose()
                    }
                )
        )
    }


    open fun toObservable(): Observable<ResponseKt<RequestType>> {
        return result
            .doOnSubscribe { start() }
            .doOnDispose { disposables.dispose() }
    }




    protected abstract fun onFetchFromNetworkSuccessSideEffects()

    protected abstract fun onFetchFromNetworkErrorSideEffects()

    protected abstract fun saveCallResult(item: RequestType): Completable

    protected abstract fun createNetworkRequest(): Maybe<RequestType>
}