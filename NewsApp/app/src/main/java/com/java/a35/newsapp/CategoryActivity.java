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
