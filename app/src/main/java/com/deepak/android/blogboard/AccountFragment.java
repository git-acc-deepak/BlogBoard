package com.deepak.android.blogboard;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private String userId;
    private FirebaseAuth mAuth;
    private TextView userAuthEmail;
    private CircleImageView userImage;
    private TextView userName;
    private TextView userBio;
    private FirebaseFirestore db;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_account, container, false);

        ImageView editSettings = view.findViewById(R.id.edit_account_pencil);
        Button logoutAction = view.findViewById(R.id.account_logout);
        userAuthEmail = view.findViewById(R.id.user_auth_email);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            userId = user.getUid();
            String email = user.getEmail();
            userAuthEmail.setText(email);
        }

        editSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountSetupIntent = new Intent(getContext(),AccountSetup.class);
                startActivity(accountSetupIntent);
            }
        });

        logoutAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Map<String, Object> tokenRemove = new HashMap<>();
                tokenRemove.put("token", FieldValue.delete());
                db.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent logout = new Intent(getContext(), LoginActivity.class);
                        startActivity(logout);
                        getActivity().finish();
                    }
                });
            }
        });

         userImage = view.findViewById(R.id.account_user_image_view);
         userName = view.findViewById(R.id.account_user_name_text_view);
         userBio = view.findViewById(R.id.user_bio_text_view);

        //setting initials to user account tab.
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().exists()){
                                String name = task.getResult().getString("name");
                                String bio = task.getResult().getString("bio");
                                String image = task.getResult().getString("image");

                                userName.setText(name);
                                userBio.setText(bio);

                                Glide.with(AccountFragment.this).
                                        asBitmap().load(image)
                                        .placeholder(R.drawable.post_user_placeholder)
                                        .into(userImage);
                            }
                        }
                    }
                });

        return view;
    }

}
