package com.deepak.android.blogboard;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * purpose of this activity is to view people's profile and send let user to send message to them.
 * on clicking to a user it will take us to the send message activity where
 * we can view previous chats as well as send new messages to the user.
 */
public class PeopleActivity extends AppCompatActivity {

    private static final String TAG = "PeopleActivity";
    private RecyclerView mPeopleListViewer;
    private List<User> mPeople;
    private UsersAdapter mPeopleAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        //toolbar
        Toolbar mToolbar = findViewById(R.id.people_toolbar);
        setSupportActionBar(mToolbar);

        //database server instance.
        db = FirebaseFirestore.getInstance();

        //initializing list details.
        mPeopleListViewer = findViewById(R.id.view_people_list);
        mPeopleListViewer.setLayoutManager(new LinearLayoutManager(this));
        mPeople = new ArrayList<>();
        mPeopleAdapter = new UsersAdapter(this, mPeople);
        mPeopleListViewer.setHasFixedSize(true);
        mPeopleListViewer.setAdapter(mPeopleAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mPeople.clear();
        db.collection("Users").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Error:" + e.getMessage());
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    //checking if the new data added
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String userId = doc.getDocument().getId();
                            User users = doc.getDocument().toObject(User.class).withId(userId);
                            mPeople.add(users);
                            mPeopleAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
