package com.deepak.android.blogboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Objects;

/**
 * purpose of this activity is to view and post comment to a blog post.
 */
public class CommentsActivity extends AppCompatActivity {

    private EditText commentText;
    private String blog_post_id;
    private String postsAuthorId;

    private FirebaseFirestore db;
    private String currentUserId;

    private ImageView userDp;
    private TextView userDisplayName;
    private TextView postDescText;
    private ImageView postImage;
    private TextView timestamp;

    private ImageView likeButton;
    private ImageView deleteButton;
    private TextView likeCount;

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
        likeButton = findViewById(R.id.is_the_post_liked);
        deleteButton = findViewById(R.id.delete_this_post);
        likeCount = findViewById(R.id.like_count_field);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            currentUserId = user.getUid();
        }
        if (getIntent().hasExtra("blog_post_id") ) {
            blog_post_id = getIntent().getStringExtra("blog_post_id");
        }

        //post data retrieval
           db.collection("Posts").document(blog_post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if (task.isSuccessful()){
                       if (Objects.requireNonNull(task.getResult()).exists()){
                           String desc = task.getResult().getString("desc");
                           postDescText.setText(desc);
                           //set post date
                           long time = task.getResult().getTimestamp("timestamp").getSeconds();
                           String date = DateFormat.format("dd-MM-yyyy", time*1000L).toString();
                           timestamp.setText(date);

                           String imageUrl = task.getResult().getString("image_url");
                           Glide.with(getBaseContext()).asBitmap().load(imageUrl)
                                   .placeholder(R.drawable.post_placeholder)
                                   .into(postImage);
                           postLikesCount(blog_post_id);
                           postsAuthorId = task.getResult().getString("user_id");
                           assert postsAuthorId != null;
                           enableEditOrDeletePost(postsAuthorId, blog_post_id);
                           db.collection("Users").document(postsAuthorId).addSnapshotListener(CommentsActivity.this, new EventListener<DocumentSnapshot>() {
                               @Override
                               public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                   if (documentSnapshot != null){
                                       String name = documentSnapshot.getString("name");
                                       userDisplayName.setText(name);

                                       String image = documentSnapshot.getString("image");
                                       Glide.with(getBaseContext())
                                               .asBitmap()
                                               .load(image)
                                               .placeholder(R.drawable.post_user_placeholder)
                                               .into(userDp);
                                   }
                               }
                           });

                       }
                   }
               }
           });


        commentText = findViewById(R.id.comment_input_text);
        ImageView sendCommentButton = findViewById(R.id.comment_post_button);
        RecyclerView commentListRecyclerView = findViewById(R.id.comment_list_recycler_view);

        //database comment list
        commenterId = new ArrayList<>();
        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(this,commentsList, commenterId);
        commentListRecyclerView.setHasFixedSize(true);
        commentListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentListRecyclerView.setAdapter(commentsAdapter);

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

    private void postLikesCount(final String blog_post_id) {
        //checking if already liked.
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Posts/" + blog_post_id + "/Likes")
                        .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> isLiked = new HashMap<>();
                            isLiked.put("likes", FieldValue.serverTimestamp());
                            db.collection("Posts/" + blog_post_id + "/Likes")
                                    .document(currentUserId).set(isLiked);
                        } else {
                            db.collection("Posts/" + blog_post_id + "/Likes")
                                    .document(currentUserId).delete();
                        }
                    }
                });
            }
        });
        //changing icon
        db.collection("Posts/" + blog_post_id + "/Likes")
                .document(currentUserId).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(CommentsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                   likeButton.setImageResource(R.drawable.ic_liked);

                } else {
                    likeButton.setImageResource(R.drawable.ic_like_button);
                }
            }
        });
        // counting likes
        db.collection("Posts/" + blog_post_id + "/Likes").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    likeCount.setText(count + " likes");
                } else {
                    likeCount.setText(0 + " likes");
                }
            }
        });
    }

    private void enableEditOrDeletePost(String postsAuthorId, final String post_id) {
        if (postsAuthorId.equals(currentUserId)) {
            deleteButton.setVisibility(View.VISIBLE);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
                builder.setTitle("Delete Post")
                        .setMessage("Are you sure?");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       /* Toast.makeText(CommentsActivity.this,"ok clicked", Toast.LENGTH_LONG).show();*/
                        db.collection("Posts").document(post_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(CommentsActivity.this,HomeFragment.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

    }
}
