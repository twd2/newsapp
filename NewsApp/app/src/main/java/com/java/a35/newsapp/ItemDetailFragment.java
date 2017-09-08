package com.java.a35.newsapp;

import android.app.Activity;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {

    public static final String ARG_CATEGORY = "category";
    public static final String ARG_NEWS_ID = "news_id";

    private LoaderManager.LoaderCallbacks<JSONObject> newsDetailCallbacks;
    private static final int NEWS_DETAIL_LOADER_ID = 0;

    private Categories.NewsItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args.containsKey(ARG_CATEGORY) && args.containsKey(ARG_NEWS_ID)) {
            Categories.CategoryType categoryType =
                    Categories.CategoryType.valueOf(
                            args.getString(ItemDetailFragment.ARG_CATEGORY));
            Categories categories = ((App)getContext().getApplicationContext()).getCategories();
            mItem = categories.categories.get(categoryType).map
                    .get(args.getString(ItemDetailFragment.ARG_NEWS_ID));

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

            Activity activity = getActivity();
            CollapsingToolbarLayout appBarLayout =
                    (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.title);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        if (mItem != null) {
            WebView webView = (WebView) rootView.findViewById(R.id.item_web);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadDataWithBaseURL(null, "<p>正在加载...</p>",
                    "text/html", "UTF-8", null);
        }

        return rootView;
    }

    protected String wordToLink(String str, JSONArray array) throws JSONException {
        String result = str;
        for (int i = 0; i < array.length(); i++) {
            String word = array.getJSONObject(i).getString("word");
            // TODO(twd2): String.replace?
            Pattern p = Pattern.compile(word);
            Matcher m = p.matcher(result);
            String urlPart = "";
            try {
                urlPart = URLEncoder.encode(word, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String dst = String.format(
                    "<a href=\"https://baike.baidu.com/item/%s\" target=\"_blank\">%s</a>",
                    urlPart,
                    word);
            result = m.replaceFirst(dst);
        }
        return result;
    }

    protected String linkToEncyclopedia(String str, JSONObject obj) throws JSONException {
        JSONArray locations = obj.getJSONArray("locations");
        JSONArray organizations = obj.getJSONArray("organizations");
        JSONArray persons = obj.getJSONArray("persons");
        String result = str;
        result = wordToLink(result, locations);
        result = wordToLink(result, organizations);
        result = wordToLink(result, persons);
        return result;
    }

    protected void showDetail(JSONObject obj) {
        WebView webView = (WebView) getView().findViewById(R.id.item_web);
        webView.setBackgroundColor(Color.TRANSPARENT);
        if (obj != null) {
            try {
                // TODO(twd2): !!!
                mItem.detail = obj.getString("news_Content").replace("　　", "\n　　");
                String htmlDetail = TextUtils.htmlEncode(mItem.detail).replace("\n", "</p>\n<p>");
                htmlDetail = linkToEncyclopedia(htmlDetail, obj);
                StringBuilder sb = new StringBuilder();
                boolean show_picture = (PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getBoolean("show_pictures", true));
                if (show_picture) {
                    JSONArray picturesPath = obj.getJSONArray("pictures_path");
                    for (int i = 0; i < picturesPath.length(); i++) {
                        sb.append(String.format("<p><img src=\"file://%s\" alt=\"xxx\" style=\"max-width: 100%%\" /></p>",
                                picturesPath.getString(i)));
                    }
                }
                Resources.Theme theme = getContext().getTheme();
                String styleString = "a {text-decoration: none; color:"
                        + Integer.toHexString(getResources().getColor(R.color.colorPrimaryDark) - 0xff000000)
                        + "; font-size: 20px;}\n"
                        + "p {font-size: 20px; line-height: 150%%}\n"
                        + "html {color:"
                        + Integer.toHexString(getResources().getColor(R.color.primary_text_dark) - 0xff000000)
                        + "}";
                sb.append(
                        String.format("<style>\n%s</style>" +
                                      "<h1>广告位招租</h1>\n<h2>联系：13000000000</h2>\n" +
                                      "<h1>%s</h1>\n<p>%s</p>\n" +
                                      "<a href=\"%s\" target=\"_blank\">查看原文</a>",
                                styleString,
                                TextUtils.htmlEncode(mItem.title),
                                htmlDetail,
                                obj.getString("news_URL")));

                webView.loadDataWithBaseURL(null,
                        sb.toString(),
                        "text/html", "UTF-8", null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            webView.loadDataWithBaseURL(null, "<h1>加载失败 :(</h1>", "text/html", "UTF-8", null);
        }
    }

}
