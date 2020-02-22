package com.deepak.android.blogboard;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * purpose of this activity is to view and post comment to a blog post.
 */
public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar ;
    private EditText commentText;
    private ImageView sendCommentButton;
    private String blog_post_id;

    private RecyclerView commentListAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private CommentsAdapter commentsAdapter;
    private List<CommentsModel> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        blog_post_id = getIntent().getStringExtra("blog_post_id");

        commentText = findViewById(R.id.comment_input_text);
        sendCommentButton = findViewById(R.id.comment_post_button);
        commentListAdapter = findViewById(R.id.comment_list_recycler_view);

        //database comment list
        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentsList);
        commentListAdapter.setHasFixedSize(true);
        commentListAdapter.setLayoutManager(new LinearLayoutManager(this));
        commentListAdapter.setAdapter(commentsAdapter);

        //comment list retrieval
        db.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String commentId = doc.getDocument().getId();
                            CommentsModel comments = doc.getDocument().toObject(CommentsModel.class);
                            commentsList.add(comments);
                            commentsAdapter.notifyDataSetChanged();


                        }
                    }

                }

            }
        });

        //setting up comment post button to post comment.
        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment_text = commentText.getText().toString();
                if (!comment_text.isEmpty()){

                    Map<String,Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment_text);
                    commentMap.put("user_id", currentUserId);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("Posts/" + blog_post_id + "/Comments")
                            .add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()){

                                commentText.setText("");

                            } else {

                                Toast.makeText(CommentsActivity.this,
                                        "Error Posting Comment" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
