package com.skylex_news_feed.news_feed.repos


import com.skylex_news_feed.news_feed.data.remote.Response
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject


abstract class NetworkBoundResource<ResultType, RequestType> {


    private val result: BehaviorSubject<Response<ResultType>> = BehaviorSubject.create()
    private val disposables = CompositeDisposable()


    /**
     *  The starting point for Network bound resource. Checks whether of not the resource should
     *  fetch fresh data from the network and dispatches responses accordingly.
     */
    private fun start() {
        result.onNext(Response.LOADING(null))
        val dbSource = loadFromDb()

        dbSource.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                disposables.add(
                    shouldFetch(null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { shouldFetch: Boolean ->
                            if (shouldFetch) {
                                fetchFromNetwork(dbSource)
                            } else {
                                result.onNext(Response.SUCCESS(null))
                                result.onComplete()
                            }
                        }
                )
            }
            .subscribe { oldData: ResultType ->
                disposables.add(
                    shouldFetch(oldData)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { shouldFetch: Boolean ->
                            if (shouldFetch) {
                                result.onNext(
                                    Response.LOADING(oldData)
                                )
                                fetchFromNetwork(dbSource)
                            } else {
                                result.onNext(
                                    Response.SUCCESS(oldData)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                        }
                )
            }
    }


    /**
     *  The backbone of this resource. It first dispatches data from the local db if available.
     *  It then proceeds to fetching data from the network, saves it locally in the database and
     *  dispatches the latest result to observers.
     *
     *  @param dbSource Observable which provides current local data if available.
     */
    private fun fetchFromNetwork(dbSource: Maybe<ResultType>) {
        val apiResponse: Maybe<RequestType> = createNetworkRequest()
        disposables.add(
            apiResponse.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { data: RequestType ->
                    onFetchFromNetworkSuccessSideEffects()
                    saveCallResult(data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete {
                            disposables.add(
                                loadFromDb()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnComplete {
                                        result.onNext(
                                            Response.ERROR(null, null)
                                        )
                                        result.onComplete()
                                        disposables.dispose()
                                    }
                                    .subscribe { newData: ResultType ->
                                        result.onNext(
                                            Response.SUCCESS(newData)
                                        )
                                        result.onComplete()
                                        disposables.dispose()
                                    }
                            )
                        }
                        .subscribe()
                }
                .doOnError { throwable: Throwable ->
                    onFetchFromNetworkErrorSideEffects()
                    disposables.add(
                        dbSource.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                result.onNext(
                                    Response.ERROR(throwable, null)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                            .subscribe { oldData: ResultType ->
                                result.onNext(
                                    Response.ERROR(throwable, oldData)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                    )
                }
                .doOnComplete {
                    disposables.add(
                        dbSource.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                result.onNext(
                                    Response.SUCCESS(null)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                            .subscribe { oldData: ResultType ->
                                result.onNext(
                                    Response.SUCCESS(oldData)
                                )
                                result.onComplete()
                                disposables.dispose()
                            }
                    )
                }
                .subscribe(
                    { },
                    { }
                )
        )
    }


    open fun toObservable(): Observable<Response<ResultType>> {
        return result.doOnSubscribe { start() }
            .doOnDispose { disposables.dispose() }
    }


    /**
     *  Called when this resource successfully fetches data from network
     */
    protected abstract fun onFetchFromNetworkSuccessSideEffects()


    /**
     * called when this resource unsuccessfully fetches data from network
     */
    protected abstract fun onFetchFromNetworkErrorSideEffects()


    /**
     *  saves network result to local database
     *
     *  @param item Result to save to db
     */
    protected abstract fun saveCallResult(item: RequestType): Completable


    /**
     *  checks whether or not the resource should fetch fresh data from network
     */
    protected abstract fun shouldFetch(data: ResultType?): Single<Boolean>

    /**
     * loads data from db
     */
    protected abstract fun loadFromDb(): Maybe<ResultType>

    /**
     * A call to load data from network
     */
    protected abstract fun createNetworkRequest(): Maybe<RequestType>
}