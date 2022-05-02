package com.skylex_news_feed.news_feed.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.data.local.LocalDataSource
import com.skylex_news_feed.news_feed.data.local.NewsDao
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NewsDaoTest {
    private lateinit var localDataSource: LocalDataSource
    private lateinit var newsDao: NewsDao

    private val baseNewsItem = News(
        id = "1",
        title = "title",
        description = "description",
        url = "url",
        author = "author",
        image = "image",
        language = "English",
        category = listOf("finance", "education"),
        published = "today"
    )

    private val newsItems = listOf(
        baseNewsItem.copy("1"),
        baseNewsItem.copy("2"),
        baseNewsItem.copy("3"),
        baseNewsItem.copy("4"),
    )

    @Before
    fun setup() {
        localDataSource = LocalDataSource.getInstance(ApplicationProvider.getApplicationContext())
        newsDao = localDataSource.newsDao()
    }

    @After
    @Throws(IOException::class)
    fun cleanUp() {
        localDataSource.close()
    }

    @Test
    fun testInsertAllAndGetAll() {

        newsDao.insertAll(newsItems)
            .test()

        newsDao.getAll()
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(newsItems)
    }

    @Test
    fun testGetNewsItem() {
        newsDao.insertAll(newsItems)
            .test()

        val resultId = "1"
        newsDao.getNewsItem(resultId)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(baseNewsItem.copy(id = resultId))
    }

    @Test
    fun testLoadAllByIds() {
        newsDao.insertAll(newsItems).subscribe()

        val newsIds = newsItems.map { it.id }
        val items = newsDao.loadAllByIds(newsIds)

        assertEquals(newsItems, items)
    }

    @Test
    fun testDelete() {
        newsDao.insertAll(newsItems).subscribe()
        newsDao.delete(newsItems[0])

        newsDao.getNewsItem(newsItems[0].id)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()
    }

    @Test
    fun testDeleteAll() {
        newsDao.insertAll(newsItems).subscribe()
        newsDao.deleteAll().subscribe()

        newsDao.getAll()
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(emptyList())
    }
}