package com.java.a35.newsapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
    }
    private void createCategory()
    {
        LinearLayout categoryLayout = (LinearLayout)findViewById(R.id.categoryLinearLayout);
        for(int i = 0; i < 10; i++)
        {
            Button btn = new Button(this);
            btn.setText(i);
            categoryLayout.addView(btn);
            int idx = categoryLayout.indexOfChild(btn);
            btn.setTag(Integer.toString(idx));
        }
    }
}

class CategoryController
{
    public static String[] categories = {"习近平","胡锦涛","江泽民","邓小平","毛泽东"};
    public static boolean[] categoriesExist = {true, true, true, true, true};
    public static int currentCategory = 0;                      //current category of showing news
    void dump()          //to do, dump current categories to file
    {

    }
    void load()          //to do, load categories from file
    {

    }
}

class CategoryBar
{
    LinearLayout categoryBarLayout;
    AppCompatActivity mActivity;
    CategoryBar(LinearLayout categoryBarLayout, AppCompatActivity inputActivity)
    {
        this.categoryBarLayout = categoryBarLayout;
        this.mActivity = inputActivity;
        for(int i = 0; i < CategoryController.categories.length; i++)
        {
            Button btn = new Button(mActivity);
            btn.setText(CategoryController.categories[i]);
            categoryBarLayout.addView(btn);
            int idx = categoryBarLayout.indexOfChild(btn);
            btn.setTag(Integer.toString(idx));
        }
    }

}