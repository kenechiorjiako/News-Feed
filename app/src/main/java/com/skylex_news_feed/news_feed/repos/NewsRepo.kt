package com.skylex_news_feed.news_feed.repos

import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.data.local.LocalDataSource
import com.skylex_news_feed.news_feed.data.remote.RemoteDataSource
import com.skylex_news_feed.news_feed.data.remote.Response
import com.skylex_news_feed.news_feed.util.API_KEY
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton


/**
 * A {@code NewsRepo}  for the application. Serves the purpose of being an access point for all
 * news data in the application. Both local and remote
 *
 * @param remoteDataSource data source for all news data on fetched from a network via Retrofit.
 *
 * @param localDataSource data source for all news data saved locally on a device via Room.
 */
@Singleton
class NewsRepo
@Inject constructor(
    remoteDataSource: RemoteDataSource,
    localDataSource: LocalDataSource
) {

    private val newsService = remoteDataSource.newsService
    private val newsDao = localDataSource.newsDao()

    /**
     * Gets one news item matching the given id from room db
     *
     * @param id id of the news item
     *
     * @return a Maybe observable for the item. It completes without data if not news item matching
     * the given id is not available in the db.
     */
    fun getNewsInfo(id: String) : Maybe<News> {
        return newsDao.getNewsItem(id)
    }

    /**
     * Gets the latest news items at the moment with the use of a {@see NetworkBoundResource}
     *
     * @return An observable which in itself dispatches several responses. It first of all dispatches
     * a response containing data cached in the database and proceeds to fetching the latest news
     * items from a remote source.
     */
    fun getLatestNews() : Observable<Response<List<News>>> {
        return object : NetworkBoundResource<List<News>, List<News>>() {
            override fun onFetchFromNetworkSuccessSideEffects() {

            }

            override fun onFetchFromNetworkErrorSideEffects() {

            }

            override fun saveCallResult(item: List<News>): Completable {
                return saveLatestNewsToDb(item)
            }

            override fun shouldFetch(data: List<News>?): Single<Boolean> {
                return Single.just(true);
            }

            override fun loadFromDb(): Maybe<List<News>> {
                return loadLatestNewsFromDb()
            }

            override fun createNetworkRequest(): Maybe<List<News>> {
                return getLatestNewsFromNetwork()
            }

        }.toObservable()
    }


    /**
     * Gets the latest news items from a remote source.
     *
     * @return Returns a Maybe observable containing latest news if available or completes successfully
     * if no data is availbable at the moment.
     */
    private fun getLatestNewsFromNetwork(): Maybe<List<News>> {
        return newsService.getLatestNews(API_KEY)
            .map { it.items }
    }

    /**
     * Saves latest news fetched from a remote source to the local database.
     *
     * @return Completable observable which completes successfully if data was cached successfully.
     */
    private fun saveLatestNewsToDb(latestNews: List<News>): Completable {
        return Completable.concatArray(
            newsDao.deleteAll(),
            newsDao.insertAll(latestNews)
        )
    }

    /**
     * loads latest news that is present in the local database.
     */
    private fun loadLatestNewsFromDb(): Maybe<List<News>> {
        return newsDao.getAll();
    }
}