package com.java.a35.newsapp;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

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
        CachedLoader cachedLoader = ((App) getContext().getApplicationContext()).getCachedLoader();
        String id = queryCallback.getId();
        try {
            JSONObject object = api.getNews(id);
            if (!((String)object.get("news_Pictures")).equals("")){
                JSONArray pictures_path = new JSONArray();
                String pictures[] = ((String) object.get("news_Pictures")).split(";");
                for (String picture: pictures){
                    pictures_path.put(cachedLoader.fetch(picture, "", new HashMap<String, String>(), false));
                }
                object.put("pictures_path", pictures_path);
            }
            Log.i("test", object.get("pictures_path").toString());
            return object;
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
