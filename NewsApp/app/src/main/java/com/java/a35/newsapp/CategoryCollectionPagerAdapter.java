package com.java.a35.newsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by wuhaozhe on 2017/9/2.
 */

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class CategoryCollectionPagerAdapter extends FragmentStatePagerAdapter {
    public CategoryCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        NewsFragment mFragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString("category", CategoryController.addedCategories[position]);
        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public int getCount() {
        return CategoryController.addedCategories.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return CategoryController.addedCategories[position];
    }
}
