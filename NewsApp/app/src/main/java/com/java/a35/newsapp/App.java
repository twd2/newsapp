package com.java.a35.newsapp;

import android.app.Application;
import android.util.Log;

import java.io.File;

import com.java.a35.newsapp.storage.StorageDbHelper;


/**
 * Created by twd2 on 17/8/30.
 */

public class App extends Application {

    private NewsAPI newsApi = null;
    private PictureAPI pictureApi = null;
    private CachedLoader cachedLoader = null;
    private Categories categories = null;
    private StorageDbHelper db = null;
    private File sharedDir = null;

    @Override
    public void onCreate() {
        super.onCreate();
        cachedLoader = new CachedLoader(this);
        categories = new Categories(this);
        newsApi = new NewsAPI(NewsAPI.SERVER_URL, cachedLoader);
        pictureApi = new PictureAPI(cachedLoader);
        db = new StorageDbHelper(this);
        Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e("test", Log.getStackTraceString(e));
            }
        });
        sharedDir = new File(getCacheDir(), "shared");
        if (!sharedDir.exists()) {
            sharedDir.mkdirs();
        }
    }

    public NewsAPI getNewsApi() {
        return newsApi;
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

    public File getSharedDir() {
        return sharedDir;
    }
}
