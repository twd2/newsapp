package com.java.a35.newsapp;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SetBlockListActivity extends AppCompatActivity {
    TreeMap<String, Boolean> mBlockMap;
    ArrayList<String> mBlockList;

    void loadBlockListStatus(){
        Set<String> blockSet =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
            .getStringSet("block_list", new HashSet<String>());
        for (String blockSetItem : blockSet)
            if (mBlockMap.containsKey(blockSetItem))
                mBlockMap.put(blockSetItem, true);
    }

    void writeBlockListStatus(){
        Set<String> blockSet =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
            .getStringSet("block_list", new HashSet<String>());
        for (Map.Entry<String, Boolean> blockListItem: mBlockMap.entrySet()){
            if (blockListItem.getValue())
                blockSet.add(blockListItem.getKey());
            else
                blockSet.remove(blockListItem.getKey());
        }
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
            .edit().putStringSet("block_list", blockSet).apply();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setblocklist);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayList<String> keywordsList =
            (ArrayList<String>) getIntent().getSerializableExtra("keywords");
        mBlockMap = new TreeMap<String, Boolean>();
        for (String key: keywordsList)
            mBlockMap.put(key, false);
        loadBlockListStatus();
        mBlockList = new ArrayList<String>();
        for (Map.Entry<String, Boolean> entry: mBlockMap.entrySet())
            mBlockList.add(entry.getKey());
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.blockListView);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new KeywordsRecyclerViewAdapter());
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_banlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.home:
                finish();
                return true;
            case R.id.app_bar_exit:
                writeBlockListStatus();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class KeywordsRecyclerViewAdapter
        extends RecyclerView.Adapter<KeywordsRecyclerViewAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.block_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final String keyWord = mBlockList.get(position);
            holder.mKeyword = keyWord;
            holder.mKeywordView.setText(keyWord);
            holder.setButtonStatus(mBlockMap.get(keyWord));
            holder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean current_status = mBlockMap.get(keyWord);
                    mBlockMap.put(keyWord, !current_status);
                    holder.setButtonStatus(!current_status);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mBlockList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            final Pair<String, Boolean> mItem = new Pair<String, Boolean>("", false);
            final View mView;
            final TextView mKeywordView;
            final Button mButton;
            String mKeyword = "";
            ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mKeywordView = (TextView) mView.findViewById(R.id.keywordView);
                mButton = (Button) mView.findViewById(R.id.banButton);
            }
            void setButtonStatus(Boolean status){
                if (status){
                    mButton.setText(R.string.unblock);
                    //mButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    //mButton.setTextColor(getResources().getColor(R.color.background_holo_light));
                }
                else {
                    mButton.setText(R.string.block);
                    //mButton.setBackgroundColor(getResources().getColor(R.color.background_holo_light));
                    //mButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        }
    }
}
