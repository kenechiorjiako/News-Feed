package com.skylex_news_feed.news_feed.data.entities

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * News Entity that can be fetched directly from the network using Retrofit and parsed to Room.
 */
@Entity(tableName = "news")
data class News (
    @PrimaryKey
    val id : String,
    val title : String,
    val description : String,
    val url : String,
    val author : String,
    val image : String,
    val language : String,
    val category : List<String>,
    val published : String
) {

    override fun equals(other: Any?): Boolean {
        return (other is News)
                && id == other.id
                && title == other.title
                && description == other.description
                && url == other.url
                && author == other.author
                && image == other.image
                && language == other.language
                && category == other.category
                && published == other.published
    }

    class DiffCallback(private val oldList: List<News>, private val newList: List<News>): DiffUtil.Callback() {


        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }
}