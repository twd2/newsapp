package com.java.a35.newsapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by wuhaozhe on 2017/9/5.
 */

public class RecommendAPI {

    static final int RANDOM_SAMPLE_NUM = 10;              //每次各个种类中随机抽样的数量
    static final int KEYWORD_SEARCH_SAMPLE = 10;          //每次根据KEY_WORD进行一次Search（不限category）,得到样本的数量
    static final int KEYWORDS_NUM = 3;                      //每次进行search的keyword的数量
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

    private JSONArray getRandomNews(int newsNum) throws IOException, JSONException{              //readNewsNum是用户已经阅读的新闻数量
        API api = ((App) context).getApi();
        JSONArray all = new JSONArray();
        Random rn = new Random();
        for(int i = API.CATEGORY_MIN; i <= API.CATEGORY_MAX; i++){
            int randomPage = rn.nextInt(RANDOM_SAMPLE_NUM - 1) + 1;             //in order to avoid server's bug
            randomPage += 1;
            JSONObject listNews = api.getListNews(i, randomPage, RANDOM_SAMPLE_NUM);
            all.put(listNews);            //首先将所有种类的新闻放心all
        }

        HashSet<String> newsIDSet = new HashSet<String>();
        JSONArray array = new JSONArray();
        int counter = 0;
        while(counter < newsNum) {
            int randomCategory = rn.nextInt(API.CATEGORY_MAX - API.CATEGORY_MIN + 1);
            JSONArray currentListNews = (all.getJSONObject(randomCategory)).getJSONArray("list");
            int randomIndex = rn.nextInt(currentListNews.length());
            String newsID = (currentListNews.getJSONObject(randomIndex)).getString("news_ID");
            if(!newsIDSet.contains(newsID)){
                newsIDSet.add(newsID);
                array.put(currentListNews.getJSONObject(randomIndex));
            }
            counter++;
        }
        return array;
    }

    private float get_score(Hashtable<String, Float> history, JSONArray target) throws JSONException
    {
        float sum = 0;
        for(int i = 0; i < target.length(); i++){
                String keyword = (target.getJSONObject(i)).getString("word");
                if (history.containsKey(keyword)) {
                    sum += (history.get(keyword) * (target.getJSONObject(i)).getDouble("score"));
                }
        }
        return sum;
    }
    public class newsAndScore implements Comparable<newsAndScore>{
        public JSONObject newsSketch;             //新闻
        public float score;                       //新闻的得分

        newsAndScore(JSONObject newsSketch, float score){
            this.newsSketch = newsSketch;
            this.score = score;
        }

        @Override
        public int compareTo(newsAndScore another) {
            return (int)(another.score - this.score);
        }

    }
    public JSONObject getRecommendNews(int page) throws  IOException, JSONException{                           //返回推荐的新闻
        //TODO:(wuhaozhe) 根据参数解析得到阅读新闻的总数，关键字与分数的map，用户最经常浏览的关键词
        if(page >= 2)
        {
            return new JSONObject();
        }
        int readNewsNum = 10;
        Hashtable<String, Float> wordScoreMap = new Hashtable<String, Float>()
        {{
            put("习近平", 100.0f);
            put("中央", 200.0f);
            put("湖人", 150.0f);
            put("NBA", 200.0f);
            put("反腐", 50.0f);
        }};
        String[] topWords = {"习近平", "中央", "湖人"};
        API api = ((App) context).getApi();
        ArrayList<newsAndScore> array = new ArrayList<newsAndScore>();

        for(int i = 0; i < topWords.length; i++) {
            JSONArray allWordNews = (api.searchAllNews(topWords[i], 1, KEYWORD_SEARCH_SAMPLE)).getJSONArray("list");
            for(int j = 0; j < allWordNews.length(); j++){
                JSONObject newsSketch = allWordNews.getJSONObject(j);
                JSONObject newsDetail = api.getNews(newsSketch.getString("news_ID"));
                float score = get_score(wordScoreMap, newsDetail.getJSONArray("Keywords"));
                array.add(new newsAndScore(newsSketch, score));
            }
        }

        JSONArray recommendArray = new JSONArray();
        Collections.sort(array);             //从大到小排序
        int randomNewsNum = randomNewsNum(readNewsNum);
        JSONArray randomNewsArray = getRandomNews(randomNewsNum);

        for(int i = 0; i < recommendSize - randomNewsNum; i++){
            recommendArray.put(array.get(i).newsSketch);
        }
        for(int i = 0; i < randomNewsNum; i++){
            recommendArray.put(randomNewsArray.get(i));
        }

        JSONObject newsObj = new JSONObject();
        newsObj.put("list", recommendArray);
        newsObj.put("pageNo", 1);
        newsObj.put("pageSize", recommendSize);
        newsObj.put("totalPages", 1);
        newsObj.put("totalRecords", recommendSize);
        return newsObj;
    }

}
