package com.java.a35.newsapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link NewsListActivity}
 * in two-pane mode (on tablets) or a {@link NewsDetailActivity}
 * on handsets.
 */
public class NewsDetailFragment extends Fragment {

    public static final String ARG_CATEGORY = "category";
    public static final String ARG_NEWS_ID = "news_id";

    private LoaderManager.LoaderCallbacks<JSONObject> newsDetailCallbacks;
    private static final int NEWS_DETAIL_LOADER_ID = 0;

    private Categories.NewsItem mItem;
    private String styleString;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        styleString = "<style>\na {text-decoration: none; color:"
                + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark) - 0xFF000000)
                + "; font-size: 20px;}\n"
                + "p {font-size: 20px; line-height: 150%%}\n"
                + "html {color:"
                + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.primaryTextDark) - 0xFF000000)
                + "}\n</style>";

        Bundle args = getArguments();

        if (args.containsKey(ARG_CATEGORY) && args.containsKey(ARG_NEWS_ID)) {
            Categories.CategoryType categoryType =
                    Categories.CategoryType.valueOf(
                            args.getString(NewsDetailFragment.ARG_CATEGORY));
            Categories categories = ((App)getContext().getApplicationContext()).getCategories();
            mItem = categories.categories.get(categoryType).map
                    .get(args.getString(NewsDetailFragment.ARG_NEWS_ID));

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
                    ((NewsDetailActivity)getActivity()).mDetail = data;
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
        View rootView = inflater.inflate(R.layout.news_detail, container, false);

        if (mItem != null) {
            WebView webView = (WebView) rootView.findViewById(R.id.item_web);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadDataWithBaseURL(null, styleString + "<p>" +
                            getString(R.string.loading) +
                            "</p>",
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
                Set<String> blockSet =
                        getContext().getSharedPreferences(BlockSettingsActivity.PREFERENCES_BLOCK,
                                                          Context.MODE_PRIVATE)
                                .getStringSet("block_list", new HashSet<String>());
                // block keywords
                boolean blocked = false;
                for (String keyword : blockSet) {
                    String lowerCaseKeyword = keyword.toLowerCase();
                    if (mItem.detail.toLowerCase().contains(lowerCaseKeyword)) {
                        blocked = true;
                        break;
                    }
                }

                if (blocked) {
                    webView.loadDataWithBaseURL(null, styleString + "<h1>" +
                            getString(R.string.news_blocked) +
                            "</h1>", "text/html", "UTF-8", null);
                    return;
                }

                // prepare detail html
                String htmlDetail = TextUtils.htmlEncode(mItem.detail).replace("\n", "</p>\n<p>");
                htmlDetail = linkToEncyclopedia(htmlDetail, obj);

                // prepare picture html
                StringBuilder pictureHtml = new StringBuilder();
                if (obj.has("pictures_path")) {
                    JSONArray picturesPath = obj.getJSONArray("pictures_path");
                    for (int i = 0; i < picturesPath.length(); i++) {
                        pictureHtml.append(String.format(
                            "<p><img src=\"file://%s\" alt=\"\" style=\"max-width: 100%%\" /></p>\n",
                                picturesPath.getString(i)));
                    }
                    if (obj.getBoolean("isSearchedImages")) {
                        pictureHtml.append("<p>(" + getString(R.string.searched_images) + ")</p>\n");
                    }
                }

                // build whole html
                StringBuilder sb = new StringBuilder();
                Resources.Theme theme = getContext().getTheme();
                sb.append(
                        String.format("%s" +
                                      "<h1>%s</h1>\n" +
                                      "%s\n" +
                                      "<p>%s</p>\n" +
                                      "<a href=\"%s\" target=\"_blank\">%s</a>",
                                styleString,
                                TextUtils.htmlEncode(mItem.title),
                                pictureHtml.toString(),
                                htmlDetail,
                                obj.getString("news_URL"),
                                getString(R.string.view_source)));

                webView.loadDataWithBaseURL(null,
                        sb.toString(),
                        "text/html", "UTF-8", null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            webView.loadDataWithBaseURL(null, styleString + "<h1>" +
                    getString(R.string.load_failed) +
                    "</h1>", "text/html", "UTF-8", null);
        }
    }

}
