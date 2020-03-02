package com.deepak.android.blogboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * purpose of this fragment class is to display a drawer on navigation button.
 */
public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    private FirebaseAuth mAuth;
    private String userName;
    private String displayImage;
    private ImageView dp;
    private TextView displayName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.fragment_bottomsheet,container,false);

        NavigationView mNavView = view.findViewById(R.id.navigation_drawer_view);
        TextView userEmail = view.findViewById(R.id.drawer_user_email);
          displayName = view.findViewById(R.id.drawer_user_name);
          dp = view.findViewById(R.id.drawer_user_image);

        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.account_logout:
                        logout();
                        return true;

                    case R.id.action_settings:
                        Intent settingIntent = new Intent(getContext(),AccountSetup.class);
                        startActivity(settingIntent);
                        return true;

                    case R.id.action_feedback:
                        Intent feedback = new Intent(getContext(),FeedbackActivity.class);
                        startActivity(feedback);
                        return true;
                }
                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            //getting details
            String email = user.getEmail();
            String userId = user.getUid();
            //setting details
            db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            userName = task.getResult().getString("name");
                            displayName.setText(userName);
                            displayImage = task.getResult().getString("image");
                            Glide.with(BottomNavigationDrawerFragment.this).load(displayImage).into(dp);
                        }
                    }
                }
            });
            userEmail.setText(email);
        }
          return view;
    }


    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(getContext(),LoginActivity.class);
        startActivity(intent);
    }
}
