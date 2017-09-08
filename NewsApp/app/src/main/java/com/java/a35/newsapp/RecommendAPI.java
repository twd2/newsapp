package com.java.a35.newsapp;

import android.content.Context;

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
    public static final int KEYWORDS_NUM = 3; // 每次进行search的keyword的数量
    public static final int RANDOM_MIN = 2; // 完全随机新闻最小数量

    // 进行推荐的方法大致如下：每次分别从完全随机的新闻和根据关键字搜索的新闻选出一部分进行推荐

    private int recommendSize; // 每次推荐的新闻数量
    public App app;

    public RecommendAPI(Context context) {
        this.app = (App)context.getApplicationContext();
        recommendSize = API.DEFAULT_PAGE_SIZE;
    }

    private int randomNewsNum(int readNewsNum) { // 完全随机的新闻占推荐新闻的比重(输入为阅读的新闻数量，随着新闻数量的增加，比重越来越低)
        int randomNum = (int)(recommendSize * Math.pow(2.0, -readNewsNum));
        randomNum = Math.max(randomNum, RANDOM_MIN);
        return randomNum;
    }

    private JSONArray getRandomNews(int newsNum) throws IOException, JSONException { // readNewsNum是用户已经阅读的新闻数量
        API api = app.getApi();
        JSONArray all = new JSONArray();
        Random rn = new Random();
        for (int i = API.CATEGORY_MIN; i <= API.CATEGORY_MAX; i++) {
            // TODO(twd2): API bug
            int randomPage = rn.nextInt(RANDOM_SAMPLE_NUM - 1) + 1; // in order to avoid server's bug
            randomPage += 1;
            JSONObject listNews = api.getListNews(i, randomPage, RANDOM_SAMPLE_NUM);
            all.put(listNews); // 首先将所有种类的新闻放入all
        }

        HashSet<String> newsIdSet = new HashSet<>();
        JSONArray array = new JSONArray();
        int counter = 0;
        while (counter < newsNum) {
            int randomCategory = rn.nextInt(API.CATEGORY_MAX - API.CATEGORY_MIN + 1);
            JSONArray currentListNews = (all.getJSONObject(randomCategory)).getJSONArray("list");
            int randomIndex = rn.nextInt(currentListNews.length());
            String newsID = (currentListNews.getJSONObject(randomIndex)).getString("news_ID");
            if (!newsIdSet.contains(newsID)) {
                newsIdSet.add(newsID);
                array.put(currentListNews.getJSONObject(randomIndex));
                counter++;
            }
        }
        return array;
    }

    private float getScore(Hashtable<String, Float> history, JSONArray target)
            throws JSONException {
        float sum = 0;
        for (int i = 0; i < target.length(); i++) {
                String keyword = (target.getJSONObject(i)).getString("word");
                if (history.containsKey(keyword)) {
                    sum += (history.get(keyword) * (target.getJSONObject(i)).getDouble("score"));
                }
        }
        return sum;
    }

    public static class NewsAndScore implements Comparable<NewsAndScore> {
        public JSONObject newsIntro; // 新闻
        public float score;           // 新闻的得分

        NewsAndScore(JSONObject newsIntro, float score) {
            this.newsIntro = newsIntro;
            this.score = score;
        }

        @Override
        public int compareTo(NewsAndScore another) {
            return (int)(another.score - this.score);
        }
    }

    private Hashtable<String, Float> generateWordScoreTable(JSONArray historyNews)
            throws JSONException {
        Hashtable<String, Float> table = new Hashtable<>();
        for (int i = 0; i < historyNews.length(); i++) {
            JSONArray keyWords = (historyNews.getJSONObject(i)).getJSONArray("Keywords");
            for (int j = 0; j < keyWords.length(); j++) {
                String word = keyWords.getJSONObject(j).getString("word");
                float score = (float) keyWords.getJSONObject(j).getDouble("score");
                if (table.containsKey(word)) {
                    table.put(word, table.get(word) + score);
                } else {
                    table.put(word, score);
                }
            }
        }
        return table;
    }

    public JSONObject getRecommendNews(int page) throws IOException, JSONException { // 返回推荐的新闻
        if (page >= 2) {
            // FIXME
            return new JSONObject();
        }

        API api = app.getApi();
        StorageDbHelper storageDbHelper = app.getDb();
        JSONArray historyNews = storageDbHelper.getListHistory(1).getJSONArray("list");

        int readNewsNum = historyNews.length();
        Hashtable<String, Float> wordScoreMap = generateWordScoreTable(historyNews);
        ArrayList<Map.Entry<String, Float>> arrayList = new ArrayList<>(wordScoreMap.entrySet());
        Collections.sort(arrayList, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }});
        String[] topWords = new String[Math.min(KEYWORDS_NUM, wordScoreMap.size())];

        for (int i = 0; i < topWords.length; i++) {
            topWords[i] = arrayList.get(i).getKey();
        }

        ArrayList<NewsAndScore> array = new ArrayList<>();

        for (int i = 0; i < topWords.length; i++) {
            JSONArray allWordNews = api.searchAllNews(topWords[i], 1, KEYWORD_SEARCH_SAMPLE)
                    .getJSONArray("list");
            for (int j = 0; j < allWordNews.length(); j++) {
                JSONObject newsIntro = allWordNews.getJSONObject(j);
                JSONObject newsDetail = api.getNews(newsIntro.getString("news_ID"));
                float score = getScore(wordScoreMap, newsDetail.getJSONArray("Keywords"));
                array.add(new NewsAndScore(newsIntro, score));
            }
        }

        JSONArray recommendArray = new JSONArray();
        Collections.sort(array); // 从大到小排序
        int randomNewsNum = randomNewsNum(readNewsNum);
        JSONArray randomNewsArray = getRandomNews(randomNewsNum);

        for (int i = 0; i < recommendSize - randomNewsNum; i++) {
            recommendArray.put(array.get(i).newsIntro);
        }
        for (int i = 0; i < randomNewsNum; i++) {
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
