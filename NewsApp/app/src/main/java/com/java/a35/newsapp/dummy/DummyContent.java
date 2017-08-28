package com.java.a35.newsapp.dummy;

import android.os.StrictMode;
import android.util.Log;

import com.java.a35.newsapp.API;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 50;

    private static final API api = new API(API.SERVER_URL);

    static {
        // FIXME
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            JSONObject obj = api.getListNews();
            JSONArray newsList = obj.getJSONArray("list");
            for (int i = 0; i < newsList.length(); ++i) {
                JSONObject news = newsList.getJSONObject(i);
                String newsContent =
                    api.getNews(news.getString("news_ID")).getString("news_Content");
                addItem(new DummyItem(String.valueOf(i + 1), news.getString("news_Title"),
                        "\n" + newsContent));
            }
        } catch (Exception e) {
            Log.d("init", e.toString());
            // Add some sample items.
            for (int i = 1; i <= COUNT; i++) {
                addItem(createDummyItem(i));
            }
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "新闻 " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("这是第").append(position).append("条新闻。");
        for (int i = 0; i < position; i++) {
            builder.append("\n这是新闻内容。");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
