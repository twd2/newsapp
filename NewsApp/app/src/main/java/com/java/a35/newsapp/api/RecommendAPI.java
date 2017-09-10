package com.java.a35.newsapp.api;

import android.content.Context;

import com.java.a35.newsapp.App;
import com.java.a35.newsapp.storage.StorageDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * Created by wuhaozhe on 2017/9/5.
 */

public class RecommendAPI {

    public static final int RANDOM_SAMPLE_NUM = 10; // 每次各个种类中随机抽样的数量
    public static final int KEYWORD_SEARCH_SAMPLE = 10; // 每次根据KEYWORD进行一次Search（不限category）,得到样本的数量
    public static final int KEYWORDS_NUM = 5; // 每次进行search的keyword的数量
    public static final int RANDOM_MIN = 2; // 完全随机新闻最小数量
    public static final int HISTORY_NEWS_NUM = 5; // 与推荐相关的最近的历史记录

    // 进行推荐的方法大致如下：每次分别从完全随机的新闻和根据关键字搜索的新闻选出一部分进行推荐

    private final int recommendSize; // 每次推荐的新闻数量
    public App app;

    public RecommendAPI(Context context) {
        this.app = (App)context.getApplicationContext();
        recommendSize = NewsAPI.DEFAULT_PAGE_SIZE;
    }

    // 完全随机的新闻占推荐新闻的比重(输入为阅读的新闻数量，随着新闻数量的增加，比重越来越低)
    private int randomNewsNum(int readNewsNum) {
        int randomNum = (int)(recommendSize * Math.pow(2.0, -readNewsNum));
        randomNum = Math.max(randomNum, RANDOM_MIN);
        return randomNum;
    }

    // readNewsNum是用户已经阅读的新闻数量
    private JSONArray getRandomNews(int requiredNum) throws IOException, JSONException {
        NewsAPI newsApi = app.getNewsApi();
        JSONArray categories = new JSONArray();
        Random rn = new Random();
        for (int i = NewsAPI.CATEGORY_MIN; i <= NewsAPI.CATEGORY_MAX; i++) {
            // TODO(twd2): NewsAPI bug
            // in order to avoid server's bug
            int randomPage = rn.nextInt(RANDOM_SAMPLE_NUM - 1) + 1;
            randomPage += 1;
            JSONObject list = newsApi.getListNews(i, randomPage, RANDOM_SAMPLE_NUM);
            categories.put(list); // 首先将所有种类的新闻放入all
        }

        Set<String> newsIdSet = new HashSet<>();
        JSONArray result = new JSONArray();
        // TODO(twd2): infinity loop?
        while (result.length() < requiredNum) {
            int randomCategory = rn.nextInt(NewsAPI.CATEGORY_MAX - NewsAPI.CATEGORY_MIN + 1);
            JSONArray list = categories.getJSONObject(randomCategory).getJSONArray("list");
            int randomIndex = rn.nextInt(list.length());
            String newsID = list.getJSONObject(randomIndex).getString("news_ID");
            if (!newsIdSet.contains(newsID)) {
                newsIdSet.add(newsID);
                result.put(list.getJSONObject(randomIndex));
            }
        }
        return result;
    }

    private float getNewsScore(Map<String, Float> historyKeywords, JSONObject newsDetail)
            throws JSONException {
        JSONArray newsKeywords = newsDetail.getJSONArray("Keywords");
        float sum = 0;
        for (int i = 0; i < newsKeywords.length(); i++) {
                String keyword = newsKeywords.getJSONObject(i).getString("word");
                if (historyKeywords.containsKey(keyword)) {
                    sum += historyKeywords.get(keyword) *
                            newsKeywords.getJSONObject(i).getDouble("score");
                }
        }
        return sum;
    }

    public static class NewsAndScore implements Comparable<NewsAndScore> {
        public JSONObject newsIntro; // 新闻
        public float score; // 新闻的得分

