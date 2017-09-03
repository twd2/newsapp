package com.java.a35.newsapp;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.java.a35.newsapp.dummy.DummyContent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private LoaderManager.LoaderCallbacks<JSONObject> newsDetailCallbacks;
    private static final int NEWS_DETAIL_LOADER_ID = 0;

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.NewsItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("frag", "onCreate");

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.NEWS_MAP.get(getArguments().getString(ARG_ITEM_ID));

            newsDetailCallbacks = new LoaderManager.LoaderCallbacks<JSONObject>() {
                @Override
                public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
                    return new NewsDetailLoader(getContext(),
                            new NewsDetailLoader.QueryCallback() {
                                @Override
                                public String getId() {
                                    if (mItem.obj != null) {
                                        try {
                                            return mItem.obj.getString("news_ID");
                                        } catch (JSONException e) {
                                            return "";
                                        }
                                    } else {
                                        return "";
                                    }
                                }
                            });
                }

                @Override
                public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                    showDetail(data);
                }

                @Override
                public void onLoaderReset(Loader<JSONObject> loader) {

                }
            };

            getLoaderManager().initLoader(NEWS_DETAIL_LOADER_ID, null, newsDetailCallbacks);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            WebView webView = rootView.findViewById(R.id.item_web);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadDataWithBaseURL(null, "<p>正在加载...</p>",
                                        "text/html", "UTF-8", null);
        }

        return rootView;
    }

    protected void showDetail(JSONObject obj) {
        if (obj != null) {
            try {
                mItem.detail = obj.getString("news_Content").replace("　　", "\n　　");
                WebView webView = getActivity().findViewById(R.id.item_web);
                webView.setBackgroundColor(Color.TRANSPARENT);
                webView.loadDataWithBaseURL(null,
                        String.format("<style>\n" +
                                      "a {color: darkblue; font-size: 20px;}\n" +
                                      "p {font-size: 20px; line-height: 150%%}" +
                                      "</style>" +
                                      "<h1>广告位招租</h1>\n<h2>联系：13000000000</h2>\n" +
                                      "<h1>%s</h1>\n<p>%s</p>\n" +
                                      "<a href=\"%s\" target=\"_blank\">查看原文</a>",
                                TextUtils.htmlEncode(mItem.content),
                                TextUtils.htmlEncode(mItem.detail).replace("\n", "</p>\n<p>"),
                                obj.getString("news_URL")),
                        "text/html", "UTF-8", null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ((TextView) getActivity().findViewById(R.id.item_detail)).setText("加载失败:(");
        }
    }

}
