package com.skylex_news_feed.news_feed.repos;

import android.util.Log;

import com.skylex_news_feed.news_feed.data.remote.Response;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public abstract class NetworkBoundResource<ResultType, RequestType> {

    private static final String TAG = "NetworkBoundResource";

    private BehaviorSubject<Response<ResultType>> result = BehaviorSubject.create();
    private CompositeDisposable disposables = new CompositeDisposable();





    private void start() {

        Log.d(TAG, "init: fetching data from database... dispatching loading state with null data value for the meantime");
        result.onNext(new Response.Builder<ResultType>().loading(null));

        Maybe<ResultType> dbSource = loadFromDb();

        disposables.add(
                dbSource.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(() -> {
                            disposables.add(
                                    shouldFetch(null).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe( shouldFetch -> {
                                                if (shouldFetch) {
                                                    Log.d(TAG, "init: there's no data in the db... fetching fresh data from api");
                                                    fetchFromNetwork(dbSource);
                                                    Log.d(TAG, "init: calling all side effects of fetching new data from api");
                                                    onFetchFromNetworkSuccessSideEffects();
                                                }
                                            })
                            );

                        })
                        .subscribe(oldData -> {
                            disposables.add(
                                    shouldFetch(oldData).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe( shouldFetch -> {
                                                if (shouldFetch) {

                                                    Log.d(TAG, "init: dispatching loading state with data currently from db");
                                                    result.onNext(new Response.Builder<ResultType>().loading(oldData));

                                                    Log.d(TAG, "init: data from the database is not up to date... fetching fresh data from api");
                                                    fetchFromNetwork(dbSource);

                                                    Log.d(TAG, "init: calling all side effects of fetching new data from api");
                                                    onFetchFromNetworkSuccessSideEffects();

                                                } else {
                                                    Log.d(TAG, "init: data is valid and up to date, dispatching a successful response to observers with data from db.");
                                                    result.onNext(new Response.Builder<ResultType>().success(oldData));
                                                    result.onComplete();
                                                    disposables.dispose();
                                                }
                                            })
                            );
                        })
        );
    }


    private void fetchFromNetwork(Maybe<ResultType> dbSource) {

        Maybe<RequestType> apiResponse = createNetworkRequest();

        disposables.add(
                apiResponse.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(data -> {

                    Log.d(TAG, "fetchFromNetwork: new data has been fetched and it is valid.... saving to db.");
                    saveCallResult(data)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete(() -> {
                                Log.d(TAG, "fetchFromNetwork: data successfully saved to db.");
                                disposables.add(
                                        loadFromDb()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnSubscribe(disposable -> Log.d(TAG, "fetchFromNetwork: fetching updated data from db"))
                                        .doOnComplete(() -> {
                                            Log.d(TAG, "fetchFromNetwork: dispatching an error response to observers with null data. Database is somehow empty... I'm confused");
                                            result.onNext(new Response.Builder<ResultType>().error(new Throwable("Could not Fetch data"), null));
                                            result.onComplete();
                                            disposables.dispose();
                                        })
                                        .subscribe(newData -> {
                                            Log.d(TAG, "fetchFromNetwork: dispatching a successful response to observers with fresh data.");
                                            result.onNext(new Response.Builder<ResultType>().success(newData));
                                            result.onComplete();
                                            disposables.dispose();
                                        })
                                );
                            })
                            .subscribe();

                })
                .doOnError(throwable -> {

                    Log.d(TAG, "fetchFromNetwork: an error occurred while trying to fetch data from our api = " + throwable.getMessage());
                    Log.d(TAG, "fetchFromNetwork: calling all side effects of an error occurring while trying to fetch data from our api");
                    onFetchFromNetworkErrorSideEffects();
                    disposables.add(
                            dbSource.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete(() -> {
                                Log.d(TAG, "fetchFromNetwork: there's no data in the database. returning an error with null value to observers.");
                                result.onNext(new Response.Builder<ResultType>().error(throwable, null));
                                result.onComplete();
                                disposables.dispose();
                            })
                            .subscribe(oldData -> {
                                Log.d(TAG, "fetchFromNetwork: dispatching new error response due to error. Sending throwable and oldData from the db to observers.");
                                result.onNext(new Response.Builder<ResultType>().error(throwable, oldData));
                                result.onComplete();
                                disposables.dispose();
                            })
                    );

                })
                .doOnComplete(() -> {

                    Log.d(TAG, "fetchFromNetwork: we got no data from the api!");
                    disposables.add(
                            dbSource.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnComplete(() -> {
                                        Log.d(TAG, "fetchFromNetwork: there's no data in the database. returning a successful response with null value to observers.");
                                        result.onNext(new Response.Builder<ResultType>().success(null));
                                        result.onComplete();
                                        disposables.dispose();
                                    })
                                    .subscribe(oldData -> {
                                        Log.d(TAG, "fetchFromNetwork: dispatching a success response with old data from the database, whatever it is.");
                                        result.onNext(new Response.Builder<ResultType>().success(oldData));
                                        result.onComplete();
                                        disposables.dispose();
                                    })
                    );

                })
                .subscribe(
                        requestType -> {},
                        throwable -> {}
                )
        );

    }


    public Observable<Response<ResultType>> toObservable() {
        return result.doOnSubscribe(disposable -> start())
                .doOnDispose(() -> disposables.dispose());
    }



    protected abstract void onFetchFromNetworkSuccessSideEffects();

    protected abstract void onFetchFromNetworkErrorSideEffects();

    protected abstract Completable saveCallResult(RequestType item);

    protected abstract Single<Boolean> shouldFetch(ResultType data);

    protected abstract Maybe<ResultType> loadFromDb();

    protected abstract Maybe<RequestType> createNetworkRequest();
}
