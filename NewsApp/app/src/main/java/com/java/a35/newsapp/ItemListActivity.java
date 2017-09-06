package com.java.a35.newsapp;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SearchView mSearchView;
    private String query = "";
    private static final int REQUEST_CATEGORY = 0;

    private CategoryCollectionPagerAdapter mFragmentPagerAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageButton categoryButton;               //该界面中用于切换到添加删除界面的button
    private Map<Categories.CategoryType, NewsListFragment> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (savedInstanceState != null) {
            query = savedInstanceState.getString("query");
        }

        Log.d("test", getFilesDir().getAbsolutePath());

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        //使用适配器将ViewPager与Fragment绑定在一起
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mFragmentPagerAdapter =
                new CategoryCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentPagerAdapter);
        //将TabLayout与ViewPager绑定在一起
        mTabLayout = (TabLayout)findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        categoryButton = (ImageButton)findViewById(R.id.addCategory);
        categoryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ItemListActivity.this, CategoryActivity.class);
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
                        Intent intent = new Intent(ItemListActivity.this, SettingsActivity.class);
                        startActivity(intent);
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
                Categories mcategories = ((App)getApplicationContext()).getCategories();
                Categories.CategoryType categorySelected = mcategories.enabledCategories[position];
                MenuItem search = toolbar.getMenu().findItem(R.id.app_bar_search);
                if (search != null) {
                    if (categorySelected == Categories.CategoryType.FAVORITE) {
                        search.collapseActionView();
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

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem search = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) search.getActionView();
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);
        searchView.setQueryHint("搜索新闻");
        searchView.setIconifiedByDefault(true);

        if (mViewPager != null) {
            Categories mcategories = ((App)getApplicationContext()).getCategories();
            Categories.CategoryType categorySelected =
                    mcategories.enabledCategories[mViewPager.getCurrentItem()];
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

        mSearchView = searchView;
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

                if (mSearchView != null) {
                    // 得到输入管理对象
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // 这将让键盘在所有的情况下都被隐藏，但是一般我们在点击搜索按钮后，输入法都会乖乖的自动隐藏的。
                        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
                    }
                    mSearchView.clearFocus(); // 不获取焦点
                }
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // doMySearch(query);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CATEGORY) {
            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("query", query);
        outState.putInt("tab_id", mViewPager.getCurrentItem());
    }

    public void doRefresh() {
        // TODO(twd2): strange code
        for (NewsListFragment fragment : fragmentMap.values()) {
            fragment.doRefresh();
        }
    }

    public void registerFragment(Categories.CategoryType category, NewsListFragment fragment) {
        fragmentMap.put(category, fragment);
    }

    public String getQuery() {
        return query;
    }
    public class CategoryCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public CategoryCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            NewsListFragment fragment = new NewsListFragment();
            Bundle args = new Bundle();
            Categories mcategories = ((App)getApplicationContext()).getCategories();
            args.putString(NewsListFragment.ARG_CATEGORY,
                    mcategories.enabledCategories[position].toString());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            Categories mcategories = ((App)getApplicationContext()).getCategories();
            return mcategories.enabledCategories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Categories mcategories = ((App)getApplicationContext()).getCategories();
            return mcategories.enabledCategories[position].getName();
        }
    }

}