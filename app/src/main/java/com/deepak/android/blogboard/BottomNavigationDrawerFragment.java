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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * purpose of this fragment class is to display a drawer on navigation button.
 */
public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    private FirebaseAuth mAuth;
    private String userName;
    private String displayImage;
    private ImageView dp;
    private TextView displayName;
    private ListenerRegistration lr;

    @Override
    public void onStop() {
        super.onStop();
        lr.remove();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.fragment_bottomsheet,container,false);

        NavigationView mNavView = view.findViewById(R.id.navigation_drawer_view);
        TextView userEmail = view.findViewById(R.id.drawer_user_email);
        displayName = view.findViewById(R.id.drawer_user_name);
        dp = view.findViewById(R.id.drawer_user_image);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //handling on list item clicks
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.action_about:
                        Intent about = new Intent(getContext(), AboutActivity.class);
                        startActivity(about);
                        break;

                    case R.id.action_view_all_users:
                        Intent viewPeople = new Intent(getContext(), PeopleActivity.class);
                        startActivity(viewPeople);
                        break;

                    case R.id.action_settings:
                        Intent settingIntent = new Intent(getContext(),AccountSetup.class);
                        startActivity(settingIntent);
                        break;

                    case R.id.action_feedback:
                        Intent feedback = new Intent(getContext(),FeedbackActivity.class);
                        startActivity(feedback);
                        break;
                }
                BottomNavigationDrawerFragment.super.dismiss();
                return false;
            }
        });

        //setting drawer current user details
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            //getting details
            String email = user.getEmail();
            String userId = user.getUid();
            //setting details
            lr = db.collection("Users").document(userId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    if (documentSnapshot.exists() && documentSnapshot != null) {
                        userName = documentSnapshot.getString("name");
                        displayName.setText(userName);
                        displayImage = documentSnapshot.getString("image");
                        Glide.with(BottomNavigationDrawerFragment.this).load(displayImage).into(dp);
                    }
                }
            });
            userEmail.setText(email);
        }
          return view;
    }
}
