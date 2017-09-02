package com.java.a35.newsapp;

import android.support.annotation.BoolRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;
import java.util.*;
import java.util.Iterator;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
    }
}

class CategoryController
{
    public static HashMap<String, Boolean> categories;            // = {"推荐","科技","教育","军事","国内","社会","文化","汽车","国际","体育","财经","健康","娱乐","收藏"};          //分类的全集，value为该分类是否在添加的列表内
    public static String[] addedCategories;
    static
    {
        categories = new HashMap<String, Boolean>();
        categories.put("推荐", false);
        categories.put("科技", true);
        categories.put("教育", true);
        categories.put("军事", true);
        categories.put("国内", true);
        categories.put("社会", true);
        categories.put("文化", true);
        categories.put("汽车", true);
        categories.put("国际", true);
        categories.put("体育", true);
        categories.put("财经", true);
        categories.put("健康", true);
        categories.put("娱乐", true);
        categories.put("收藏", true);
        refreshAddedCategories();
    }
    static void refreshAddedCategories()
    {
        int size = 0;
        for(Boolean value: categories.values())
        {
            if(value) {
                size++;
            }
        }
        addedCategories = new String[size];
        int index = 0;
        for(String value: categories.keySet())
        {
            if(categories.get(value))
            {
                addedCategories[index] = value;
                index++;
            }
        }
    }
}
