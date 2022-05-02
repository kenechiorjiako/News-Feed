package com.skylex_news_feed.news_feed.data.remote

/**
 *  Represents a network/local call response
 */
data class Response<T>(val loading: Boolean, val data: T?, val error: Throwable?) {

    enum class DetailedState {
        ERROR_WITH_NO_DATA, EMPTY_RESPONSE, ERROR_WITH_DATA, LOADING, LOADING_WITH_DATA, SUCCESSFUL_LOAD
    }

    val detailedState: DetailedState?
        get() {
            return if (error != null && data == null) {
                DetailedState.ERROR_WITH_NO_DATA
            } else if (error != null && data != null) {
                DetailedState.ERROR_WITH_DATA
            } else if (!loading && data == null) {
                DetailedState.EMPTY_RESPONSE
            } else if (loading && data == null) {
                DetailedState.LOADING
            } else if (!loading && data != null && error == null) {
                DetailedState.SUCCESSFUL_LOAD
            } else if (loading && data != null) {
                DetailedState.LOADING_WITH_DATA
            } else {
                null
            }
        }

    companion object {
        fun <T> LOADING(data: T?) : Response<T> {
            return Response(true, data, null)
        }
        fun <T> SUCCESS(data: T?) : Response<T> {
            return Response(false, data, null)
        }
        fun <T> ERROR(error: Throwable?, data: T?) : Response<T> {
            return Response(false, data, error)
        }
    }
}