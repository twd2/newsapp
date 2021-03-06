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
    private boolean noMore = false;
    private int loadedPage = 0;
    private int expectPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("frag", "NewsListFragment.onCreate");

        newsListCallbacks = new LoaderManager.LoaderCallbacks<JSONObject>() {
            @Override
            public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
                return new NewsListLoader(getContext(),
                        new NewsListLoader.QueryCallback() {
                            @Override
                            public NewsListLoader.Query getQuery() {
                                NewsListActivity activity = (NewsListActivity)getActivity();
                                if (activity != null) {
                                    return new NewsListLoader.Query(activity.getQuery(),
                                            loadedPage, expectPage, categoryType);
                                } else {
                                    Log.d("cb", "activity == null");
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String type = getArguments().getString(ARG_CATEGORY);
        Log.d("onCreateView", type);
        categoryType = Categories.CategoryType.valueOf(type);

        if (savedInstanceState != null) {
            Log.d("frag " + categoryType, "onCreateView, restoring saved state");
            // loadedPage = savedInstanceState.getInt("loadedPage");
            loadedPage = 0; // TODO(twd2)
            expectPage = savedInstanceState.getInt("expectPage");
            noMore = savedInstanceState.getBoolean("noMore");
            Log.d("frag " + categoryType, "saved state = " + loadedPage + "/" + expectPage);
        }

        final SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)inflater.inflate(R.layout.news_list, container, false);
        refreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("frag " + categoryType, "OnRefresh");
                doRefresh();
            }
        });
        refreshLayout.setRefreshing(true);

        RecyclerView recyclerView = (RecyclerView)refreshLayout.findViewById(R.id.newsList);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

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
                if (!isLoadingMore && !noMore && totalItemCount <= (lastVisibleItem + 5) &&
                        getActivity() != null) {
                    isLoadingMore = true;
                    ++expectPage;
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                ((NewsItemRecyclerViewAdapter) recyclerView.getAdapter())
                                        .mValues.add(
                                                new Categories.NewsItem(R.string.loading_more));
                                recyclerView.getAdapter().notifyItemInserted(totalItemCount);
                                Log.d("frag " + categoryType, "onScrolled");
                                getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null,
                                        newsListCallbacks);
                            }
                        }
                    });
                }
            }
        });

        getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);

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
        Log.d("frag " + categoryType, "doRefresh");

        loadedPage = 0;
        expectPage = 1;

        SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);
    }

    private void updateNews(JSONObject obj, boolean append) {
        if (getContext() == null || getActivity() == null) {
            return;
        }

        Log.d("frag " + categoryType, "update = " + obj);

        Categories categories = ((App)getContext().getApplicationContext()).getCategories();
        Categories.Category category = categories.categories.get(categoryType);

        if (loadedPage == 0) {
            category.clear();
        } else {
            if (category.items.size() > 0 &&
                    category.items.get(category.items.size() - 1).special) {
                category.items.remove(category.items.size() - 1);
            }
        }

        if (obj == null) {
            Log.d("frag " + categoryType, "loader returned null, // retrying");
            noMore = true;
            category.addItem(new Categories.NewsItem(R.string.network_error));
            // getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);
            isLoadingMore = false;
            SwipeRefreshLayout refreshLayout =
                    (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
            refreshLayout.setRefreshing(false);
            RecyclerView newsList = (RecyclerView)refreshLayout.findViewById(R.id.newsList);
            newsList.getAdapter().notifyDataSetChanged();
            return;
        }

        try {
            JSONArray newsList = obj.getJSONArray("list");

            if (loadedPage != expectPage && obj.getBoolean("noMore")) {
                noMore = true;
                category.addItem(new Categories.NewsItem(R.string.no_more));
            } else {
                noMore = false;
            }

            for (int i = 0; i < newsList.length(); ++i) {
                JSONObject news = newsList.getJSONObject(i);
                category.addItem(new Categories.NewsItem(news.getString("news_ID"),
                        news.getString("news_Title"), news.getBoolean("read"),
                        news.getBoolean("favorite"), news));
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        Log.d("frag " + categoryType, "list.size = " + category.items.size());

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
        super.onSaveInstanceState(outState);

        Log.d("frag " + categoryType, "onSaveInstanceState");
        outState.putInt("loadedPage", loadedPage);
        outState.putInt("expectPage", expectPage);
        outState.putBoolean("noMore", noMore);
        // outState.putString("category", categoryType.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d("frag " + categoryType, "onDestroyView " + getLoaderManager().hasRunningLoaders());
        getLoaderManager().destroyLoader(NEWS_LIST_LOADER_ID);

        // I think this is workaround.
        try {
            SwipeRefreshLayout refreshLayout =
                    (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
            refreshLayout.setRefreshing(false);
            refreshLayout.setVisibility(View.INVISIBLE);
            RecyclerView recyclerView = (RecyclerView)getView().findViewById(R.id.newsList);
            recyclerView.setVisibility(View.INVISIBLE);
            getView().setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (getActivity() == null) {
                return;
            }

            holder.mItem = mValues.get(position);

            if (holder.mItem.special) {
                holder.mSourceView.setText("");
                holder.mDatetimeView.setText("");
                holder.mTitleView.setTextColor(ResourcesCompat.getColor(
                        getResources(),
                        R.color.primaryTextDark,
                        getContext().getTheme()));
                holder.mTitleView.setText(holder.mItem.specialType);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.mItem.specialType != R.string.loading_more) {
                            doRefresh();
                        }
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
                        R.color.primaryTextDark,
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
                    holder.mItem.read = true;
                    notifyItemChanged(holder.getAdapterPosition());
                    Context context = v.getContext();
                    Intent intent = new Intent(context, NewsDetailActivity.class);
                    intent.putExtra(NewsDetailFragment.ARG_CATEGORY, categoryType.toString());
                    intent.putExtra(NewsDetailFragment.ARG_NEWS_ID, holder.mItem.id);
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
