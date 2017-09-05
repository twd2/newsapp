package com.java.a35.newsapp;

import android.support.annotation.RequiresPermission;
import android.util.JsonReader;
import android.util.Log;

import com.java.a35.newsapp.dummy.Utility;

import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


/**
 * Created by twd2 on 17/8/27.
 */

public class API {

    public static final String SERVER_URL = "http://166.111.68.66:2042";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int CATEGORY_COUNT = 12;

    private final String server;

    public API(String server) {
        this.server = server;
    }

    private JSONObject get(String action, String queryString) throws IOException, JSONException {
        StringBuffer sb = new StringBuffer();
        sb.append(server);
        sb.append(action);
        if (queryString != null && queryString.length() > 0) {
            sb.append("?");
            sb.append(queryString);
        }
        URL url = new URL(sb.toString());
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "NewsApp/0.0");
        String jsonString = Utility.readAllString(conn.getInputStream());
        Log.d("API", jsonString);
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
        return get("/news/action/query/detail", queryString);
    }

    public JSONObject searchNews(String query, int category, int page, int pageSize)
            throws IOException, JSONException {
        String queryString = String.format("keyword=%s&category=%d&pageNo=%d&pageSize=%d",
                                           URLEncoder.encode(query, "UTF-8"),
                                           category, page, pageSize);
        return get("/news/action/query/search", queryString);
    }

    public JSONObject searchNews(String query, int category, int page)
            throws IOException, JSONException {
        return searchNews(query, category, page, DEFAULT_PAGE_SIZE);
    }

    public JSONObject searchNews(String query, int category)
            throws IOException, JSONException {
        return searchNews(query, category, 1, DEFAULT_PAGE_SIZE);
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
