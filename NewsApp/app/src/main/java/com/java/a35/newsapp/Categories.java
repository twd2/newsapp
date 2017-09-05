package com.java.a35.newsapp;

import java.util.*;

public class Categories {
    public enum CategoryType {
        RECOMMENDED(-1, "推荐", -1),
        FAVORITE(-1, "收藏", -1),
        CHINA(API.CATEGORY_CHINA, "国内", R.drawable.category_china),
        INTERNATIONAL(API.CATEGORY_INTERNATIONAL, "国际", R.drawable.category_international),
        SOCIAL(API.CATEGORY_SOCIAL, "社会", R.drawable.category_social),
        CULTURE(API.CATEGORY_CULTURE, "文化", R.drawable.category_culture),
        SPORTS(API.CATEGORY_SPORTS, "体育", R.drawable.category_sports),
        TECHNOLOGY(API.CATEGORY_TECHNOLOGY, "科技", R.drawable.category_technology),
        ENTERTAINMENT(API.CATEGORY_ENTERTAINMENT, "娱乐", R.drawable.category_entertainment),
        EDUCATION(API.CATEGORY_EDUCATION, "教育", R.drawable.category_education),
        MILITARY(API.CATEGORY_MILITARY, "军事", R.drawable.category_military),
        FINANCIAL(API.CATEGORY_FINANCIAL, "财经", R.drawable.category_financial),
        HEALTH(API.CATEGORY_HEALTH, "健康", R.drawable.category_health),
        AUTOMOBILE(API.CATEGORY_AUTOMOBILE, "汽车", R.drawable.category_automobile);

        private final int apiId;
        private final String name;
        private final int uiId;
        private CategoryType(int apiId, String name, int uiId) {
            this.apiId = apiId;
            this.name = name;
            this.uiId = uiId;
        }

        public int getApiId() {
            return apiId;
        }

        public String getName() {
            return name;
        }

        public int getUiId() {
            return uiId;
        }
    }

    public static class Category {
        public CategoryType type;
        public boolean enabled;

        public Category(CategoryType type) {
            this.type = type;
            this.enabled = true; // TODO(twd2): read from preferences
        }
    }

    public static LinkedHashMap<CategoryType, Category> categories;
    public static CategoryType[] enabledCategories;

    static {
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

        updateCategories();
    }

    private static void addCategory(Category c) {
        categories.put(c.type, c);
    }

    public static void updateCategories() {
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
}
