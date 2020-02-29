package com.deepak.android.blogboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Purpose of this class is to add a animated splash screen
 */

public class SplashActivity extends AppCompatActivity {

    Animation logoAnimation;
    ImageView splashImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashImage = findViewById(R.id.splash_image);

        logoAnimation = AnimationUtils.loadAnimation(this,R.anim.from_right);
        logoAnimation.setRepeatCount(2);

        logoAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent home = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(home);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        splashImage.startAnimation(logoAnimation);

    }

}
