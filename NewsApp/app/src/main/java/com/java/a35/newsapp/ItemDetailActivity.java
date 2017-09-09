package com.java.a35.newsapp;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.java.a35.newsapp.storage.StorageDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    private SpeechSynthesizer mTts;

    private Categories.NewsItem mItem;

    protected JSONObject mDetail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        initTts();

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.app_bar_favorite:
                        // TODO(twd2): async?
                        final StorageDbHelper db = ((App)getApplicationContext()).getDb();
                        db.setFavorite(mItem.obj, true);
                        Snackbar.make(getCurrentFocus(),
                                R.string.marked_as_favorite, Snackbar.LENGTH_LONG)
                                .setAction(R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        db.setFavorite(mItem.obj, false);
                                        Toast.makeText(ItemDetailActivity.this,
                                                R.string.canceled, Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                        break;
                    case R.id.app_bar_tts:
                        playTts();
                        break;
                    case R.id.app_bar_share:
                        doShare();
                        break;
                    case R.id.app_bar_block:
                        if (mDetail == null) break;
                        Intent intent =
                                new Intent(ItemDetailActivity.this, BlockSettingsActivity.class);
                        ArrayList<String> keywordsList = new ArrayList<>();
                        try {
                            ArrayList<Pair<Double, String> > candidatesList = new ArrayList<>();
                            Log.i("test", mDetail.toString());
                            JSONArray keywords = mDetail.getJSONArray("Keywords");
                            for (int i = 0; i < keywords.length(); ++ i) {
                                JSONObject kw = keywords.getJSONObject(i);
                                candidatesList.add(Pair.create(kw.getDouble("score"), kw.getString("word")));
                            }
                            Collections.sort(candidatesList, new Comparator<Pair<Double, String>>() {
                                @Override
                                public int compare(Pair<Double, String> o1, Pair<Double, String> o2) {
                                    return o1.first.compareTo(o2.first);
                                }
                            });
                            for (int i = 0; i < candidatesList.size() && i < 30; ++ i) {
                                keywordsList.add(candidatesList.get(i).second);
                            }
                        } catch (JSONException e){
                            Log.e("test", Log.getStackTraceString(e));
                        }
                        intent.putExtra("keywords", keywordsList);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Categories.CategoryType categoryType =
                Categories.CategoryType.valueOf(
                        getIntent().getStringExtra(ItemDetailFragment.ARG_CATEGORY));
        Categories categories = ((App)getApplicationContext()).getCategories();
        mItem = categories.categories.get(categoryType).map
                .get(getIntent().getStringExtra(ItemDetailFragment.ARG_NEWS_ID));

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_NEWS_ID,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_NEWS_ID));
            arguments.putString(ItemDetailFragment.ARG_CATEGORY,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_CATEGORY));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(savedInstanceState);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mTts != null) {
                    mTts.stopSpeaking();
                }
                finish();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mTts != null) {
                mTts.stopSpeaking();
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ItemDetailFragment.ARG_CATEGORY,
                           getIntent().getStringExtra(ItemDetailFragment.ARG_CATEGORY));
        outState.putString(ItemDetailFragment.ARG_NEWS_ID,
                getIntent().getStringExtra(ItemDetailFragment.ARG_NEWS_ID));
    }

    protected void doShare() {
        if (mDetail == null) {
            return;
        }
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            if (!mDetail.has("pictures_path")) {
                share.setType("text/plain");
            } else {
                JSONArray pictures = mDetail.getJSONArray("pictures_path");
                share.setType("image/*");
                File pictureFile = new File(pictures.getString(0));
                File tempFile = new File(((App)getApplicationContext()).getSharedDir(),
                        pictureFile.getName() + ".jpg");
                try {
                    Utility.copyFileUsingFileChannels(pictureFile, tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Uri uri = FileProvider.getUriForFile(this, "com.java.a35.newsapp", tempFile);

                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d("detail", "doShare: " + tempFile);
                Log.d("detail", "doShare: " + uri);
            }

            share.putExtra(Intent.EXTRA_TEXT, String.format("【%s】%s 原文链接：%s -- %s",
                    mItem.title, mItem.obj.getString("news_Intro"),
                    mItem.obj.getString("news_URL"),
                    getString(R.string.from_my_newsapp)));

            share.putExtra(Intent.EXTRA_TITLE, mItem.title);
            share.putExtra(Intent.EXTRA_SUBJECT, mItem.title);

            // extra for WeChat
            share.putExtra("Kdescription", share.getStringExtra(Intent.EXTRA_TEXT));
            startActivity(Intent.createChooser(share, getString(R.string.share_to_sns)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // TTS interfaces

    boolean isTtsPlaying = false;

    protected void initTts() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=59a78bf9");
        mTts = SpeechSynthesizer.createSynthesizer(ItemDetailActivity.this, mTtsInitListener);
        if (mTts != null) {
            mTts.setParameter(SpeechConstant.PITCH, "50");
        }
    }

    protected void playTts() {
        if (mItem.detail.equals("")) {
            return;
        }
        if (mTts != null) {
            if (!isTtsPlaying) {
                int code = mTts.startSpeaking(mItem.detail, mTtsListener);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(ItemDetailActivity.this,
                            getString(R.string.tts_failed) + code,
                            Toast.LENGTH_SHORT).show();
                } else {
                    isTtsPlaying = true;
                    Toast.makeText(ItemDetailActivity.this,
                            R.string.reading, Toast.LENGTH_SHORT).show();
                }
            } else {
                isTtsPlaying = false;
                mTts.stopSpeaking();
            }
        } else {
            Toast.makeText(ItemDetailActivity.this,
                    R.string.tts_engine_init_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("tts", "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d("tts", "初始化失败，错误码：" + code);
            }
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.d("tts", "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.d("tts", "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.d("tts", "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            isTtsPlaying = false;
            if (error == null) {
                Log.d("tts", "播放完成");
                Toast.makeText(ItemDetailActivity.this,
                        R.string.read_completed, Toast.LENGTH_SHORT).show();
            } else {
                Log.d("tts", error.getPlainDescription(true));
                Toast.makeText(ItemDetailActivity.this,
                        error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };
}
