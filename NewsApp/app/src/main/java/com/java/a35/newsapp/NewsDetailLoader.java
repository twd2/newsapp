package com.java.a35.newsapp;

import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.java.a35.newsapp.api.NewsAPI;
import com.java.a35.newsapp.api.PictureAPI;
import com.java.a35.newsapp.storage.StorageDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by twd2 on 17/9/3.
 */

public class NewsDetailLoader extends AsyncTaskLoader<JSONObject> {

    private Map<String, String> headers;

    interface QueryCallback {
        String getId();
    }
    private QueryCallback queryCallback;

    public NewsDetailLoader(Context context, QueryCallback callback) {
        super(context);
        queryCallback = callback;
        headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
    }

    @Override
    public JSONObject loadInBackground() {
        App app = (App) getContext().getApplicationContext();
        CachedLoader cachedLoader = app.getCachedLoader();
        NewsAPI newsApi = app.getNewsApi();
        PictureAPI pictureAPI = app.getPictureApi();
        StorageDbHelper db = app.getDb();

        String id = queryCallback.getId();
        try {
            JSONObject obj = newsApi.getNews(id);
            db.setHistory(obj);
            boolean showPictures = (PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getBoolean("show_pictures", true)); // TODO(twd2): default value?
            if (!showPictures) return obj;
            pictureAPI.checkAndAddImage(obj);
            if (!obj.getString("news_Pictures").equals("")) {
                JSONArray pictures_path = new JSONArray();
                String pictures[] = obj.getString("news_Pictures").replace(' ', ';').split(";");
                for (String picture : pictures) {
                    try {
                        // combine
                        URL url = new URL(new URL(obj.getString("news_URL")), picture);
                        Log.d("loader", "downloading " + url);
                        pictures_path.put(cachedLoader.fetch(url.toString(), "", headers, false));
                    } catch (IOException e) {
                        Log.d("loader", "picture load failed");
                        e.printStackTrace();
                    }
                }
                obj.put("pictures_path", pictures_path);
            } else {
                obj.put("pictures_path", new JSONArray());
            }
            Log.d("loader", "pictures_path" + obj.get("pictures_path").toString());
            return obj;
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
