package com.java.a35.newsapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.java.a35.newsapp.api.NewsAPI;

import org.json.JSONObject;

import java.util.*;

public class Categories {

    public static final String PREFERENCES_CATEGORY = "categories";

    public enum CategoryType {
        RECOMMENDED(-1, R.string.category_short_recommended, -1),
        FAVORITE(-1, R.string.category_short_favorited, -1),

        CHINA(NewsAPI.CATEGORY_CHINA, R.string.category_short_china, R.drawable.category_china),
        INTERNATIONAL(NewsAPI.CATEGORY_INTERNATIONAL, R.string.category_short_international, R.drawable.category_international),
        SOCIAL(NewsAPI.CATEGORY_SOCIAL, R.string.category_short_social, R.drawable.category_social),
        CULTURE(NewsAPI.CATEGORY_CULTURE, R.string.category_short_culture, R.drawable.category_culture),
        SPORTS(NewsAPI.CATEGORY_SPORTS, R.string.category_short_sports, R.drawable.category_sports),
        TECHNOLOGY(NewsAPI.CATEGORY_TECHNOLOGY, R.string.category_short_technology, R.drawable.category_technology),
        ENTERTAINMENT(NewsAPI.CATEGORY_ENTERTAINMENT, R.string.category_short_entertainment, R.drawable.category_entertainment),
        EDUCATION(NewsAPI.CATEGORY_EDUCATION, R.string.category_short_education, R.drawable.category_education),
        MILITARY(NewsAPI.CATEGORY_MILITARY, R.string.category_short_military, R.drawable.category_military),
        FINANCIAL(NewsAPI.CATEGORY_FINANCIAL, R.string.category_short_financial, R.drawable.category_financial),
        HEALTH(NewsAPI.CATEGORY_HEALTH, R.string.category_short_health, R.drawable.category_health),
        AUTOMOBILE(NewsAPI.CATEGORY_AUTOMOBILE, R.string.category_short_automobile, R.drawable.category_automobile);

        private final int apiId;
        private final int nameId;
        private final int uiId;
        private CategoryType(int apiId, int nameId, int uiId) {
            this.apiId = apiId;
            this.nameId = nameId;
            this.uiId = uiId;
        }

        public int getApiId() {
            return apiId;
        }

        public int getNameId() {
            return nameId;
        }

        public int getUiId() {
            return uiId;
        }
    }

    public class Category {
        public CategoryType type;
        public boolean enabled = false;

        public List<NewsItem> items = new ArrayList<>();
        public Map<String, NewsItem> map = new HashMap<>();

        public Category(CategoryType type) {
            this.type = type;
        }

        public void clear() {
            items.clear();
            map.clear();
        }

        public void addItem(NewsItem item) {
            items.add(item);
            map.put(item.id, item);
        }

    }

    public Context context;
    public LinkedHashMap<CategoryType, Category> categories;
    public CategoryType[] enabledCategories;

    public Categories(Context context) {
        this.context = context;
        categories = new LinkedHashMap<>();

        addCategory(new Category(CategoryType.RECOMMENDED));
        addCategory(new Category(CategoryType.FAVORITE));

        addCategory(new Category(CategoryType.CHINA));
        addCategory(new Category(CategoryType.INTERNATIONAL));
        addCategory(new Category(CategoryType.SOCIAL));
        addCategory(new Category(CategoryType.CULTURE));
        addCategory(new Category(CategoryType.SPORTS));
        addCategory(new Category(CategoryType.TECHNOLOGY));
        addCategory(new Category(CategoryType.ENTERTAINMENT));
        addCategory(new Category(CategoryType.EDUCATION));
        addCategory(new Category(CategoryType.MILITARY));
        addCategory(new Category(CategoryType.FINANCIAL));
        addCategory(new Category(CategoryType.HEALTH));
        addCategory(new Category(CategoryType.AUTOMOBILE));

        load();
        updateCategories();
    }

    private void addCategory(Category c) {
        categories.put(c.type, c);
    }

    public void updateCategories() {
        int size = 0;
        for (Category c : categories.values()) {
            if (c.enabled) {
                size++;
            }
        }
        enabledCategories = new CategoryType[size];
        int index = 0;
        for (Category c : categories.values()) {
            if (c.enabled) {
                enabledCategories[index] = c.type;
                index++;
            }

        }
    }

    public void load() {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFERENCES_CATEGORY, Context.MODE_PRIVATE);
        for (final Map.Entry<Categories.CategoryType, Categories.Category> entry
                : categories.entrySet()) {
            // TODO(twd2): default value
            entry.getValue().enabled =
                    sharedPreferences.getBoolean(entry.getKey().toString(), true);
        }
    }

    public void save() {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREFERENCES_CATEGORY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (final Map.Entry<Categories.CategoryType, Categories.Category> entry
                : categories.entrySet()) {
            editor.putBoolean(entry.getKey().toString(), entry.getValue().enabled);
        }
        editor.apply();
    }

    public static class NewsItem {
        public final String id;
        public final String title;
        public String detail = "";
        public boolean read = false;
        public boolean favorite = false;
        public final JSONObject obj;
        public boolean special = false;
        public int specialType = 0;

        public NewsItem(String id, String title, boolean read, boolean favorite,
                        JSONObject obj) {
            this.id = id;
            this.title = title;
            this.read = read;
            this.favorite = favorite;
            this.obj = obj;
        }

        // special
        public NewsItem(int specialType) {
            this("", "", false, false, null);
            special = true;
            this.specialType = specialType;
        }

        @Override
        public String toString() {
            return title;
        }
    }

}
