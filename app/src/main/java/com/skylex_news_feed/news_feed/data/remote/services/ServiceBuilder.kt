package com.skylex_news_feed.news_feed.data.remote.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Service builder for the application
 */
object ServiceBuilder {

    private const val BASE_URL = "http://api.currentsapi.services/v1/"

    private val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val okHttpClient = OkHttpClient().newBuilder().apply {
        connectTimeout(30, TimeUnit.SECONDS)
        addInterceptor(loggingInterceptor)
        readTimeout(80, TimeUnit.SECONDS)
        writeTimeout(80, TimeUnit.SECONDS)
    }.build()

    private val builder : Retrofit.Builder = Retrofit.Builder().apply {
        baseUrl(BASE_URL)
        client(okHttpClient)
        addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        addConverterFactory(GsonConverterFactory.create(getGson()))
    }

    private val retrofit : Retrofit = builder.build()


    fun <T> buildService(serviceType : Class<T>) : T {
        return retrofit.create(serviceType)
    }


    private fun getGson() : Gson = GsonBuilder().create()
}