package com.java.a35.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.app_bar_favorite:
                        Snackbar.make(getCurrentFocus(), "已收藏", Snackbar.LENGTH_LONG)
                                .setAction("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(ItemDetailActivity.this,
                                                "已取消收藏", Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                        // TODO
//                        Drawable icon = Resources.getSystem().getDrawable(R.drawable.ic_favorite_border_white_24dp, null);
//                        item.setIcon(icon);
                        break;
                    case R.id.app_bar_share:
                        Toast.makeText(ItemDetailActivity.this,
                                "分享到社交网络", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(savedInstanceState);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
            NestedScrollView scroll = (NestedScrollView)findViewById(R.id.item_detail_container);
            Log.d("scroll", "" + scroll.getWidth());
//            int scroll_y = savedInstanceState.getInt("scroll_y");
//            scroll.setScrollY(scroll_y / scroll.getHeight());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState != null) {
            NestedScrollView scroll = (NestedScrollView)findViewById(R.id.item_detail_container);
            Log.d("scroll", "" + scroll.getWidth());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ItemDetailFragment.ARG_ITEM_ID,
                           getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
        NestedScrollView scroll = (NestedScrollView)findViewById(R.id.item_detail_container);
        outState.putInt("scroll_y", scroll.getScrollY() * scroll.getHeight());
        Log.d("scroll", "" + scroll.getWidth());
    }
}
