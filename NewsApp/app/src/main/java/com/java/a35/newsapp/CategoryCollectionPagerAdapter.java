package com.java.a35.newsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by wuhaozhe on 2017/9/2.
 */

public class CategoryCollectionPagerAdapter extends FragmentStatePagerAdapter {
    public CategoryCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putString(NewsListFragment.ARG_CATEGORY_ID,
                Categories.enabledCategories[position].toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return Categories.enabledCategories.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Categories.enabledCategories[position].getName();
    }
}
