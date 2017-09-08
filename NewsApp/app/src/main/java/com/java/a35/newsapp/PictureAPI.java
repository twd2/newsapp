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
 * Created by wuhaozhe on 2017/9/5.
 */

public class PictureAPI {
    public static final String IMAGE_SERVER_URL = "https://api.cognitive.microsoft.com";
    public static final String IMAGE_SIZE = "Medium";   //there are five types of page sizes, Small, Medium, Large, Wallpaper and all
    public static final int IMAGE_NUM = 3;               //for each of keyword, the number of picture grabbed is IMAGE_NUM

    public static final String SEARCH_PLACE = "zh-CN";     //the location of my client(in which country)
    public static final String SEARCH_KEY = "3f6dbf8a32c842cb996577084b329068";           //the key of using bing's api
    public static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 822)";

    private CachedLoader cachedLoader;
    private Map<String, String> headers;

    public PictureAPI(CachedLoader cachedLoader) {
        this.cachedLoader = cachedLoader;
        headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        headers.put("Ocp-Apim-Subscription-Key", SEARCH_KEY);
    }

    private JSONObject get(String action, String queryString) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(IMAGE_SERVER_URL);
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
        String jsonString = Utility.readAllString(conn.getInputStream());
        Log.d("PictureAPI", jsonString);
        return new JSONObject(jsonString);
    }

    private JSONObject cachedGet(String action, String queryString)
            throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(IMAGE_SERVER_URL);
        sb.append(action);
        if (queryString != null && queryString.length() > 0) {
            sb.append("?");
            sb.append(queryString);
        }
        String jsonString = cachedLoader.fetch(sb.toString(), "", headers, true);
        Log.d("PictureAPI", "from cache: " + jsonString);
        return new JSONObject(jsonString);
    }

    public JSONArray getImages(String queryKeyword) throws IOException, JSONException {
        String queryString = String.format("q=%s&mkt=%s&count=%d",
                URLEncoder.encode(queryKeyword, "UTF-8"),
                SEARCH_PLACE, IMAGE_NUM);
        JSONObject src = cachedGet("/bing/v7.0/images/search", queryString);
        JSONArray imagesArray = src.getJSONArray("value");
        return imagesArray;
    }

    public JSONArray getImageJson(JSONObject inputNews) throws IOException, JSONException {
        String query = inputNews.getString("news_Title");
        JSONArray images = new JSONArray();
        JSONArray src = getImages(query);
        if (src.length() == 0) {
            query = inputNews.getJSONArray("Keywords").getJSONObject(0).getString("word");
            src = getImages(query);
        }
        for (int i = 0; i < src.length(); i++) {
            JSONObject dst = new JSONObject();
            dst.put("url", src.getJSONObject(i).getString("contentUrl"));
            dst.put("encodingFormat", src.getJSONObject(i).getString("encodingFormat"));
            images.put(dst);
        }
        return images;
    }

    public void checkAndAddImage(JSONObject inputNews) throws IOException, JSONException {
        if (!inputNews.getString("news_Pictures").equals("")) {
            inputNews.put("searchImage", false);
        } else {
            JSONArray imagePaths = getImageJson(inputNews);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < imagePaths.length(); i++) {
                sb.append(imagePaths.getJSONObject(i).getString("url"));
                sb.append(";");
            }
            inputNews.put("news_Pictures", sb.toString());
            inputNews.put("searchImage", true);
        }
    }
}
