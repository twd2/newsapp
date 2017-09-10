package com.java.a35.newsapp;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.java.a35.newsapp.storage.StorageDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
        Log.d("loader", "this = " + toString());
        NewsAPI newsApi = ((App) getContext().getApplicationContext()).getNewsApi();
        RecommendAPI recommendAPI = new RecommendAPI(getContext());
        StorageDbHelper db = ((App) getContext().getApplicationContext()).getDb();

        Query query = queryCallback.getQuery();
        Log.d("loader", "query = " + query);

        if (query == null) {
            Log.d("loader", "query == null, returning");
            return null;
        }

        int category = query.category.getApiId();
        try {
            JSONObject obj = new JSONObject();
            JSONArray list = new JSONArray();
            obj.put("list", list);
            obj.put("noMore", true);
            Set<String> blockSet =
                    getContext().getSharedPreferences(BlockSettingsActivity.PREFERENCES_BLOCK,
                                                      Context.MODE_PRIVATE)
                            .getStringSet("block_list", new HashSet<String>());
            Log.d("loader", "page = " + query.loadedPage + "/" + query.expectPage);
            for (int page = query.loadedPage + 1; page <= query.expectPage; ++page) {
                JSONObject subObj;
                if (query.query != null && query.query.length() > 0) {
                    if (NewsAPI.CATEGORY_MIN <= category && category <= NewsAPI.CATEGORY_MAX) {
                        subObj = newsApi.searchNews(category, query.query, page);
                    } else {
                        // search recommended
                        subObj = newsApi.searchAllNews(query.query, page);
                    }
                } else {
                    if (NewsAPI.CATEGORY_MIN <= category && category <= NewsAPI.CATEGORY_MAX) {
                        subObj = newsApi.getListNews(category, page);
                    } else if (query.category == Categories.CategoryType.RECOMMENDED) {
                        // list recommended
                        subObj = recommendAPI.getRecommendNews(page);
                    } else if (query.category == Categories.CategoryType.FAVORITE) {
                        // list favorite
                        subObj = db.getListFavorite(page);
                    } else {
                        // ???
                        return null;
                    }
                }

                JSONArray subList = subObj.getJSONArray("list");
                for (int i = 0; i < subList.length(); ++i) {
                    obj.put("noMore", false);
                    JSONObject news = subList.getJSONObject(i);
                    // block keywords
                    boolean blocked = false;
                    String newsTitle = news.getString("news_Title").toLowerCase();
                    String newsIntro = news.getString("news_Intro").toLowerCase();
                    for (String keyword : blockSet) {
                        String lowerCaseKeyword = keyword.toLowerCase();
                        if (newsTitle.contains(lowerCaseKeyword) ||
                            newsIntro.contains(lowerCaseKeyword)) {
                            blocked = true;
                            break;
                        }
                    }
                    if (blocked) {
                        continue;
                    }
                    news.put("read", db.getHistory(news.getString("news_ID")) != null);
                    list.put(news);
                }
            }
            return obj;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d("loader", "an exception occurred, returning");
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
