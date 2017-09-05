package com.java.a35.newsapp;

/**
 * Created by wuhaozhe on 2017/9/5.
 */

public class RecommendAPI {

    public static final int RANDOM_SAMPLE_NUM = 500;              //每次从所有种类中随机抽样的数量
    public static final int KEYWORD_SEARCH_SAMPLE = 50;          //每次根据KEY_WORD进行一次Search（不限category）,得到样本的数量
    public static final int KEYWORDS_NUM = 5;                      //每次进行search的keyword的数量

    //进行推荐的方法大致如下：每次分别从完全随机的新闻和根据关键字搜索的新闻选出一部分进行推荐


    private int recommendSize;                 //每次推荐的新闻数量
}
