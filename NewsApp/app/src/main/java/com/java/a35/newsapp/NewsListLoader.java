package com.java.a35.newsapp;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by twd2 on 17/8/30.
 */

public class NewsListLoader extends AsyncTaskLoader<JSONObject> {

    interface QueryCallback {
        String getQuery();
        Categories.CategoryType getCategory();
    }
    private QueryCallback queryCallback;

    public NewsListLoader(Context context, QueryCallback callback) {
        super(context);
        queryCallback = callback;
    }

    @Override
    public JSONObject loadInBackground() {
        Log.d("loader", "loading... " + toString());
        API api = ((App) getContext().getApplicationContext()).getApi();

        String query = queryCallback.getQuery();
        Categories.CategoryType categoryType = queryCallback.getCategory();
        int category = categoryType.getApiId();
        try {
            if (query != null && query.length() > 0) {
                if (API.CATEGORY_MIN <= category && category <= API.CATEGORY_MAX) {
                    return api.searchNews(category, query);
                } else {
                    return api.searchAllNews(query);
                }
            } else {
                if (API.CATEGORY_MIN <= category && category <= API.CATEGORY_MAX) {
                    return api.getListNews(category);
                } else if (categoryType == Categories.CategoryType.RECOMMENDED) {
                    // TODO(twd2): list recommended
                    return api.getListNews(category);
                } else if (categoryType == Categories.CategoryType.FAVORITE) {
                    // TODO(twd2): list favorite
                    return api.getListNews(category);
                } else {
                    // ???
                    return null;
                }
            }
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    @Override
    public void deliverResult(JSONObject data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
    }
}
