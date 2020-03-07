package com.deepak.android.blogboard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * purpose of this activity is to display the version and about section of app
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView buildVersion = findViewById(R.id.app_version);
        buildVersion.setText(BuildConfig.VERSION_NAME);
    }
}
