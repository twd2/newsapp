package com.java.a35.newsapp;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by twd2 on 17/8/30.
 */

public class NewsListLoader extends AsyncTaskLoader<JSONObject> {

    public static class Query {
        public String query = "";
        public int loadedPage = 0;
        public int expectPage = 1;
        public Categories.CategoryType category;

        public Query(String query, int loadedPage, int expectPage,
                     Categories.CategoryType category) {
            this.query = query;
            this.loadedPage = loadedPage;
            this.expectPage = expectPage;
            this.category = category;
        }
    }

    interface QueryCallback {
        Query getQuery();
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
        RecommendAPI recommendAPI = new RecommendAPI(getContext());

        Query query = queryCallback.getQuery();
        Log.d("loader", "loading... " + query);

        if (query == null) {
            return null;
        }

        int category = query.category.getApiId();
        try {
            JSONObject obj = new JSONObject();
            JSONArray list = new JSONArray();
            obj.put("list", list);
            Log.d("loading", query.loadedPage + "/" + query.expectPage);
            for (int page = query.loadedPage + 1; page <= query.expectPage; ++page) {
                JSONObject subObj;
                if (query.query != null && query.query.length() > 0) {
                    if (API.CATEGORY_MIN <= category && category <= API.CATEGORY_MAX) {
                        subObj = api.searchNews(category, query.query, page);
                    } else {
                        // search recommended
                        subObj = api.searchAllNews(query.query, page);
                    }
                } else {
                    if (API.CATEGORY_MIN <= category && category <= API.CATEGORY_MAX) {
                        subObj = api.getListNews(category, page);
                    } else if (query.category == Categories.CategoryType.RECOMMENDED) {
                        // TODO(wuhaozhe): list recommended
                        subObj = recommendAPI.getRecommendNews(page);
                    } else if (query.category == Categories.CategoryType.FAVORITE) {
                        // TODO(twd2): list favorite
                        subObj = api.getListNews(category, page);
                    } else {
                        // ???
                        return null;
                    }
                }
                JSONArray subList = subObj.getJSONArray("list");
                for (int i = 0; i < subList.length(); ++i) {
                    list.put(subList.get(i));
                }
            }
            return obj;
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
