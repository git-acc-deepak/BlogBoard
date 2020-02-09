package com.deepak.android.blogboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AddPostActivity extends AppCompatActivity {

    private ImageView choosedImage;
    private EditText newPostDesc;
    private Button addNewPost;
    private Uri postImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        newPostDesc = findViewById(R.id.post_desc);
        addNewPost = findViewById(R.id.add_post_button);
        choosedImage = findViewById(R.id.selected_post_image);
        ImageButton addImage = findViewById(R.id.add_images_to_post);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }
    //ask user to select image to add to post.
    public void chooseImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2, 1)
                .setMinCropResultSize(512,256)
                .start(AddPostActivity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                choosedImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                String errorMessage = error.getMessage();
                Toast.makeText(AddPostActivity.this, errorMessage,Toast.LENGTH_LONG).show();
            }
        }

    }
}
