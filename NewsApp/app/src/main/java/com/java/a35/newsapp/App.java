package com.java.a35.newsapp;

import android.app.Application;

import com.java.a35.newsapp.storage.StorageDbHelper;

/**
 * Created by twd2 on 17/8/30.
 */

public class App extends Application {

    private API api = null;
    private PictureAPI pictureApi = null;
    private CachedLoader cachedLoader = null;
    private Categories categories = null;
    private StorageDbHelper db = null;

    @Override
    public void onCreate() {
        super.onCreate();
        cachedLoader = new CachedLoader(this);
        categories = new Categories(this);
        api = new API(API.SERVER_URL, cachedLoader);
        pictureApi = new PictureAPI(cachedLoader);
        db = new StorageDbHelper(this);
    }

    public API getApi() {
        return api;
    }

    public CachedLoader getCachedLoader() {
        return cachedLoader;
    }

    public Categories getCategories() {
        return categories;
    }

    public PictureAPI getPictureApi() {
        return pictureApi;
    }

    public StorageDbHelper getDb() {
        return db;
    }
}
