package com.skylex_news_feed.news_feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.skylex_news_feed.news_feed.databinding.ActivityWebViewBinding


class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var pageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.WebViewActivityTheme)

        binding = ActivityWebViewBinding.inflate(LayoutInflater.from(this))

        pageUrl = intent.getStringExtra(URL_KEY)


        setupViewListeners()
        setupViews()
        setupPage()

        setContentView(binding.root)
    }

    private fun setupPage() {
        binding.apply {
            webview.settings.loadsImagesAutomatically = true;
            webview.settings.javaScriptEnabled = true;
            webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY;
            webview.loadUrl(pageUrl);
        }
    }

    private fun setupViews() {
        binding.apply {
            pageTitle.text = pageUrl
            webview.webViewClient = MyBrowser(object : MyBrowser.LoadListener {
                override fun loadingNewPage(url: String) {
                    pageTitle.text = url
                }

            })

            webview.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if(newProgress < 100 && progressBar.visibility == ProgressBar.GONE){
                        progressBar.visibility = ProgressBar.VISIBLE;
                    }

                    progressBar.progress = newProgress;
                    if(newProgress == 100) {
                        progressBar.visibility = ProgressBar.GONE;
                    }
                }
            }
        }
    }

    private fun setupViewListeners() {
        binding.apply {
            closeButton.setOnClickListener {
                finish()
            }
        }
    }

    private class MyBrowser(private var loadListener: LoadListener? = null) : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            loadListener?.loadingNewPage(url)
            view.loadUrl(url)
            return true
        }



        interface LoadListener {
            fun loadingNewPage(url: String)
        }
    }

    companion object {
        const val URL_KEY = "URL_KEY"
    }

}