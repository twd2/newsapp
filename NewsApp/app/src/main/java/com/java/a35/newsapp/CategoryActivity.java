package com.java.a35.newsapp;

import android.content.Intent;
import android.os.Bundle;
import java.util.*;

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

/**
 * Created by wuhaozhe on 2017/9/2.
 */

public class CategoryActivity extends AppCompatActivity {
    final int selectedColor = 0x44000000;
    final int canceledColor = 0xcc000000;
    final int textSelectedColor = 0xcc000000;
    final int textCanceledColor = 0xddeeeeee;

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
        height = height * 2 / childCount;
        width /= rowChildCount;

        int counter = 0;
        for (final Map.Entry<Categories.CategoryType, Categories.Category> entry
                : Categories.categories.entrySet()) {
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
            final Button categoryButton = (Button)categoryItem.getChildAt(1);
            categoryButton.setText(entry.getKey().getName());

            // TODO (wuhaozhe): add shadow to button
            // categoryButton.setTranslationZ(10);
            // categoryButton.setElevation(10);

            if (entry.getValue().enabled) {
                // TODO(twd2): use R.color.xxx
                categoryButton.setBackgroundColor(selectedColor);
                categoryButton.setTextColor(textSelectedColor);
            } else {
                categoryButton.setBackgroundColor(canceledColor);
                categoryButton.setTextColor(textCanceledColor);
            }

            // set the click listener of button
            categoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    entry.getValue().enabled = !entry.getValue().enabled;
                    if (entry.getValue().enabled) {
                        // TODO(twd2): use R.color.xxx
                        categoryButton.setBackgroundColor(selectedColor);
                        categoryButton.setTextColor(textSelectedColor);
                    } else {
                        categoryButton.setBackgroundColor(canceledColor);
                        categoryButton.setTextColor(textCanceledColor);
                    }
                }
            });

            // set the image of category
            ImageView categoryImage = (ImageView)categoryItem.getChildAt(0);
            categoryImage.setImageResource(entry.getKey().getUiId());
            counter++;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Categories.updateCategories();
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.app_bar_exit) {
            Categories.updateCategories();
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
