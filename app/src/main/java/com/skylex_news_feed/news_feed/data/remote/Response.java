package com.skylex_news_feed.news_feed.data.remote;

import androidx.annotation.Nullable;

public class Response<T> {

    public enum DetailedState {
        ERROR_WITH_NO_DATA,
        EMPTY_RESPONSE,
        ERROR_WITH_DATA,
        LOADING,
        LOADING_WITH_DATA,
        SUCCESSFUL_LOAD
    }

    private boolean loading;
    private T mData;
    private Throwable mError;
    private DetailedState detailedState;


    private Response(boolean loading, T mData, Throwable mError) {
        this.loading = loading;
        this.mData = mData;
        this.mError = mError;
    }


    public boolean isLoading() {
        return loading;
    }
    public T getData() {
        return mData;
    }
    public Throwable getError() {
        return mError;
    }
    public DetailedState getDetailedState() {
        if (detailedState == null) {
            if (mError != null && mData == null) {
                detailedState = DetailedState.ERROR_WITH_NO_DATA;
            }
            else if (mError != null && mData != null) {
                detailedState = DetailedState.ERROR_WITH_DATA;
            }
            else if(!loading && mData == null) {
                detailedState = DetailedState.EMPTY_RESPONSE;
            }
            else if (loading && mData == null) {
                detailedState = DetailedState.LOADING;
            }
            else if (!loading && mData != null && mError == null) {
                detailedState = DetailedState.SUCCESSFUL_LOAD;
            }
            else if (loading && mData != null) {
                detailedState = DetailedState.LOADING_WITH_DATA;
            }
        }
        return detailedState;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Response) {
            Response response = ((Response) obj);

            boolean errorEqual;
            if (mError != null && response.getError() != null) {
                errorEqual = mError.getMessage().equals(response.getError().getMessage());
            } else if (mError == null && response.getError() == null) {
                errorEqual = true;
            } else {
                errorEqual = false;
            }

            if (this.getData().equals(response.getData())
                && errorEqual
                && this.loading == response.loading)
            {
                return true;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public static class Builder<T> {


        private boolean loading;
        private T mData;
        private Throwable mError;


        public Builder() {

        }


        private void isLoading(boolean loading) {
            this.loading = loading;
        }
        private void setData(T mData) {
            this.mData = mData;
        }
        private void setError(Throwable mError) {
            this.mError = mError;
        }


        public Response loading(T data) {
            setData(data);
            isLoading(true);
            setError(null);
            return build();
        }
        public Response success(T data) {
            setData(data);
            setError(null);
            isLoading(false);
            return build();
        }
        public Response error(Throwable error, T data) {
            setData(data);
            setError(error);
            isLoading(false);
            return build();
        }


        private Response build() {
            return new Response<>(loading, mData, mError);
        }


    }
}
