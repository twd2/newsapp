package com.java.a35.newsapp;

import android.support.annotation.RequiresPermission;
import android.util.JsonReader;

import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by twd2 on 17/8/27.
 */

public class API {
    public static final String SERVER_URL = "http://166.111.68.66:2042";
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final String server;

    public API(String server) {
        this.server = server;
    }

    private String readAllString(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[4096];
        StringBuffer sb = new StringBuffer();
        int count;
        while ((count = reader.read(buffer)) >= 0) {
            sb.append(buffer, 0, count);
        }
        return sb.toString();
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
        String jsonString = readAllString(conn.getInputStream());
        System.out.println(jsonString);
        return new JSONObject(jsonString);
    }

    public JSONObject getListNews(int page, int pageSize) throws IOException, JSONException {
        String queryString = String.format("pageNo=%d&pageSize=%d", page, pageSize);
        return get("/news/action/query/latest", queryString);
    }

    public JSONObject getListNews(int page) throws IOException, JSONException {
        return getListNews(page, DEFAULT_PAGE_SIZE);
    }

    public JSONObject getListNews() throws IOException, JSONException {
        return getListNews(1);
    }

    public JSONObject getNews(String newsId) throws IOException, JSONException {
        String queryString = String.format("newsId=%s", newsId);
        return get("/news/action/query/detail", queryString);
    }
}