        NewsAndScore(JSONObject newsIntro, float score) {
            this.newsIntro = newsIntro;
            this.score = score;
        }

        @Override
        public int compareTo(NewsAndScore another) {
            return (int)(another.score - this.score);
        }
    }

    private Map<String, Float> calcKeywordsScore(JSONArray historyNews)
            throws JSONException {
        Map<String, Float> keywordScore = new HashMap<>();
        for (int i = 0; i < historyNews.length(); i++) {
            JSONArray keywords = historyNews.getJSONObject(i).getJSONArray("Keywords");
            for (int j = 0; j < keywords.length(); j++) {
                String word = keywords.getJSONObject(j).getString("word");
                float score = (float) keywords.getJSONObject(j).getDouble("score");
                if (keywordScore.containsKey(word)) {
                    keywordScore.put(word, keywordScore.get(word) + score);
                } else {
                    keywordScore.put(word, score);
                }
            }
        }
        return keywordScore;
    }

    // 获得推荐的新闻
    public JSONObject getRecommendedNews(int page) throws IOException, JSONException {
        if (page >= 2) {
            JSONObject obj = new JSONObject();
            obj.put("list", new JSONArray());
            obj.put("noMore", true);
            return obj;
        }

        NewsAPI newsApi = app.getNewsApi();
        StorageDbHelper db = app.getDb();
        JSONArray historyNews = db.getListHistory(1, HISTORY_NEWS_NUM).getJSONArray("list");

        // 计算读者的关键字分数
        Map<String, Float> keywordsScore = calcKeywordsScore(historyNews);
        ArrayList<Map.Entry<String, Float>> keywordScorePairs = new ArrayList<>(keywordsScore.entrySet());
        Collections.sort(keywordScorePairs, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }});

        // 选出一些分数大的关键字
        String[] topKeywords = new String[Math.min(KEYWORDS_NUM, keywordsScore.size())];
        for (int i = 0; i < topKeywords.length; i++) {
            topKeywords[i] = keywordScorePairs.get(i).getKey();
        }

        // 获得新闻并计算它们与读者喜好的相关度
        ArrayList<NewsAndScore> candidates = new ArrayList<>();
        for (int i = 0; i < topKeywords.length; i++) {
            JSONArray keywordNews = newsApi.searchAllNews(topKeywords[i], 1, KEYWORD_SEARCH_SAMPLE)
                    .getJSONArray("list");
            for (int j = 0; j < keywordNews.length(); j++) {
                JSONObject newsIntro = keywordNews.getJSONObject(j);
                String newsId = newsIntro.getString("news_ID");
                if (db.getHistory(newsId) == null) {
                    JSONObject newsDetail = newsApi.getNews(newsIntro.getString("news_ID"));
                    float score = getNewsScore(keywordsScore, newsDetail);
                    candidates.add(new NewsAndScore(newsIntro, score));
                }
            }
        }
        // 从大到小排序
        Collections.sort(candidates);

        // 决定推荐新闻、随机新闻的数量
        int randomNewsNum = Math.max(randomNewsNum(historyNews.length()),
                recommendSize - candidates.size());

        // 获得推荐新闻
        JSONArray recommendedNews = new JSONArray();
        for (int i = 0; i < recommendSize - randomNewsNum; i++) {
            recommendedNews.put(candidates.get(i).newsIntro);
        }

        // 获得随机新闻
        JSONArray randomNews = getRandomNews(randomNewsNum);
        for (int i = 0; i < randomNews.length(); i++) {
            recommendedNews.put(randomNews.get(i));
        }

        JSONObject newsObj = new JSONObject();
        newsObj.put("list", recommendedNews);
        newsObj.put("pageNo", 1);
        newsObj.put("pageSize", recommendSize);
        newsObj.put("totalPages", 1);
        newsObj.put("totalRecords", recommendSize);
        return newsObj;
    }

}
