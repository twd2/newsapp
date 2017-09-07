package com.java.a35.newsapp;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by wuhaozhe on 2017/9/2.
 * Modified by twd2 on 2017/9/5.
 */

public class NewsListFragment extends Fragment {

    public static final String ARG_CATEGORY = "category";

    private LoaderManager.LoaderCallbacks<JSONObject> newsListCallbacks;
    private static final int NEWS_LIST_LOADER_ID = 0;
    private Categories.CategoryType categoryType;
    private boolean isLoadingMore = false;
    private int loadedPage = 0;
    private int expectPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("frag", "NewsListFragment.onCreate " + categoryType);

        newsListCallbacks = new LoaderManager.LoaderCallbacks<JSONObject>() {
            @Override
            public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
                return new NewsListLoader(getContext(),
                        new NewsListLoader.QueryCallback() {
                            @Override
                            public NewsListLoader.Query getQuery() {
                                ItemListActivity activity = (ItemListActivity)getActivity();
                                if (activity != null) {
                                    return new NewsListLoader.Query(activity.getQuery(),
                                            loadedPage, expectPage, categoryType);
                                } else {
                                    return null;
                                }
                            }
                        });
            }

            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                updateNews(data, isLoadingMore);
            }

            @Override
            public void onLoaderReset(Loader<JSONObject> loader) {

            }
        };

        if (savedInstanceState != null) {
            loadedPage = savedInstanceState.getInt("loadedPage");
            expectPage = savedInstanceState.getInt("expectPage");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String type = getArguments().getString(ARG_CATEGORY);
        Log.d("onCreateView", type);
        categoryType = Categories.CategoryType.valueOf(type);

        final SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)inflater.inflate(R.layout.news_list, container, false);
        refreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("frag", "1");
                doRefresh();
            }
        });
        refreshLayout.setRefreshing(true);

        RecyclerView recyclerView = (RecyclerView)refreshLayout.findViewById(R.id.newsList);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);
        getLoaderManager().initLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        // load more
        final LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoadingMore && totalItemCount <= (lastVisibleItem + 5) &&
                        getActivity() != null) {
                    isLoadingMore = true;
                    ++expectPage;
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                                ((NewsItemRecyclerViewAdapter) recyclerView.getAdapter())
                                        .mValues.add(null);
                                recyclerView.getAdapter().notifyItemInserted(totalItemCount);
                                Log.d("frag", "2");
                                getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null,
                                        newsListCallbacks);
                        }
                    });
                }
            }
        });

        // TODO(twd2): strange code
        Log.d("frag", "" + (ItemListActivity)getActivity());
        ((ItemListActivity)getActivity()).registerFragment(categoryType, this);
        return refreshLayout;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Categories categories = ((App)getContext().getApplicationContext()).getCategories();
        recyclerView.setAdapter(new NewsItemRecyclerViewAdapter(
                categories.categories.get(categoryType).items
        ));
    }

    public void doRefresh() {
        if (getView() == null) {
            return;
        }
        Log.d("frag", "doRefresh");

        loadedPage = 0;
        expectPage = 1;

        SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);
    }

    private void updateNews(JSONObject obj, boolean append) {
        Categories categories = ((App)getContext().getApplicationContext()).getCategories();
        Categories.Category category = categories.categories.get(categoryType);

        if (loadedPage == 0) {
            category.clear();
        } else {
            if (category.items.size() > 0 &&
                    category.items.get(category.items.size() - 1) == null) {
                category.items.remove(category.items.size() - 1);
            }
        }

        try {
            JSONArray newsList = obj.getJSONArray("list");
            for (int i = 0; i < newsList.length(); ++i) {
                JSONObject news = newsList.getJSONObject(i);
                category.addItem(new Categories.NewsItem(news.getString("news_ID"),
                        news.getString("news_Title"), news.getBoolean("read"), news));
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        loadedPage = expectPage;
        isLoadingMore = false;
        SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshing(false);
        RecyclerView newsList = (RecyclerView)refreshLayout.findViewById(R.id.newsList);
        newsList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("loadedPage", loadedPage);
        outState.putInt("expectPage", expectPage);
    }

    public class NewsItemRecyclerViewAdapter
            extends RecyclerView.Adapter<NewsItemRecyclerViewAdapter.ViewHolder> {

        private final List<Categories.NewsItem> mValues;

        public NewsItemRecyclerViewAdapter(List<Categories.NewsItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.news_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);

            if (holder.mItem == null) {
                holder.mSourceView.setText("");
                holder.mDatetimeView.setText("");
                holder.mTitleView.setText("加载更多中...");
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                return;
            }

            try {
                holder.mSourceView.setText(mValues.get(position).obj.getString("news_Author"));
                String timeString = mValues.get(position).obj.getString("news_Time");
                if (timeString.length() >= 8) {
                    holder.mDatetimeView.setText(timeString.substring(0, 8));
                } else {
                    holder.mDatetimeView.setText("");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.mTitleView.setText(mValues.get(position).title);
            if (!mValues.get(position).read) {
                holder.mTitleView.setTextColor(ResourcesCompat.getColor(
                        getResources(),
                        R.color.newsTitleUnread,
                        getContext().getTheme()));
            } else {
                holder.mTitleView.setTextColor(ResourcesCompat.getColor(
                        getResources(),
                        R.color.newsTitleRead,
                        getContext().getTheme()));
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_CATEGORY, categoryType.toString());
                    intent.putExtra(ItemDetailFragment.ARG_NEWS_ID, holder.mItem.id);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mSourceView, mDatetimeView; 
            public Categories.NewsItem mItem;
            

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.title);
                mSourceView = (TextView) view.findViewById(R.id.source);
                mDatetimeView = (TextView) view.findViewById(R.id.datetime);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }

}
