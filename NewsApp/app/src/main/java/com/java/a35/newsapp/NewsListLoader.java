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
        int getCategory();
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
        int category = queryCallback.getCategory();
        try {
            if (query != null && query.length() > 0) {
                // TODO: search category?
                return api.searchAllNews(query);
            } else {
                return api.getListNews(category);
            }
        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
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
