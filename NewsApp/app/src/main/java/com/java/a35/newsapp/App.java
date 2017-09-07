package com.java.a35.newsapp;

import android.app.Application;

/**
 * Created by twd2 on 17/8/30.
 */

public class App extends Application {

    private API api = null;
    private PictureAPI pictureApi = null;
    private CachedLoader cachedLoader = null;
    private Categories categories = null;

    @Override
    public void onCreate() {
        super.onCreate();
        cachedLoader = new CachedLoader(this.getApplicationContext());
        categories = new Categories(this.getApplicationContext());
        api = new API(API.SERVER_URL, cachedLoader);
        pictureApi = new PictureAPI(cachedLoader);
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
}
