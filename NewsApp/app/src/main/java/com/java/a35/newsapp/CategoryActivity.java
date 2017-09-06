package com.java.a35.newsapp;

import android.content.Intent;
import android.os.Bundle;
import java.util.*;

import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.content.res.Configuration;
import android.content.Context;



/**
 * Created by wuhaozhe on 2017/9/2.
 */

public class CategoryActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        TableLayout categoryTableLayout = (TableLayout) findViewById(R.id.categoryTableLayout);
        int childCount = categoryTableLayout.getChildCount();
        int rowChildCount = ((TableRow)categoryTableLayout.getChildAt(0)).getChildCount();


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.widthPixels;
        int width = metrics.heightPixels;
        if(height > width)   //swap
        {
            int tmp = height;
            height = width;
            width = tmp;
        }
        height = height * 2 / childCount;
        width /= rowChildCount;

        int counter = 0;
        Categories mcategories = ((App)getApplicationContext()).getCategories();
        for (final Map.Entry<Categories.CategoryType, Categories.Category> entry
                : mcategories.categories.entrySet()) {
            if (entry.getKey().getApiId() < 0) {
                continue;
            }

            RelativeLayout categoryItem = (RelativeLayout)((TableRow)categoryTableLayout
                    .getChildAt(counter / rowChildCount))
                    .getChildAt(counter % rowChildCount);
            LayoutParams params = categoryItem.getLayoutParams();
            params.height = height;
            params.width = width;
            categoryItem.setLayoutParams(params);

            // get the button and set its color
            final Button button = (Button)categoryItem.getChildAt(1);
            final ImageView flag = (ImageView)categoryItem.getChildAt(2);
            button.setText(entry.getKey().getName());

            // TODO (wuhaozhe): add shadow to button
//            button.setTranslationZ(10);
//            button.setElevation(10);

            if (entry.getValue().enabled) {
                button.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                        R.color.categoryEnabledColor, getTheme()));
                button.setTextColor(ResourcesCompat.getColor(getResources(),
                        R.color.categoryEnabledTextColor, getTheme()));
                flag.setVisibility(View.VISIBLE);
            } else {
                button.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                        R.color.categoryDisabledColor, getTheme()));
                button.setTextColor(ResourcesCompat.getColor(getResources(),
                        R.color.categoryDisabledTextColor, getTheme()));
                flag.setVisibility(View.INVISIBLE);
            }

            // set the click listener of button
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    entry.getValue().enabled = !entry.getValue().enabled;
                    entry.getValue().save();
                    if (entry.getValue().enabled) {
                        button.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                                R.color.categoryEnabledColor, getTheme()));
                        button.setTextColor(ResourcesCompat.getColor(getResources(),
                                R.color.categoryEnabledTextColor, getTheme()));
                        flag.setVisibility(View.VISIBLE);
                    } else {
                        button.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                                R.color.categoryDisabledColor, getTheme()));
                        button.setTextColor(ResourcesCompat.getColor(getResources(),
                                R.color.categoryDisabledTextColor, getTheme()));
                        flag.setVisibility(View.INVISIBLE);
                    }
                }
            });

            // set the image of category
            ImageView image = (ImageView)categoryItem.getChildAt(0);
            image.setImageResource(entry.getKey().getUiId());
            counter++;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Categories mcategories = ((App)getApplicationContext()).getCategories();
                mcategories.updateCategories();
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.app_bar_exit) {
            Categories mcategories = ((App)getApplicationContext()).getCategories();
            mcategories.updateCategories();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

}
