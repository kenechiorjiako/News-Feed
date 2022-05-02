package com.skylex_news_feed.news_feed.repos


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.data.remote.Response
import com.skylex_news_feed.news_feed.util.RxImmediateSchedulerRule
import com.skylex_news_feed.news_feed.util.mockNewsItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NetworkBoundResourceTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private lateinit var sut: NetworkBoundResource<News, News>

    @Test
    fun `Given networkBoundResource, when should fetch is false and old data present, values emitted should be correct`() {


        sut = object: NetworkBoundResource<News, News>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: News): Completable {
                return Completable.complete()
            }

            override fun shouldFetch(data: News?): Single<Boolean> {
                return Single.just(false)
            }

            override fun loadFromDb(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }

            override fun createNetworkRequest(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }
        }

        sut.toObservable()
            .test()
            .await()
            .assertValueAt(0, Response.LOADING(null))
            .assertValueSequence(mutableListOf(Response.LOADING(null), Response.SUCCESS(mockNewsItem)))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `Given networkBoundResource, when should fetch is false and old data is not present, values emitted should be correct`() {

        sut = object: NetworkBoundResource<News, News>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: News): Completable {
                return Completable.complete()
            }

            override fun shouldFetch(data: News?): Single<Boolean> {
                return Single.just(false)
            }

            override fun loadFromDb(): Maybe<News> {
                return Maybe.empty()
            }

            override fun createNetworkRequest(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }
        }

        sut.toObservable()
            .test()
            .await()
            .assertValueSequence(mutableListOf(
                Response.LOADING(null),
                Response.SUCCESS(null)
            ))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `Given networkBoundResource, when should fetch is true and old data present, values emitted should be correct`() {

        sut = object: NetworkBoundResource<News, News>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: News): Completable {
                return Completable.complete()
            }

            override fun shouldFetch(data: News?): Single<Boolean> {
                return Single.just(true)
            }

            override fun loadFromDb(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }

            override fun createNetworkRequest(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }
        }

        sut.toObservable()
            .test()
            .await()
            .assertValueSequence(mutableListOf(
                Response.LOADING(null),
                Response.LOADING(mockNewsItem),
                Response.SUCCESS(mockNewsItem)
            ))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `Given networkBoundResource, when should fetch is true and old data is not present, values emitted should be correct`() {

        sut = object: NetworkBoundResource<News, News>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: News): Completable {
                return Completable.complete()
            }

            override fun shouldFetch(data: News?): Single<Boolean> {
                return Single.just(true)
            }

            override fun loadFromDb(): Maybe<News> {
                return Maybe.empty()
            }

            override fun createNetworkRequest(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }
        }

        sut.toObservable()
            .test()
            .await()
            .assertValueSequence(mutableListOf(
                Response.LOADING(null),
                Response.SUCCESS(null)
            ))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `Given networkBoundResource, when old data doesn't exist should fetch is true and error occurs fetching data, values emitted should be correct`() {

        val error = Error()

        sut = object: NetworkBoundResource<News, News>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: News): Completable {
                return Completable.complete()
            }

            override fun shouldFetch(data: News?): Single<Boolean> {
                return Single.just(true)
            }

            override fun loadFromDb(): Maybe<News> {
                return Maybe.empty()
            }

            override fun createNetworkRequest(): Maybe<News> {
                return Maybe.error(error)
            }
        }

        sut.toObservable()
            .test()
            .await()
            .assertValueSequence(mutableListOf(
                Response.LOADING(null),
                Response.ERROR(error, null)
            ))
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `Given networkBoundResource, when old data exists should fetch is true and error occurs fetching data, values emitted should be correct`() {

        val error = Error()

        sut = object: NetworkBoundResource<News, News>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: News): Completable {
                return Completable.complete()
            }

            override fun shouldFetch(data: News?): Single<Boolean> {
                return Single.just(true)
            }

            override fun loadFromDb(): Maybe<News> {
                return Maybe.just(mockNewsItem)
            }

            override fun createNetworkRequest(): Maybe<News> {
                return Maybe.error(error)
            }
        }

        sut.toObservable()
            .test()
            .await()
            .assertValueSequence(mutableListOf(
                Response.LOADING(null),
                Response.LOADING(mockNewsItem),
                Response.ERROR(error, mockNewsItem)
            ))
            .assertNoErrors()
            .assertComplete()
    }
}