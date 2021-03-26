package com.skylex_news_feed.news_feed.repos

import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.data.local.LocalDataSource
import com.skylex_news_feed.news_feed.data.remote.RemoteDataSource
import com.skylex_news_feed.news_feed.data.remote.ResponseKt
import com.skylex_news_feed.news_feed.util.API_KEY
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NewsRepo
@Inject constructor(
    remoteDataSource: RemoteDataSource,
    localDataSource: LocalDataSource
) {

    private val newsService = remoteDataSource.newsService
    private val newsDao = localDataSource.newsDao()

    fun getNewsInfo(id: String) : Maybe<News> {
        return newsDao.getNewsItem(id)
    }

    fun getLatestNews() : Observable<ResponseKt<List<News>>> {
        return object: NetworkOnlyResourceKt<List<News>>() {
            override fun saveCallResult(item: List<News>): Completable {
                return saveLatestNewsToDb(item)
            }

            override fun createNetworkRequest(): Maybe<List<News>> {
                return getLatestNewsFromNetwork()
            }

            override fun onFetchFromNetworkSuccessSideEffects() {
            }

            override fun onFetchFromNetworkErrorSideEffects() {
            }

        }.toObservable()
    }



    fun getLatestNewsFromNetwork(): Maybe<List<News>> {
        return newsService.getLatestNews(API_KEY)
            .map { it.items }
    }

    fun saveLatestNewsToDb(latestNews: List<News>): Completable {
        return Completable.concatArray(
            newsDao.deleteAll(),
            newsDao.insertAll(latestNews)
        )
    }
}