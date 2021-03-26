package com.skylex_news_feed.news_feed.repos

import android.util.Log
import com.skylex_news_feed.news_feed.data.remote.ResponseKt
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject


abstract class NetworkBoundResourceKt<ResultType, RequestType> {

    companion object {
        private const val TAG = "NetworkBoundResourceKt"
    }

    private val result: BehaviorSubject<ResponseKt<ResultType>> = BehaviorSubject.create()
    private val disposables: CompositeDisposable = CompositeDisposable()



    private fun start() {
        Log.d(
            TAG,
            "start: fetching data from database... dispatching loading state with null data value for the meantime"
        )
        result.onNext(ResponseKt.LOADING(null))
        val dbSource = loadFromDb()

        dbSource.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(Action {
                disposables.add(
                    shouldFetch(null)!!.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer { shouldFetch: Boolean ->
                            if (shouldFetch) {
                                Log.d(
                                    TAG,
                                    "init: there's no data in the db... fetching fresh data from api"
                                )
                                fetchFromNetwork(dbSource)
                            }
                        })
                )
            })
            .subscribe(Consumer { oldData: ResultType ->
                disposables.add(
                    shouldFetch(oldData)!!.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer { shouldFetch: Boolean ->
                            if (shouldFetch) {
                                Log.d(
                                    TAG,
                                    "init: dispatching loading state with data currently from db"
                                )
                                result.onNext(
                                    ResponseKt.LOADING(oldData)
                                )
                                Log.d(
                                    TAG,
                                    "init: data from the database is not up to date... fetching fresh data from api"
                                )
                                fetchFromNetwork(dbSource)
                            } else {
                                Log.d(
                                    TAG,
                                    "init: data is valid and up to date, dispatching a successful response to observers with data from db."
                                )
                                result.onNext(
                                    ResponseKt.SUCCESS(oldData)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                        })
                )
            })
    }


    private fun fetchFromNetwork(dbSource: Maybe<ResultType>) {
        val apiResponse: Maybe<RequestType>? = createNetworkRequest()
        disposables.add(
            apiResponse!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { data: RequestType ->
                    Log.d(
                        TAG,
                        "fetchFromNetwork: new data has been fetched and it is valid.... saving to db."
                    )
                    Log.d(
                        TAG,
                        "init: calling all side effects of fetching new data from api"
                    )
                    onFetchFromNetworkSuccessSideEffects()
                    saveCallResult(data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(Action {
                            Log.d(
                                TAG,
                                "fetchFromNetwork: data successfully saved to db."
                            )
                            disposables.add(
                                loadFromDb()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnSubscribe(Consumer { disposable: Disposable? ->
                                        Log.d(
                                            TAG,
                                            "fetchFromNetwork: fetching updated data from db"
                                        )
                                    })
                                    .doOnComplete(Action {
                                        Log.d(
                                            TAG,
                                            "fetchFromNetwork: dispatching an error response to observers with null data. Database is somehow empty... I'm confused"
                                        )
                                        result.onNext(
                                            ResponseKt.ERROR(null, null)
                                        )
                                        result.onComplete()
                                        disposables.dispose()
                                    })
                                    .subscribe(Consumer { newData: ResultType ->
                                        Log.d(
                                            TAG,
                                            "fetchFromNetwork: dispatching a successful response to observers with fresh data."
                                        )
                                        result.onNext(
                                            ResponseKt.SUCCESS(newData)
                                        )
                                        result.onComplete()
                                        disposables.dispose()
                                    })
                            )
                        })
                        .subscribe()
                }
                .doOnError { throwable: Throwable ->
                    Log.d(
                        TAG,
                        "fetchFromNetwork: an error occurred while trying to fetch data from our api = " + throwable.message
                    )
                    Log.d(
                        TAG,
                        "fetchFromNetwork: calling all side effects of an error occurring while trying to fetch data from our api"
                    )
                    onFetchFromNetworkErrorSideEffects()
                    disposables.add(
                        dbSource.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                Log.d(
                                    TAG,
                                    "fetchFromNetwork: there's no data in the database. returning an error with null value to observers."
                                )
                                result.onNext(
                                    ResponseKt.ERROR(throwable, null)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                            .subscribe { oldData: ResultType ->
                                Log.d(
                                    TAG,
                                    "fetchFromNetwork: dispatching new error response due to error. Sending throwable and oldData from the db to observers."
                                )
                                result.onNext(
                                    ResponseKt.ERROR(throwable, oldData)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                    )
                }
                .doOnComplete {
                    Log.d(
                        TAG,
                        "fetchFromNetwork: we got no data from the api!"
                    )
                    disposables.add(
                        dbSource.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                Log.d(
                                    TAG,
                                    "fetchFromNetwork: there's no data in the database. returning a successful response with null value to observers."
                                )
                                result.onNext(
                                    ResponseKt.SUCCESS(null)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                            .subscribe { oldData: ResultType ->
                                Log.d(
                                    TAG,
                                    "fetchFromNetwork: dispatching a success response with old data from the database, whatever it is."
                                )
                                result.onNext(
                                    ResponseKt.SUCCESS(oldData)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                    )
                }
                .subscribe(
                    { requestType: RequestType -> },
                    { throwable: Throwable? -> }
                )
        )
    }


    open fun toObservable(): Observable<ResponseKt<ResultType>> {
        return result.doOnSubscribe(Consumer { disposable: Disposable? -> start() })
            .doOnDispose(Action { disposables.dispose() })
    }




    protected abstract fun onFetchFromNetworkSuccessSideEffects()

    protected abstract fun onFetchFromNetworkErrorSideEffects()

    protected abstract fun saveCallResult(item: RequestType): Completable

    protected abstract fun shouldFetch(data: ResultType?): Single<Boolean>

    protected abstract fun loadFromDb(): Maybe<ResultType>

    protected abstract fun createNetworkRequest(): Maybe<RequestType>
}