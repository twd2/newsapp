package com.java.a35.newsapp;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by wuhaozhe on 2017/9/5.
 */

public class RecommendAPI {

    static final int RANDOM_SAMPLE_NUM = 50;              //每次各个种类中随机抽样的数量
    static final int KEYWORD_SEARCH_SAMPLE = 50;          //每次根据KEY_WORD进行一次Search（不限category）,得到样本的数量
    static final int KEYWORDS_NUM = 5;                      //每次进行search的keyword的数量
    static final int RANDOM_MIN = 2;                         //完全随机新闻最小数量
    //进行推荐的方法大致如下：每次分别从完全随机的新闻和根据关键字搜索的新闻选出一部分进行推荐


    private int recommendSize;                 //每次推荐的新闻数量
    public Context context;

    public RecommendAPI(Context context){
        this.context = context;
        recommendSize = ((App) context).getApi().DEFAULT_PAGE_SIZE;
    }

    private int randomNewsNum(int readNewsNum){                 //完全随机的新闻占推荐新闻的比重(输入为阅读的新闻数量，随着新闻数量的增加，比重越来越低)
        int randomNum = (int)(recommendSize * (1 / Math.sqrt(readNewsNum)));
        randomNum = Math.max(randomNum, RANDOM_MIN);
        return randomNum;
    }

    private String[] getRandomNews(int readNewsNum) throws IOException, JSONException{              //readNewsNum是用户已经阅读的新闻数量
        API api = ((App) context).getApi();
        int newsNum = randomNewsNum(readNewsNum);
        JSONArray all = new JSONArray();
        Random rn = new Random();
        for(int i = API.CATEGORY_MIN; i <= API.CATEGORY_MAX; i++){
            int randomPage = rn.nextInt() % RANDOM_SAMPLE_NUM;
            randomPage += 1;
            JSONObject listNews = api.getListNews(i, randomPage, RANDOM_SAMPLE_NUM);
            all.put(i, listNews);            //首先将所有种类的新闻放心all
        }

        HashSet<String> newsID = new HashSet<String>();
        int counter = 0;
        while(counter < newsNum) {
            int randomCategory = rn.nextInt() % (API.CATEGORY_MAX - API.CATEGORY_MIN + 1) + 1;
            JSONArray currentListNews = (all.getJSONObject(randomCategory)).getJSONArray("list");
            int randomIndex = rn.nextInt() % currentListNews.length();
            newsID.add(
                    (currentListNews.getJSONObject(randomIndex)).getString("news_Author"));
            counter++;
        }
        return newsID.toArray(new String[newsNum]);
    }

    private float get_score(Hashtable<String, Float> history, JSONArray target) throws  IOException, JSONException
    {
        float sum = 0;
        for(int i = 0; i < target.length(); i++){
            String keyword = (target.getJSONObject(i)).getString("word");
            if(history.containsKey(keyword))
            {
                sum += (history.get(keyword) * (target.getJSONObject(i)).getDouble("score"));
            }
        }
        return sum;
    }

    public String[] getRecommendNews() throws  IOException, JSONException{                           //返回推荐的新闻
        //TODO:(wuhaozhe) 根据参数解析得到阅读新闻的总数，关键字与分数的map，用户最经常浏览的关键词，不同分类在推荐中的重要程度

        int readNewsNum;
        Hashtable<String, Float> wordScoreMap = new Hashtable<String, Float>();
        String[] topWords = new String[KEYWORDS_NUM];
        float[] categortyWeight = new float[API.CATEGORY_MAX - API.CATEGORY_MIN + 1];

        API api = ((App) context).getApi();

        for(int i = 0; i < topWords.length; i++) {
            JSONArray allWordNews = (api.searchAllNews(topWords[i], 1, KEYWORD_SEARCH_SAMPLE)).getJSONArray("list");
            for(int j = 0; j < allWordNews.length(); j++)
            {

                float score = get_score(wordScoreMap, allWordNews);
            }


        }
        return new String[10];
    }

}
