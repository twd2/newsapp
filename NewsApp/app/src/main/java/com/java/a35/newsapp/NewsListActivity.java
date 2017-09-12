package com.java.a35.newsapp;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NewsListActivity extends AppCompatActivity {

    private String query = "";
    private static final int REQUEST_CATEGORY = 0;

    private CategoryCollectionPagerAdapter mFragmentPagerAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageButton categoryButton;               //该界面中用于切换到添加删除界面的button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        if (savedInstanceState == null) {
            boolean nightMode =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean("night_mode", false);
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            getDelegate().applyDayNight();
        }

        if (savedInstanceState != null) {
            query = savedInstanceState.getString("query");
        }

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mFragmentPagerAdapter =
                new CategoryCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        categoryButton = (ImageButton)findViewById(R.id.addCategory);
        categoryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(NewsListActivity.this, CategoryActivity.class);
                startActivityForResult(intent, REQUEST_CATEGORY);
            }
        });

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.app_bar_settings: {
                        Intent intent = new Intent(NewsListActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
                return true;
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Categories categories = ((App)getApplicationContext()).getCategories();
                Categories.CategoryType categorySelected = categories.enabledCategories[position];
                MenuItem search = toolbar.getMenu().findItem(R.id.app_bar_search);
                if (search != null) {
                    if (categorySelected == Categories.CategoryType.FAVORITE) {
                        if (search.isActionViewExpanded()) {
                            search.collapseActionView();
                        }
                        search.setVisible(false);
                    } else {
                        search.setVisible(true);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt("tab_id"));
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem search = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) search.getActionView();
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);
        searchView.setQueryHint(getString(R.string.search_news));
        searchView.setIconifiedByDefault(true);

        if (mViewPager != null) {
            Categories categories = ((App)getApplicationContext()).getCategories();
            Categories.CategoryType categorySelected =
                    categories.enabledCategories[mViewPager.getCurrentItem()];
            if (categorySelected == Categories.CategoryType.FAVORITE) {
                search.collapseActionView();
                search.setVisible(false);
            } else {
                search.setVisible(true);
            }
        }

        if (query != null && query.length() > 0) {
            search.expandActionView();
            searchView.setQuery(query, false);
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
            searchView.clearFocus();
        }

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d("search", "onMenuItemActionExpand");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d("search", "onMenuItemActionCollapse");
                query = "";
                doRefresh();
                return true;
            }
        });

        searchView.onActionViewExpanded();
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String queryText) {
                Log.d("search", queryText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String queryText) {
                Log.d("search", queryText);
                query = queryText;
                doRefresh();

                if (searchView != null) {
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }
                    searchView.clearFocus();
                }
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CATEGORY) {
            Log.d("list", "resetAdapter");
            // reset adaptor
            mViewPager.getAdapter().notifyDataSetChanged();
            mViewPager.setCurrentItem(0);
            mViewPager.setAdapter(mViewPager.getAdapter());
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("list", "onSaveInstanceState");
        outState.putString("query", query);
        outState.putInt("tab_id", mViewPager.getCurrentItem());
    }

    public void doRefresh() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof NewsListFragment) {
                ((NewsListFragment)fragment).doRefresh();
            } else {
                Log.d("list", "unknown fragment " + fragment);
            }
        }
    }

    public String getQuery() {
        return query;
    }

    public class CategoryCollectionPagerAdapter extends FragmentStatePagerAdapter {

        private Categories mCategories;

        public CategoryCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
            mCategories = ((App)getApplicationContext()).getCategories();
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("list/adaptor", "getItem: " + position);
            NewsListFragment fragment = new NewsListFragment();
            Bundle args = new Bundle();
            args.putString(NewsListFragment.ARG_CATEGORY,
                    mCategories.enabledCategories[position].toString());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mCategories.enabledCategories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mCategories.enabledCategories[position].getNameId());
        }
    }

}