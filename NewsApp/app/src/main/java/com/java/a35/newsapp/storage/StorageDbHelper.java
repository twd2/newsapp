package com.java.a35.newsapp.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by twd2 on 17/9/7.
 */

public class StorageDbHelper extends SQLiteOpenHelper {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private static final String SQL_CREATE_FAVORITE =
            "CREATE TABLE IF NOT EXISTS " + FavoriteData.FavoriteEntry.TABLE_NAME + " (" +
                    FavoriteData.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID + " TEXT UNIQUE," +
                    FavoriteData.FavoriteEntry.COLUMN_NAME_DATA + " TEXT)";

    private static final String SQL_DELETE_FAVORITE =
            "DROP TABLE IF EXISTS " + FavoriteData.FavoriteEntry.TABLE_NAME;

    private static final String SQL_CREATE_FAVORITE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID +
                    " ON " + FavoriteData.FavoriteEntry.TABLE_NAME +
                    " (" + FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID + " ASC)";

    private static final String SQL_CREATE_HISTORY =
            "CREATE TABLE IF NOT EXISTS " + HistoryData.HistoryEntry.TABLE_NAME + " (" +
                    HistoryData.HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID + " TEXT UNIQUE," +
                    HistoryData.HistoryEntry.COLUMN_NAME_DATA + " TEXT)";

    private static final String SQL_DELETE_HISTORY =
            "DROP TABLE IF EXISTS " + HistoryData.HistoryEntry.TABLE_NAME;

    private static final String SQL_CREATE_HISTORY_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID +
                    " ON " + HistoryData.HistoryEntry.TABLE_NAME +
                    " (" + HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID + " ASC)";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "storage.db";

    public StorageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FAVORITE);
        db.execSQL(SQL_CREATE_FAVORITE_INDEX);

        db.execSQL(SQL_CREATE_HISTORY);
        db.execSQL(SQL_CREATE_HISTORY_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO(twd2)
    }

    public void setFavorite(JSONObject news, boolean value) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            String newsId = news.getString("news_ID");
            if (value) {
                ContentValues values = new ContentValues();
                values.put(FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID, newsId);
                values.put(FavoriteData.FavoriteEntry.COLUMN_NAME_DATA, news.toString());
                db.insertWithOnConflict(FavoriteData.FavoriteEntry.TABLE_NAME,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } else {
                String[] args = { newsId };
                db.delete(FavoriteData.FavoriteEntry.TABLE_NAME,
                          FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID + " = ?",
                          args);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setHistory(JSONObject news) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            String newsId = news.getString("news_ID");
            ContentValues values = new ContentValues();
            values.put(HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID, newsId);
            values.put(HistoryData.HistoryEntry.COLUMN_NAME_DATA, news.toString());
            db.insertWithOnConflict(HistoryData.HistoryEntry.TABLE_NAME,
                    null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getHistory(String newsId) {
        final String[] cols = {
                HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID,
                HistoryData.HistoryEntry.COLUMN_NAME_DATA
        };

        SQLiteDatabase db = getWritableDatabase();

        String[] args = { newsId };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    HistoryData.HistoryEntry.TABLE_NAME,
                    cols,
                    HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID + " = ?",
                    args,
                    null, // no GROUP BY
                    null, // no HAVING
                    null, // no ORDER BY
                    null // no LIMIT
            );

            if (cursor.moveToFirst()) {
                String jsonString = cursor.getString(1);
                return new JSONObject(jsonString);
            } else {
                return null;
            }
        } catch (JSONException e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public JSONObject getFavorite(String newsId) {
        final String[] cols = {
                FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID,
                FavoriteData.FavoriteEntry.COLUMN_NAME_DATA
        };

        SQLiteDatabase db = getWritableDatabase();

        String[] args = { newsId };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    FavoriteData.FavoriteEntry.TABLE_NAME,
                    cols,
                    FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID + " = ?",
                    args,
                    null, // no GROUP BY
                    null, // no HAVING
                    null, // no ORDER BY
                    null // no LIMIT
            );

            if (cursor.moveToFirst()) {
                String jsonString = cursor.getString(1);
                return new JSONObject(jsonString);
            } else {
                return null;
            }
        } catch (JSONException e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public JSONObject getListFavorite(int page, int pageSize) {
        final String[] cols = {
                FavoriteData.FavoriteEntry.COLUMN_NAME_NEWS_ID,
                FavoriteData.FavoriteEntry.COLUMN_NAME_DATA
        };

        SQLiteDatabase db = getWritableDatabase();

        String[] args = { };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    FavoriteData.FavoriteEntry.TABLE_NAME,
                    cols,
                    null, // no WHERE
                    args,
                    null, // no GROUP BY
                    null, // no HAVING
                    FavoriteData.FavoriteEntry._ID + " DESC",
                    String.format("%d, %d", (page - 1) * pageSize, pageSize)
            );

            JSONObject obj = new JSONObject();
            JSONArray list = new JSONArray();
            obj.put("list", list);

            if (cursor.moveToFirst()) {
                do {
                    list.put(new JSONObject(cursor.getString(1)));
                } while (cursor.moveToNext());
            }
            return obj;
        } catch (JSONException e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public JSONObject getListFavorite(int page) {
        return getListFavorite(page, DEFAULT_PAGE_SIZE);
    }

    public JSONObject getListHistory(int page, int pageSize) {
        final String[] cols = {
                HistoryData.HistoryEntry.COLUMN_NAME_NEWS_ID,
                HistoryData.HistoryEntry.COLUMN_NAME_DATA
        };

        SQLiteDatabase db = getWritableDatabase();

        String[] args = { };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    HistoryData.HistoryEntry.TABLE_NAME,
                    cols,
                    null, // no WHERE
                    args,
                    null, // no GROUP BY
                    null, // no HAVING
                    HistoryData.HistoryEntry._ID + " DESC",
                    String.format("%d, %d", (page - 1) * pageSize, pageSize)
            );

            JSONObject obj = new JSONObject();
            JSONArray list = new JSONArray();
            obj.put("list", list);

            if (cursor.moveToFirst()) {
                do {
                    list.put(new JSONObject(cursor.getString(1)));
                } while (cursor.moveToNext());
            }
            return obj;
        } catch (JSONException e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public JSONObject getListHistory(int page) {
        return getListHistory(page, DEFAULT_PAGE_SIZE);
    }
}