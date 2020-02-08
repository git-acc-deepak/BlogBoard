package com.deepak.android.blogboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSetup extends AppCompatActivity {

    private CircleImageView setImage;
    private EditText nameText;
    private Button saveSettingsButton;
    private ProgressBar settingUpdateProgress;
    private Uri mainImageUri = null;
    private String userId;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        Toolbar accountSettingsToolbar = findViewById(R.id.account_setup_toolbar);
        setSupportActionBar(accountSettingsToolbar);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();


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
                if (task.isSuccessful()){
                    //checking if the data even exist on the cloud.
                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        nameText.setText(name);
                        //placeholder until the image is loaded
                        RequestOptions placeHolder = new RequestOptions();
                        placeHolder.placeholder(R.drawable.profile_picture);

                        Glide.with(AccountSetup.this).setDefaultRequestOptions(placeHolder).load(image).into(setImage);

                    }else {
                        Toast.makeText(AccountSetup.this,"Data doesn't exist" , Toast.LENGTH_LONG).show();
                    }

                }else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountSetup.this,"Database Retrieval ERROR:" + error, Toast.LENGTH_LONG).show();
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
                //checking if any field are empty.
                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {
                    userId = mAuth.getCurrentUser().getUid();
                    settingUpdateProgress.setVisibility(View.VISIBLE);

                    final StorageReference imagePath = mStorageRef.child("profile_images").child(userId + ".jpg");
                    imagePath.putFile(mainImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //getting the image download URL
                                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    Map<String , String> userMap = new HashMap<>();
                                    userMap.put("name",userName);
                                    userMap.put("image", downloadUri.toString());
                                    //uploading to database collection.
                                    db.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

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
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error = "Ops! Something went wrong. Unable to upload the image at this moment.Please try again later";
                            Toast.makeText(AccountSetup.this, error , Toast.LENGTH_LONG).show();
                            settingUpdateProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }


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

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
