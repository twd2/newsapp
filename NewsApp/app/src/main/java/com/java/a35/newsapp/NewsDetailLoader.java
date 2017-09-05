package com.java.a35.newsapp;

import android.graphics.Picture;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by twd2 on 17/9/3.
 */

public class NewsDetailLoader extends AsyncTaskLoader<JSONObject> {

    interface QueryCallback {
        String getId();
    }
    private QueryCallback queryCallback;

    public NewsDetailLoader(Context context, QueryCallback callback) {
        super(context);
        queryCallback = callback;
    }

    @Override
    public JSONObject loadInBackground() {
        API api = ((App) getContext().getApplicationContext()).getApi();
        String id = queryCallback.getId();
        PictureAPI pictureAPI = new PictureAPI();
        try {
            pictureAPI.getImageJson(api.getNews(id));
            return api.getNews(id);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
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
