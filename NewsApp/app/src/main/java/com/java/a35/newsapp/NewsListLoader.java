package com.java.a35.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by twd2 on 17/8/30.
 */

public class NewsListLoader extends AsyncTaskLoader<JSONObject> {

    interface QueryCallback {
        String getQuery();
    }
    private QueryCallback queryCallback;

    public NewsListLoader(Context context, QueryCallback callback) {
        super(context);
        queryCallback = callback;
    }

    @Override
    public JSONObject loadInBackground() {
        API api = ((App) getContext().getApplicationContext()).getApi();
        String query = queryCallback.getQuery();
        try {
            if (query != null && query.length() > 0) {
                // TODO: search
                return api.getListNews();
            } else {
                return api.getListNews();
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
