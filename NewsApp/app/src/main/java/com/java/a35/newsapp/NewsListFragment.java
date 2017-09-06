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
                            public String getQuery() {
                                ItemListActivity activity = (ItemListActivity)getActivity();
                                if (activity != null) {
                                    return activity.getQuery();
                                } else {
                                    return "";
                                }
                            }

                            @Override
                            public Categories.CategoryType getCategory() {
                                return categoryType;
                            }
                        });
            }

            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                updateNews(data);
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

        final SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)inflater.inflate(R.layout.news_list, container, false);
        refreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);
            }
        });
        refreshLayout.setRefreshing(true);

        View recyclerView = refreshLayout.findViewById(R.id.newsList);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        getLoaderManager().initLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);

        // TODO(twd2): strange code
        Log.d("frag", "" + (ItemListActivity)getActivity());
        ((ItemListActivity)getActivity()).registerFragment(categoryType, this);
        return refreshLayout;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new NewsItemRecyclerViewAdapter(
                Categories.categories.get(categoryType).items
        ));
    }

    public void doRefresh() {
        if (getView() == null) {
            return;
        }

        SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(NEWS_LIST_LOADER_ID, null, newsListCallbacks);
    }

    private void updateNews(JSONObject obj) {
        Categories.Category category = Categories.categories.get(categoryType);

        category.clear();
        try {
            JSONArray newsList = obj.getJSONArray("list");
            for (int i = 0; i < newsList.length(); ++i) {
                JSONObject news = newsList.getJSONObject(i);
                category.addItem(
                        new Categories.NewsItem(news.getString("news_ID"),
                                news.getString("news_Title"),
                                news));
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            category.addItem(new Categories.NewsItem(";(", "加载失败", null));
        }

        SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)getView().findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshing(false);
        RecyclerView newsList = (RecyclerView)refreshLayout.findViewById(R.id.newsList);
        newsList.getAdapter().notifyDataSetChanged();
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
            holder.mTitleView.setText(mValues.get(position).title);
            // TODO(twd2)
            if (Math.random() > 0.5) {
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
            public Categories.NewsItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }

}
