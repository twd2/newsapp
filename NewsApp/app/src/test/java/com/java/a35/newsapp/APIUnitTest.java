package com.java.a35.newsapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by twd2 on 17/8/27.
 */

public class APIUnitTest {

    private API api;

    public APIUnitTest() {
        api = new API(API.SERVER_URL);
    }

    @Test
    public void getListTest() throws Exception {
        JSONObject obj = api.getListNews();
        assertTrue(obj.has("list"));
        assertTrue(obj.get("list") instanceof JSONArray);
        assertTrue(obj.has("pageNo"));
        assertTrue(obj.getInt("pageNo") == 1);
        assertTrue(obj.has("pageSize"));
        assertTrue(obj.getInt("pageSize") == API.DEFAULT_PAGE_SIZE);
    }
}
