package com.java.a35.newsapp;

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
 * Created by wuhaozhe on 2017/9/5.
 */

public class PictureAPI {
    public static final String IMAGE_SERVER_URL = "https://api.cognitive.microsoft.com";
    public static final String IMAGE_SIZE = "Medium";   //there are five types of page sizes, Small, Medium, Large, Wallpaper and all
    public static final int IMAGE_NUM = 1;               //for each of keyword, the number of picture grabbed is IMAGE_NUM

    public static final String SEARCH_PLACE = "zh-CN";     //the location of my client(in which country)
    public static final String SEARCH_KEY = "3f6dbf8a32c842cb996577084b329068";           //the key of using bing's api
    public static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 822)";
    public static final int KEYWORD_NUM = 3;              //the number of keyword that will be queried

    private JSONObject get(String action, String queryString) throws IOException, JSONException
    {
        StringBuffer sb = new StringBuffer();
        sb.append(IMAGE_SERVER_URL);
        sb.append(action);
        if (queryString != null && queryString.length() > 0) {
            sb.append("?");
            sb.append(queryString);
        }
        URL url = new URL(sb.toString());
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Ocp-Apim-Subscription-Key", SEARCH_KEY);
        String jsonString = Utility.readAllString(conn.getInputStream());
        return new JSONObject(jsonString);
    }

    private String[] getQueries(JSONObject inputNews) throws IOException, JSONException                //参数是输入的新闻，根据新闻得到需要查询的关键字
    {
        JSONArray keywordsArray = (JSONArray) inputNews.get("Keywords");
        int length = Math.min(KEYWORD_NUM, keywordsArray.length());
        String[] keywords = new String[length];
        for(int i = 0; i < length; i++)
        {
            keywords[i] = (keywordsArray.getJSONObject(i)).getString("word");
        }
        return keywords;
    }

    public JSONObject getImages(String queryKeyword) throws IOException, JSONException
    {
        String queryString = String.format("q=%s&mkt=%s&count=%d",
                URLEncoder.encode(queryKeyword, "UTF-8"),
                SEARCH_PLACE, IMAGE_NUM);
        JSONObject src = get("/bing/v7.0/images/search", queryString);
        JSONArray imagesArray = src.getJSONArray("value");
        return imagesArray.getJSONObject(0);
    }

    public JSONArray getImageJson(JSONObject inputNews) throws IOException, JSONException                //参数是输入的新闻，输出是一个JSONArray, 每一项中是一个JSONObject
    {
        String[] queries = getQueries(inputNews);
        JSONArray images = new JSONArray();
        for(int i = 0; i < queries.length; i++)
        {
            JSONObject src = getImages(queries[i]);
            JSONObject dst = new JSONObject();
            dst.put("url", src.getString("contentUrl"));
            dst.put("encodingFormat", src.getString("encodingFormat"));
            dst.put("keyword", queries[i]);
            images.put(dst);
        }
        Log.d("ImageAPI", images.toString());
        return images;
    }

    public void addImage(JSONObject inputNews) throws  IOException, JSONException            //if no image in the news, add some images
    {
        if(!inputNews.getString("news_Pictures").equals(""))
        {
            inputNews.put("searchImage", false);
            return;
        }
        else             //no picture in the news
        {
            JSONArray imagePaths =getImageJson(inputNews);
            String paths = "";
            for(int i = 0; i < imagePaths.length(); i++)
            {
                paths += imagePaths.getJSONObject(i).getString("url");
                paths += ";";
            }
            inputNews.put("news_Pictures", paths);
            inputNews.put("searchImage", true);
        }
    }
}
