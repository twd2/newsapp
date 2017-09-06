package com.java.a35.newsapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by twd2 on 17/8/27.
 */

@RunWith(AndroidJUnit4.class)
public class APIUnitTest {

    private API api;

    public APIUnitTest() {
        api = new API(API.SERVER_URL);
    }

    @Test
    public void getListTest() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        JSONObject obj = api.getListNews(2);
        assertTrue(obj.has("list"));
        assertTrue(obj.get("list") instanceof JSONArray);
        assertTrue(obj.has("pageNo"));
        assertTrue(obj.getInt("pageNo") == 1);
        assertTrue(obj.has("pageSize"));
        assertTrue(obj.getInt("pageSize") == API.DEFAULT_PAGE_SIZE);
    }

    @Test
    public void cachedLoaderTest() throws Exception{
        Context appContext = InstrumentationRegistry.getTargetContext();
        CachedLoader cachedLoader = new CachedLoader(appContext);
        String json = cachedLoader.fetch("http://166.111.68.66:2042/news/action/query/latest",
                "?pageNo=20&pageSize=20", new HashMap<String, String>(), true);
        Log.i("test", json);
    }
}
