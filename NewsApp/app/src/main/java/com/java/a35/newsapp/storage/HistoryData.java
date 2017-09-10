package com.java.a35.newsapp.storage;

import android.provider.BaseColumns;

/**
 * Created by twd2 on 17/9/7.
 */

public final class HistoryData {
    private HistoryData() { }

    public static class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_NEWS_ID = "newsId";
        public static final String COLUMN_NAME_DATA = "data";
    }
}
