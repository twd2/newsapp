package com.java.a35.newsapp;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.java.a35.newsapp.dummy.DummyContent;

import java.util.List;

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

    private CategoryCollectionPagerAdapter mFragmentPagerAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TabLayout.Tab[] mTabs;
    private ImageButton categoryButton;               //该界面中用于切换到添加删除界面的button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        Log.d("test", getFilesDir().getAbsolutePath());

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mTabs = new TabLayout.Tab[CategoryController.addedCategories.length];
        //使用适配器将ViewPager与Fragment绑定在一起
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mFragmentPagerAdapter =
                new CategoryCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentPagerAdapter);
        //将TabLayout与ViewPager绑定在一起
        mTabLayout = (TabLayout)findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        //指定Tab位置
        for(int i = 0; i < CategoryController.addedCategories.length; i++)
        {
            mTabs[i] = mTabLayout.getTabAt(i);
        }
        //TODO(wuhaozhe): 设置Tab的图标
        //初始化categoryButton
        categoryButton = (ImageButton)findViewById(R.id.addCategory);
        categoryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ItemListActivity.this, CategoryActivity.class);
                startActivity(intent);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.app_bar_search: {
                        Toast.makeText(ItemListActivity.this, "Search!", Toast.LENGTH_SHORT).show();

//                        Context context = ItemListActivity.this.getCurrentFocus().getContext();
//                        Intent intent = new Intent(context, SearchActivity.class);
//                        context.startActivity(intent);
                        break;
                    } case R.id.app_bar_settings: {
                        Context context = ItemListActivity.this.getCurrentFocus().getContext();
                        Intent intent = new Intent(context, SettingsActivity.class);
                        context.startActivity(intent);
                    }
                }
                return true;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "菜单", Snackbar.LENGTH_LONG)
                        .setAction("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(ItemListActivity.this,"你点击了action",Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        /*View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);*/

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        handleIntent(getIntent());
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);

        searchView.setIconifiedByDefault(true);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ItemListActivity.this,
                               "s=" + searchView.getQuery(), Toast.LENGTH_LONG);
            }
        });

        mSearchView = searchView;

        /**
         * 默认情况下是没提交搜索的按钮，所以用户必须在键盘上按下"enter"键来提交搜索.你可以同过setSubmitButtonEnabled(
         * true)来添加一个提交按钮（"submit" button)
         * 设置true后，右边会出现一个箭头按钮。如果用户没有输入，就不会触发提交（submit）事件
         */
        mSearchView.setSubmitButtonEnabled(true);
        /**
         * 初始是否已经是展开的状态
         * 写上此句后searchView初始展开的，也就是是可以点击输入的状态，如果不写，那么就需要点击下放大镜，才能展开出现输入框
         */
        mSearchView.onActionViewExpanded();
        /**
         * 默认情况下, search widget是"iconified“的，只是用一个图标 来表示它(一个放大镜),
         * 当用户按下它的时候才显示search box . 你可以调用setIconifiedByDefault(false)让search
         * box默认都被显示。 你也可以调用setIconified()让它以iconified“的形式显示。
         */
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String queryText) {
                Toast.makeText(ItemListActivity.this, queryText, Toast.LENGTH_LONG);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String queryText) {
                Toast.makeText(ItemListActivity.this, queryText, Toast.LENGTH_LONG);

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
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        // TODO 自动生成的方法存根
        Toast.makeText(this, "do search " + query, Toast.LENGTH_LONG).show();
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
