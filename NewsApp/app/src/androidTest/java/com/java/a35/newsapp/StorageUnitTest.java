package com.java.a35.newsapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.java.a35.newsapp.storage.StorageDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by twd2 on 17/8/27.
 */

@RunWith(AndroidJUnit4.class)
public class StorageUnitTest {

    private StorageDbHelper db;
    JSONObject dummy, dummy2;

    public StorageUnitTest() throws JSONException {
        super();

        Context appContext = InstrumentationRegistry.getTargetContext();
        db = new StorageDbHelper(appContext);

        dummy = new JSONObject(
                "{\"news_ID\": \"11" + System.currentTimeMillis() + "\", \"dummy\": \"hello\"}");
        dummy2 = new JSONObject(
                "{\"news_ID\": \"22" + System.currentTimeMillis() + "\", \"dummy2\": \"world\"}");
    }

    @Test
    public void getSetFavoriteTest() throws Exception {
        assertTrue(db.getFavorite(dummy.getString("news_ID")) == null);
        assertTrue(db.getFavorite(dummy2.getString("news_ID")) == null);
        db.setFavorite(dummy, true);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) != null);
        assertTrue(db.getFavorite(dummy2.getString("news_ID")) == null);
        db.setFavorite(dummy2, true);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) != null);
        assertTrue(db.getFavorite(dummy2.getString("news_ID")) != null);

        db.setFavorite(dummy, false);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) == null);
        db.setFavorite(dummy, true);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) != null);
        db.setFavorite(dummy, false);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) == null);
        db.setFavorite(dummy, true);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) != null);
        db.setFavorite(dummy, true);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) != null);
        db.setFavorite(dummy, false);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) == null);
        db.setFavorite(dummy, false);
        assertTrue(db.getFavorite(dummy.getString("news_ID")) == null);
    }

    @Test
    public void getListFavoriteTest() throws Exception {
        db.setFavorite(dummy, true);
        db.setFavorite(dummy2, true);

        JSONArray list = db.getListFavorite(1).getJSONArray("list");
        assertTrue(list.getJSONObject(0).getString("news_ID").equals(dummy2.getString("news_ID")));
        assertTrue(list.getJSONObject(1).getString("news_ID").equals(dummy.getString("news_ID")));
    }

    @Test
    public void getSetHistoryTest() throws Exception {
        assertTrue(db.getHistory(dummy.getString("news_ID")) == null);
        db.setHistory(dummy);
        assertTrue(db.getHistory(dummy.getString("news_ID")) != null);
        db.setHistory(dummy);
        assertTrue(db.getHistory(dummy.getString("news_ID")) != null);
    }

    @Test
    public void getListHistoryTest() throws Exception {
        db.setHistory(dummy);
        db.setHistory(dummy2);

        JSONArray list = db.getListHistory(1).getJSONArray("list");
        assertTrue(list.getJSONObject(0).getString("news_ID").equals(dummy2.getString("news_ID")));
        assertTrue(list.getJSONObject(1).getString("news_ID").equals(dummy.getString("news_ID")));
    }
}
