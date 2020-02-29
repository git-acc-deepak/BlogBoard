package com.deepak.android.blogboard;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

    private EditText commentText;
    private ImageView sendCommentButton;
    private String blog_post_id;
    private String blog_user_id;

    private RecyclerView commentListAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ImageView userDp;
    private TextView userDisplayName;
    private TextView postDescText;
    private ImageView postImage;
    private TextView timestamp;


    private CommentsAdapter commentsAdapter;
    private List<CommentsModel> commentsList;
    private List<User> commenterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        userDisplayName = findViewById(R.id.publishers_name);
        userDp = findViewById(R.id.publishers_image);
        postDescText = findViewById(R.id.desc_of_the_post);
        postImage = findViewById(R.id.image_of_the_post);
        timestamp = findViewById(R.id.post_date);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            currentUserId = user.getUid();
        }

        blog_post_id = getIntent().getStringExtra("blog_post_id");
/*        blog_user_id = getIntent().getStringExtra("blog_user_id");*/

        //user data retrieval
       /* db.collection("Users").document(blog_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                     if (task.getResult().exists()){
                         String name = task.getResult().getString("name");
                         userDisplayName.setText(name);
                         //post date

                         String image = task.getResult().getString("image");
                         Glide.with(getBaseContext())
                                 .asBitmap()
                                 .load(image)
                                 .placeholder(R.drawable.post_user_placeholder)
                                 .into(userDp);
                     }
                }
            }
        });
        //post data retrieval
        db.collection("Posts").document(blog_post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String desc = task.getResult().getString("desc");
                        postDescText.setText(desc);
                        String imageUrl = task.getResult().getString("image_url");
                        Glide.with(getBaseContext()).asBitmap().load(imageUrl)
                                .placeholder(R.drawable.post_placeholder)
                                .into(postImage);
                    }
                }
            }
        });
*/
        commentText = findViewById(R.id.comment_input_text);
        sendCommentButton = findViewById(R.id.comment_post_button);
        commentListAdapter = findViewById(R.id.comment_list_recycler_view);

        //database comment list
        commenterId = new ArrayList<>();
        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentsList, commenterId);
        commentListAdapter.setHasFixedSize(true);
        commentListAdapter.setLayoutManager(new LinearLayoutManager(this));
        commentListAdapter.setAdapter(commentsAdapter);

        //comment list retrieval
        db.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                        commenterId.clear();
                        commentsList.clear();
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            final String commentId = doc.getDocument().getId();
                            final CommentsModel comments = doc.getDocument().toObject(CommentsModel.class);
                            String blogUserId = doc.getDocument().getString("user_id");
                            db.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        User user = task.getResult().toObject(User.class);
                                        commenterId.add(user);
                                        commentsList.add(comments);
                                        commentsAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

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
