package com.deepak.android.blogboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSetup extends AppCompatActivity {

    private CircleImageView setImage;
    private TextInputEditText nameText;
    private TextInputEditText bioText;
    private Button saveSettingsButton;
    private ProgressBar settingUpdateProgress;
    private Uri mainImageUri = null;
    private String userId;
    private boolean isChanged = false;
    private String token_id;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        Toolbar accountSettingsToolbar = findViewById(R.id.account_setup_toolbar);
        setSupportActionBar(accountSettingsToolbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        bioText = findViewById(R.id.user_bio);
        setImage = findViewById(R.id.profile_image);
        nameText = findViewById(R.id.name_text);
        saveSettingsButton = findViewById(R.id.save_setting_button);
        settingUpdateProgress = findViewById(R.id.profile_update_progress);

        /*
         * retrieving uploaded data from the database to show it on the
         * app after the user setup the profile data.
         */
        settingUpdateProgress.setVisibility(View.VISIBLE);
        saveSettingsButton.setEnabled(false);
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //checking if the data even exist on the cloud.
                    if (task.getResult().exists()) {

                        String bio = task.getResult().getString("bio");
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        nameText.setText(name);
                        bioText.setText(bio);
                        //placeholder until the image is loaded
                        RequestOptions placeHolder = new RequestOptions();
                        placeHolder.placeholder(R.drawable.post_user_placeholder);

                        Glide.with(AccountSetup.this).setDefaultRequestOptions(placeHolder)
                                .asBitmap()
                                .load(image)
                                .into(setImage);

                    }

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetup.this, "Database Retrieval ERROR:" + error, Toast.LENGTH_LONG).show();
                }
                settingUpdateProgress.setVisibility(View.INVISIBLE);
                saveSettingsButton.setEnabled(true);
            }
        });

        //set profile picture.
        setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(AccountSetup.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(AccountSetup.this, "Permission Denied.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(AccountSetup.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        goToImageCropper();
                    }

                } else {
                    goToImageCropper();
                }
            }
        });

          /*
            Upload task to upload data when the user press the save setting button
            by filling the name and profile picture.
             */

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = nameText.getText().toString();
                final String userBio = bioText.getText().toString();
                //checking if any field are empty.
                if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userBio) && mainImageUri != null) {
                    settingUpdateProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        File newImageFile = new File(mainImageUri.getPath());

                        try {

                            compressedImageFile = new Compressor(AccountSetup.this)
                                    .setMaxHeight(125)
                                    .setMaxWidth(125)
                                    .setQuality(50)
                                    .compressToBitmap(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                        byte[] thumbData = baos.toByteArray();

                        final UploadTask uploadTask = mStorageRef.child("profile_images").child(userId + ".jpg").putBytes(thumbData);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //getting the image download URL
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        storeToDatabase(uri, userName, userBio);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String error = e.getMessage();
                                Toast.makeText(AccountSetup.this, "Error:" + error , Toast.LENGTH_LONG).show();
                                settingUpdateProgress.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {
                        storeToDatabase(null, userName, userBio);
                    }
                }

            }

        });
    }
    /*
     database storage task added to separate method to upload data only when it is changed
     and the method will be called only when the change occur.
     */
    private void storeToDatabase(Uri uri, final String userName, final String userBio) {
        final Uri downloadUri;
        if (uri != null) {
            downloadUri = uri;
        } else {
            downloadUri = mainImageUri;
        }
        token_id = FirebaseInstanceId.getInstance().getToken();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("bio", userBio);
        userMap.put("image", downloadUri.toString());
        userMap.put("token", token_id);
        //uploading to database collection.
        db.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(AccountSetup.this, "Settings Updated.", Toast.LENGTH_LONG).show();
                    Intent homeIntent = new Intent(AccountSetup.this, MainActivity.class);
                    startActivity(homeIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetup.this, "Database ERROR:" + error, Toast.LENGTH_LONG).show();
                }
                settingUpdateProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    //use of external image cropper library.
    private void goToImageCropper() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AccountSetup.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setImage.setImageURI(mainImageUri);
                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                String errorMessage = error.getMessage();
                Toast.makeText(AccountSetup.this, errorMessage,Toast.LENGTH_LONG).show();
            }
        }
    }

}
