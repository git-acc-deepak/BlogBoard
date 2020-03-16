package com.deepak.android.blogboard;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * function of this adapter class is to recycle the blog posts that the home fragment receive
 * from the BlogPostModel class.
 */
// adapter with view holder.
public class PostAdapter extends RecyclerView.Adapter <PostAdapter.ViewHolder>{

    private static final String TAG = "PostAdapter";
    private List<User> userList ;
    private List<BlogPostModel> postList ;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context mContext;
    private String currentUserId;

    //this default constructor receive data from the home fragment array list to display it on the card.


    public PostAdapter(Context context, List<User> userList, List<BlogPostModel> postList) {
        this.userList = userList;
        this.postList = postList;
        this.mContext = context;
    }

    //this method is responsible for inflating the views.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_post, parent,false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            currentUserId = user.getUid();
        }
        return new ViewHolder(view);
    }

    //this method binds views to layout
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolderCalled");

        holder.setIsRecyclable(false);

        final String postsId = postList.get(position).BlogPostsId;

         /* getting and setting data taken form the setter method in model class
         to views on item layout
         */
         //getting n setting title and desc of post
        String descData = postList.get(position).getDesc();
        String titleData = postList.get(position).getTitle();
        holder.setDesc(descData, titleData);

        // setting image of post
        String image_url = postList.get(position).getImage_url();
        String thumbnail = postList.get(position).getThumbnail();
        holder.setPostImage(image_url, thumbnail);
        
        //setting user details
        final String userName = userList.get(position).getName();
        String userImage = userList.get(position).getImage();
        holder.setUserInfo(userName, userImage);

        //post time
        long time = postList.get(position).getTimestamp().getTime();
        String date = DateFormat.format("dd-MM-yyyy", time).toString();
        holder.setTime(date);

        // opening post for comments or viewing

        holder.comment_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   openPost(postsId);
                }
            });

        holder.blogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPost(postsId);
            }
        });

        //opening message on profile image click.
        final String userId = postList.get(position).getUser_id();
        holder.blogAuthorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SendMessageActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                mContext.startActivity(intent);
            }
        });

        //getting comment count
        db.collection("Posts/" + postsId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    holder.commentCount.setText(count + " comments");
                } else {
                    holder.commentCount.setText(0 + " comments");
                }
            }
        });

        //setting up likes feature

        /*
         * if the user hits the like button check if the post id already liked if liked delete the like
         * and if not put a like there.
         */
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Posts/" + postsId + "/Likes")
                        .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> isLiked = new HashMap<>();
                            isLiked.put("likes", FieldValue.serverTimestamp());
                            db.collection("Posts/" + postsId + "/Likes")
                                    .document(currentUserId).set(isLiked);
                        } else {
                            db.collection("Posts/" + postsId + "/Likes")
                                    .document(currentUserId).delete();
                        }
                    }
                });
            }
        });
        //setting like button drawable and  like counts
        db.collection("Posts/" + postsId + "/Likes")
                .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    holder.likeButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_liked));
                    getLikesCount(postsId, holder);

                } else {
                    holder.likeButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_like_button));
                }
            }
        });

    }

    private void getLikesCount(String postsId, final ViewHolder holder) {
        db.collection("Posts/" + postsId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                   int count = queryDocumentSnapshots.size();
                   holder.likesCount.setText(count + " likes");
               } else {
                   holder.likesCount.setText(0 + " likes");
               }
            }
        });
    }

    private void openPost(String postsId) {
       if (postsId != null){
           Intent intent = new Intent(mContext,CommentsActivity.class);
           intent.putExtra("blog_post_id", postsId);
           mContext.startActivity(intent);
       }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //class to hold items together to inflate them on a layout using the onCreateViewHolder.
    public class ViewHolder extends RecyclerView.ViewHolder{
        //views to be inflated on a post item.
        ImageView likeButton;
        ImageView comment_button;

        TextView descView;
        TextView blogTitleView;
        ImageView blogImageView;
        CircleImageView blogAuthorImage;
        TextView blogAuthorName;
        TextView postDate;

        TextView likesCount;
        TextView commentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //views initialization
            likeButton = itemView.findViewById(R.id.like_post_button);
            comment_button = itemView.findViewById(R.id.view_comments_button);
            descView = itemView.findViewById(R.id.post_desc_text_view);
            blogTitleView = itemView.findViewById(R.id.post_title_text_view);
            blogImageView = itemView.findViewById(R.id.post_image_view);
            blogAuthorImage = itemView.findViewById(R.id.user_image_profile_view);
            blogAuthorName = itemView.findViewById(R.id.author_name_text_view);
            postDate = itemView.findViewById(R.id.date_of_post_text_view);
            likesCount = itemView.findViewById(R.id.like_count);
            commentCount = itemView.findViewById(R.id.comment_count);

        }

        public void setDesc(String descData, String titleData) {
            blogTitleView.setText(titleData);
            descView.setText(descData);
        }

        public void setPostImage(String image_url, String thumbnail) {

            Glide.with(mContext)
                    .asBitmap()
                    .load(image_url)
                    .placeholder(R.drawable.post_placeholder)
                    .thumbnail(
                    Glide.with(mContext)
                            .asBitmap().load(thumbnail)
            ).into(blogImageView);
        }

        public void setTime(String date) {
            postDate.setText(date);
        }

        public void setUserInfo(String userName, String userImage) {
            blogAuthorName.setText(userName);
            Glide.with(mContext).asBitmap()
                    .load(userImage)
                    .placeholder(R.drawable.post_user_placeholder)
                    .into(blogAuthorImage);
        }
    }
}
