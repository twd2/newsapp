package com.java.a35.newsapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import java.util.*;

import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wuhaozhe on 2017/9/2.
 */

public class CategoryActivity extends AppCompatActivity{
    Button[] categoryButton;             //存储category_item.xml中的button
    final int selectedColor = 0x88000000;
    final int canceledColor = 0xcc000000;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        categoryButton = new Button[CategoryController.categories.size()];
        setContentView(R.layout.activity_category);
        LinearLayout tmp = (LinearLayout) findViewById(R.id.categoryLinearLayout);
        int counter = 0;
        Iterator it = CategoryController.categories.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            final String categoryName = (String)pair.getKey();
            if(categoryName == "推荐" || categoryName == "收藏")
            {
                continue;
            }
            final Boolean categoryExist = (Boolean)pair.getValue();
            RelativeLayout categoryItem = (RelativeLayout) tmp.getChildAt(counter);
            //get the button and set its color
            final Button categoryButton = (Button)categoryItem.getChildAt(1);
            categoryButton.setText(categoryName);
            if(categoryExist)
            {
                categoryButton.setBackgroundColor(selectedColor);
            }
            else
            {
                categoryButton.setBackgroundColor(canceledColor);
            }
            //set the click listener of button
            categoryButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    Boolean tmp = CategoryController.categories.get(categoryName);
                    if(tmp)
                    {
                        categoryButton.setBackgroundColor(canceledColor);
                    }
                    else
                    {
                        categoryButton.setBackgroundColor(selectedColor);
                    }
                    CategoryController.categories.put(categoryName, !tmp);
                }
            });
            //set the image of category
            ImageView categoryImage = (ImageView)categoryItem.getChildAt(0);
            switch (categoryName)
            {
                case "科技":
                    categoryImage.setImageResource(R.drawable.science);
                    break;
                case "教育":
                    categoryImage.setImageResource(R.drawable.education);
                    break;
                case "军事":
                    categoryImage.setImageResource(R.drawable.army);
                    break;
                case "国内":
                    categoryImage.setImageResource(R.drawable.china);
                    break;
                case "社会":
                    categoryImage.setImageResource(R.drawable.social);
                    break;
                case "文化":
                    categoryImage.setImageResource(R.drawable.culture);
                    break;
                case "汽车":
                    categoryImage.setImageResource(R.drawable.car);
                    break;
                case "国际":
                    categoryImage.setImageResource(R.drawable.international);
                    break;
                case "体育":
                    categoryImage.setImageResource(R.drawable.sports);
                    break;
                case "财经":
                    categoryImage.setImageResource(R.drawable.financial);
                    break;
                case "健康":
                    categoryImage.setImageResource(R.drawable.health);
                    break;
                case "娱乐":
                    categoryImage.setImageResource(R.drawable.entertainment);
                    break;
            }
            counter++;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                CategoryController.refreshAddedCategories();
                Intent intent = new Intent(this, ItemListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.app_bar_exit) {
            CategoryController.refreshAddedCategories();
            Intent intent = new Intent(this, ItemListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
