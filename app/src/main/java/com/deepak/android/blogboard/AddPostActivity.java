package com.deepak.android.blogboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class AddPostActivity extends AppCompatActivity {

    private static final String TAG = "AddPostActivity";

    private ImageView choosedImage;
    private TextInputEditText newPostDesc;
    private TextInputEditText newPostTitle;
    private Uri postImageUri = null;
    private ProgressBar newPostProgress;
    private String currentUserId;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Toolbar newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        newPostTitle = findViewById(R.id.post_title);
        newPostDesc = findViewById(R.id.post_desc);
        Button addNewPost = findViewById(R.id.add_post_button);
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
                final String title = newPostTitle.getText().toString();

                if (!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(title) && postImageUri != null) {

                    newPostProgress.setVisibility(View.VISIBLE);

                    final String randName = UUID.randomUUID().toString();

                    final StorageReference filePath = mStorageRef.child("post_images").child(randName + ".jpg");
                    filePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    final String downloadUri = uri.toString();
                                    File newImageFile = new File(postImageUri.getPath());
                                    try {
                                        //thumbnail image size and quality.
                                        compressedImageFile = new Compressor(AddPostActivity.this)
                                                .setMaxHeight(100)
                                                .setMaxWidth(100)
                                                .setQuality(1)
                                                .compressToBitmap(newImageFile);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] thumbData = baos.toByteArray();

                                    UploadTask uploadTask = mStorageRef
                                            .child("post_images/thumbs").child(randName + ".jpg")
                                            .putBytes(thumbData);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            taskSnapshot.getStorage().getDownloadUrl()
                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String downloadThumbUri = uri.toString();

                                                    //database storage task.
                                                    Map<String, Object> postMap = new HashMap<>();
                                                    postMap.put("image_url", downloadUri);
                                                    postMap.put("thumbnail", downloadThumbUri);
                                                    postMap.put("title", title);
                                                    postMap.put("search", title.toLowerCase());
                                                    postMap.put("desc", desc);
                                                    postMap.put("user_id", currentUserId);
                                                    postMap.put("timestamp", FieldValue.serverTimestamp());
                                                    db.collection("Posts").add(postMap)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                                                            Log.w(TAG, "Error writing document", e);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            String error = e.getMessage();
                                            Toast.makeText(AddPostActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            });
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error = e.getCause().getMessage();
                            Toast.makeText(AddPostActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
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
                .setMinCropResultSize(512, 256)
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
                Toast.makeText(AddPostActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }

    }
}
