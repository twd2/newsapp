package com.java.a35.newsapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO
public class DummyContent {

    public static final List<NewsItem> NEWS = new ArrayList<NewsItem>();

    public static final Map<String, NewsItem> NEWS_MAP = new HashMap<String, NewsItem>();

    public static void clear() {
        NEWS.clear();
        NEWS_MAP.clear();
    }

    public static void addItem(NewsItem item) {
        NEWS.add(item);
        NEWS_MAP.put(item.id, item);
    }

    public static class NewsItem {
        public final String id;
        public final String content;
        public final String details;

        public NewsItem(String id, String content, String details) {
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
