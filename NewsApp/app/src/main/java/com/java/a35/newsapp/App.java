package com.java.a35.newsapp;

import android.app.Application;

/**
 * Created by twd2 on 17/8/30.
 */

public class App extends Application {

    private static final API api = new API(API.SERVER_URL);
    private static CachedLoader cachedLoader = null;
    private static Categories categories = null;
    public API getApi() {
        return api;
    }
    public CachedLoader getCachedLoader(){
        return cachedLoader;
    }
    public Categories getCategories() {return categories;}

    @Override
    public void onCreate() {
        super.onCreate();
        cachedLoader = new CachedLoader(this.getApplicationContext());
        categories = new Categories(this.getApplicationContext());
    }
    // TODO
}
