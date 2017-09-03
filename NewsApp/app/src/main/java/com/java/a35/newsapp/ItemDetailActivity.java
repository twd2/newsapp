package com.java.a35.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.java.a35.newsapp.dummy.DummyContent;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    private SpeechSynthesizer mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        initTts();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.app_bar_favorite:
                        Snackbar.make(getCurrentFocus(), "已收藏", Snackbar.LENGTH_LONG)
                                .setAction("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(ItemDetailActivity.this,
                                                "已取消收藏", Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                        // TODO
//                        Drawable icon = Resources.getSystem().getDrawable(R.drawable.ic_favorite_border_white_24dp, null);
//                        item.setIcon(icon);
                        break;
                    case R.id.app_bar_tts:
                        playTts();
                        break;
                    case R.id.app_bar_share:
                        doShare();
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

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
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
            NestedScrollView scroll = (NestedScrollView)findViewById(R.id.item_detail_container);
            Log.d("scroll", "" + scroll.getWidth());
//            int scroll_y = savedInstanceState.getInt("scroll_y");
//            scroll.setScrollY(scroll_y / scroll.getHeight());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState != null) {
            NestedScrollView scroll = (NestedScrollView)findViewById(R.id.item_detail_container);
            Log.d("scroll", "" + scroll.getWidth());
        }
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
        outState.putString(ItemDetailFragment.ARG_ITEM_ID,
                           getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
        NestedScrollView scroll = (NestedScrollView)findViewById(R.id.item_detail_container);
        outState.putInt("scroll_y", scroll.getScrollY() * scroll.getHeight());
        Log.d("scroll", "" + scroll.getWidth());
    }

    protected void doShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        // TODO(twd2): image
        // share.putExtra(Intent.EXTRA_STREAM, Uri.parse("https://twd2.me/smile_photo.jpg"));
        // extra for WeChat
        share.putExtra("Kdescription", "测试描述 ——发自我的 NewsApp");
        DummyContent.NewsItem item = DummyContent.NEWS_MAP
                .get(getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
        share.putExtra(Intent.EXTRA_TEXT, item.content + " ——发自我的 NewsApp");
        startActivity(Intent.createChooser(share, "分享到社交网络"));
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
        if (mTts != null) {
            if (!isTtsPlaying) {
                // TODO(twd2): refactor
                DummyContent.NewsItem mItem = DummyContent.NEWS_MAP.get(
                        getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
                int code = mTts.startSpeaking(mItem.detail, mTtsListener);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(ItemDetailActivity.this,
                            "语音合成失败，错误码: " + code,
                            Toast.LENGTH_SHORT).show();
                } else {
                    isTtsPlaying = true;
                    Toast.makeText(ItemDetailActivity.this,
                            "朗读中...", Toast.LENGTH_SHORT).show();
                }
            } else {
                isTtsPlaying = false;
                mTts.stopSpeaking();
            }
        } else {
            Toast.makeText(ItemDetailActivity.this,
                    "合成失败", Toast.LENGTH_SHORT).show();
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
                        "播放完成", Toast.LENGTH_SHORT).show();
            } else if (error != null) {
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
