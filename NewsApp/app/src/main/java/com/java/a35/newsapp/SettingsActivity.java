package com.java.a35.newsapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity{
    private SharedPreferences sharedPreference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setupShowPicturesSettings();
        setupNightModeSettings();
        setupBlockingSettings();
    }

    private void setupShowPicturesSettings() {
        final Switch showPictures = (Switch)findViewById(R.id.show_pictures_switch);
        showPictures.setChecked(sharedPreference.getBoolean("show_pictures", true));
        showPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean checked = ((Switch)v).isChecked();
                sharedPreference.edit().putBoolean("show_pictures", checked).apply();
            }
        });
    }

    private void setupNightModeSettings() {
        final Switch nightMode = (Switch)findViewById(R.id.night_mode_switch);
        nightMode.setChecked(sharedPreference.getBoolean("night_mode", false));
        nightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean checked = ((Switch)v).isChecked();
                if (checked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                //getDelegate().applyDayNight();
                sharedPreference.edit().putBoolean("night_mode", checked).apply();
            }
        });
    }

    private void setupBlockingSettings() {
        final Button blockingSettings = (Button)findViewById(R.id.blocking_settings_button);
        blockingSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        new Intent(SettingsActivity.this, BlockSettingsActivity.class);
                Set<String> blockSet =
                        getApplicationContext()
                                .getSharedPreferences(BlockSettingsActivity.PREFERENCES_BLOCK,
                                Context.MODE_PRIVATE)
                                .getStringSet("block_list", new HashSet<String>());
                intent.putExtra("keywords", new ArrayList<String>(blockSet));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
