package com.deepak.android.blogboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * purpose of this activity is to take user feedback
 */
public class FeedbackActivity extends AppCompatActivity {

    private String ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        String userName;
        EditText feedbackText = findViewById(R.id.feedback_text);
         ft = feedbackText.getText().toString();

        Button sendButton = findViewById(R.id.send_feedback);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"deepak.oo8@outlook.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT,"Blog Board Feedback" );
                intent.putExtra(Intent.EXTRA_TEXT, ft);
                if (intent.resolveActivity ( getPackageManager () ) != null) {
                    startActivity ( intent );
                }
            }
        });
    }
}
