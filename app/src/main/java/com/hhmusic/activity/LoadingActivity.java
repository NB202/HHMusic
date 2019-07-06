package com.hhmusic.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hhmusic.R;

public class LoadingActivity extends Activity {


    private static final long SPLASH_DELAY_MILLIS = 1600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                goHome();
            }
        }, SPLASH_DELAY_MILLIS);
    }

    private void goHome() {
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);

        LoadingActivity.this.startActivity(intent);
        LoadingActivity.this.finish();
    }
}
