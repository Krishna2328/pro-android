package com.nativetemplate.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import pl.droidsonroids.gif.GifImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreenActivity extends AppCompatActivity {

    GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        gifImageView = (GifImageView) findViewById(R.id.gifImg);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent goto_login = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(goto_login);
                finish();
            }
        }, 5000);



    }

}
