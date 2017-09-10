package com.java.a35.newsapp;

import android.util.Log;

import org.json.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by twd2 on 17/8/27.
 */

public class NewsAPI {

    public static final int CATEGORY_TECHNOLOGY = 1;
    public static final int CATEGORY_EDUCATION = 2;
    public static final int CATEGORY_MILITARY = 3;
    public static final int CATEGORY_CHINA = 4;
    public static final int CATEGORY_SOCIAL = 5;
    public static final int CATEGORY_CULTURE = 6;
    public static final int CATEGORY_AUTOMOBILE = 7;
    public static final int CATEGORY_INTERNATIONAL = 8;
    public static final int CATEGORY_SPORTS = 9;
    public static final int CATEGORY_FINANCIAL = 10;
    public static final int CATEGORY_HEALTH = 11;
    public static final int CATEGORY_ENTERTAINMENT = 12;
    public static final int CATEGORY_MIN = 1;
    public static final int CATEGORY_MAX = 12;

    public static final String SERVER_URL = "http://166.111.68.66:2042";
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final String server;
    private CachedLoader cachedLoader;
    private Map<String, String> headers;

    public NewsAPI(String server, CachedLoader cachedLoader) {
        this.server = server;
        this.cachedLoader = cachedLoader;
        headers = new HashMap<>();
        headers.put("User-Agent", "NewsApp/0.0");
    }

    private JSONObject get(String action, String queryString) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(server);
        sb.append(action);
        if (queryString != null && queryString.length() > 0) {
            sb.append("?");
            sb.append(queryString);
        }
        URL url = new URL(sb.toString());
        URLConnection conn = url.openConnection();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        String jsonString = Utils.readAllString(conn.getInputStream());
        Log.d("NewsAPI", jsonString);
        return new JSONObject(jsonString);
    }

    private JSONObject cachedGet(String action, String queryString)
            throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(server);
        sb.append(action);
        if (queryString != null && queryString.length() > 0) {
            sb.append("?");
            sb.append(queryString);
        }
        String jsonString = cachedLoader.fetch(sb.toString(), "", headers, true);
        Log.d("NewsAPI", "from cache: " + jsonString);
        return new JSONObject(jsonString);
    }

    public JSONObject getListNews(int category, int page, int pageSize)
            throws IOException, JSONException {
        String queryString = String.format("category=%d&pageNo=%d&pageSize=%d",
                                           category, page, pageSize);
        return get("/news/action/query/latest", queryString);
    }

    public JSONObject getListNews(int category, int page) throws IOException, JSONException {
        return getListNews(category, page, DEFAULT_PAGE_SIZE);
    }

    public JSONObject getListNews(int category) throws IOException, JSONException {
        return getListNews(category, 1, DEFAULT_PAGE_SIZE);
    }

    public JSONObject getNews(String newsId) throws IOException, JSONException {
        String queryString = String.format("newsId=%s", newsId);
        if (cachedLoader == null) {
            return get("/news/action/query/detail", queryString);
        } else {
            return cachedGet("/news/action/query/detail", queryString);
        }
    }

    public JSONObject searchNews(int category, String query, int page, int pageSize)
            throws IOException, JSONException {
        String queryString = String.format("keyword=%s&category=%d&pageNo=%d&pageSize=%d",
                                           URLEncoder.encode(query, "UTF-8"),
                                           category, page, pageSize);
        return get("/news/action/query/search", queryString);
    }

    public JSONObject searchNews(int category, String query, int page)
            throws IOException, JSONException {
        return searchNews(category, query, page, DEFAULT_PAGE_SIZE);
    }

    public JSONObject searchNews(int category, String query)
            throws IOException, JSONException {
        return searchNews(category, query, 1, DEFAULT_PAGE_SIZE);
    }

    public JSONObject searchAllNews(String query, int page, int pageSize)
            throws IOException, JSONException {
        String queryString = String.format("keyword=%s&pageNo=%d&pageSize=%d",
                                           URLEncoder.encode(query, "UTF-8"),
                                           page, pageSize);
        return get("/news/action/query/search", queryString);
    }

    public JSONObject searchAllNews(String query, int page)
            throws IOException, JSONException {
        return searchAllNews(query, page, DEFAULT_PAGE_SIZE);
    }

    public JSONObject searchAllNews(String query)
            throws IOException, JSONException {
        return searchAllNews(query, 1, DEFAULT_PAGE_SIZE);
    }
}
