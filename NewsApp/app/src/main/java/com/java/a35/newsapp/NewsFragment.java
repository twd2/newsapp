package com.java.a35.newsapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wuhaozhe on 2017/9/2.
 */

public class NewsFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //TODO(twd2): savedInstanceState传入了指定类型,inflater中需要inflate指定类型新闻的layout
        String type = getArguments().getString("category");
        return inflater.inflate(R.layout.activity_category, container, false);
    }

}
