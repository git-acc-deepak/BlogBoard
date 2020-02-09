package com.deepak.android.blogboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private static final String TAG = "AddPostActivity";

    private ImageView choosedImage;
    private EditText newPostDesc;
    private Button addNewPost;
    private Uri postImageUri = null;
    private ProgressBar newPostProgress;
    private String currentUserId;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Toolbar newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        newPostDesc = findViewById(R.id.post_desc);
        addNewPost = findViewById(R.id.add_post_button);
        choosedImage = findViewById(R.id.selected_post_image);
        newPostProgress = findViewById(R.id.new_post_progress);

        ImageButton addImage = findViewById(R.id.add_images_to_post);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //Uploading task.
        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc = newPostDesc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null){

                    newPostProgress.setVisibility(View.VISIBLE);

                    String randName = FieldValue.serverTimestamp().toString();
                    final StorageReference filePath = mStorageRef.child("post_images").child(randName + ".jpg");
                    filePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   Uri downloadUri = uri;

                            //database storage task.
                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("image_url", downloadUri.toString());
                            postMap.put("desc", desc);
                            postMap.put("user_id", currentUserId);
                            postMap.put("timestamp", FieldValue.serverTimestamp());
                            db.collection("Posts").add(postMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(AddPostActivity.this,
                                                    "Posted Successfully", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(AddPostActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w( TAG, "Error writing document", e);
                                        }
                            });
                               }
                           });
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error = e.getCause().getMessage();
                            Toast.makeText(AddPostActivity.this, "Error:" + error , Toast.LENGTH_LONG).show();
                            newPostProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }
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
