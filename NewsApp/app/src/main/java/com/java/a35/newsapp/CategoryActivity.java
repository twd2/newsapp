package com.java.a35.newsapp;

import android.app.ListActivity;
import android.os.Bundle;
import java.util.*;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wuhaozhe on 2017/9/2.
 */

public class CategoryActivity extends AppCompatActivity{
    Button[] categoryButton;             //存储category_item.xml中的button
    final int selectedColor = 0x22000000;
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
            String categoryName = (String)pair.getKey();
            if(categoryName == "推荐" || categoryName == "收藏")
            {
                continue;
            }
            Boolean categoryExist = (Boolean)pair.getValue();
            RelativeLayout categoryItem = (RelativeLayout) tmp.getChildAt(counter);
            Button categoryButton = (Button)categoryItem.getChildAt(1);
            categoryButton.setText(categoryName);
            if(categoryExist)
            {
                categoryButton.setBackgroundColor(selectedColor);
            }
            else
            {
                categoryButton.setBackgroundColor(canceledColor);
            }
            counter++;
        }
    }
}
