package com.skylex_news_feed.news_feed.ui.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skylex_news_feed.news_feed.R
import com.skylex_news_feed.news_feed.data.entities.News
import com.skylex_news_feed.news_feed.databinding.NewsListItemV1Binding
import com.squareup.picasso.Picasso


class NewsAdapter(private val eventHandler: EventHandler? = null) : RecyclerView.Adapter<NewsAdapter.ViewHolder1>()     {

    private var newsItems: MutableList<News> = mutableListOf()
    private var globalImageWidth: Int = 0
    private var globalImageHeight: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder1 {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.news_list_item_v1,
            parent,
            false
        )
        return ViewHolder1(itemView, eventHandler)
    }

    override fun onBindViewHolder(holder: ViewHolder1, position: Int) {
        holder.bind(newsItems[position])
    }

    override fun getItemCount(): Int {
        return newsItems.size
    }

    fun setItems(items: List<News>) {
        val diffCallback = News.DiffCallback(newsItems, items)
        newsItems = mutableListOf()
        newsItems.addAll(items)
        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(this)
    }

    inner class ViewHolder1(itemView: View, private val eventHandler: EventHandler?) : RecyclerView.ViewHolder(
        itemView
    ) {

        private val binding : NewsListItemV1Binding = NewsListItemV1Binding.bind(itemView)
        private var news: News? = null
        private var permitGlobalLoading = false

        init {
            setupViewListeners()
        }

        private fun setupViewListeners() {
            binding.rootView.setOnClickListener {
                if (this.news != null) {
                    eventHandler?.onNewsItemClicked(news!!)
                }
            }
        }

        fun bind(news: News) {
            this.news = news

            binding.apply {
                newsImage.viewTreeObserver
                    .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (globalImageHeight == 0 || globalImageWidth == 0 || permitGlobalLoading) {
                                globalImageWidth = newsImage.measuredWidth
                                globalImageHeight = newsImage.measuredHeight
                                Picasso.get()
                                    .load(news.image)
                                    .placeholder(R.color.color_image_placeholder)
                                    .centerCrop()
                                    .resize(globalImageWidth, globalImageHeight)
                                    .into(newsImage)
                            }

                            permitGlobalLoading = false

                            newsImage.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })

                if (globalImageWidth != 0 && globalImageHeight != 0) {
                    Picasso.get()
                        .load(news.image)
                        .placeholder(R.color.color_image_placeholder)
                        .centerCrop()
                        .resize(globalImageWidth, globalImageHeight)
                        .into(newsImage)
                } else {
                    permitGlobalLoading = true
                }

                newsTitle.text = news.title
                authorName.text = news.author
            }
        }
    }

    /**
     * Event handler for all recycler view interactions.
     */
    interface EventHandler {
        fun onNewsItemClicked(news: News)
    }
}