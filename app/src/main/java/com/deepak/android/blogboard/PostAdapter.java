package com.deepak.android.blogboard;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * function of this adapter class is to recycle the blog posts that the home fragment receive
 * from the BlogPostModel class.
 */
// adapter with view holder.
public class PostAdapter extends RecyclerView.Adapter <PostAdapter.ViewHolder>{


    private static final String TAG = "PostAdapter";
    public List<BlogPostModel> blogPostModelList;
    public List<User> userList;
    public Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    //this constructor receive data from the home fragment array list to display it on the card.

    public  PostAdapter(List<BlogPostModel> blogPostModelList, List<User> userList){
        // assigned item list received from the model class to local list to operate on.
        this.blogPostModelList = blogPostModelList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating the list item card view layout
        View view  = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.individual_post, parent ,false);
        context = parent.getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        final String postsId = blogPostModelList.get(position).BlogPostsId;
        final String currentUserId = mAuth.getCurrentUser().getUid();

        String descData = blogPostModelList.get(position).getDesc();
        String titleData = blogPostModelList.get(position).getTitle();
        /* descData and titleData contains post desc taken form the setter method in model class
        and passed to the view holder setDesc method which inflate it to the cardView of the post.
         */
        holder.setDesc(descData, titleData);

        String image_url = blogPostModelList.get(position).getImage_url();
        String thumbnail = blogPostModelList.get(position).getThumbnail();
        holder.setPostImage(image_url, thumbnail);

        //post time
        long time = blogPostModelList.get(position).getTimestamp().getTime();
        String date = DateFormat.format("dd-MM-yyyy", time).toString();
        holder.setTime(date);

        String user_id = blogPostModelList.get(position).getUser_id();
        //user Data retrieval

                    String userName = userList.get(position).getName();
                    String userImage = userList.get(position).getImage();
                    holder.setUserInfo(userName, userImage);


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
                        if (!task.getResult().exists()){
                            Map<String,Object> isLiked = new HashMap<>();
                            isLiked.put("likes", FieldValue.increment(1));
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

        //setting like button drawable
        db.collection("Posts/" + postsId + "/Likes")
                .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(context,"Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()){
                    holder.likeButton.setImageDrawable(context.getDrawable(R.drawable.ic_liked));

                } else {
                    holder.likeButton.setImageDrawable(context.getDrawable(R.drawable.ic_like_button));
                }
            }
        });

        //get likes count.


        //comment button.

        holder.comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comments = new Intent(context, CommentsActivity.class);
                comments.putExtra("blog_post_id",postsId);
                context.startActivity(comments);
            }
        });

    }

    /**
     *
     * @return number of post the adapter have.
     */
    @Override
    public int getItemCount() {
        return blogPostModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogAuthorName;
        private ImageView blogAuthorImg;
        private TextView blogTitleView;
        private TextView postDate;
        private ImageView likeButton;
        private TextView likeCounter;
        private ImageView comment_button;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            comment_button = mView.findViewById(R.id.view_comments_button);
            likeButton = mView.findViewById(R.id.like_post_button);

            }
        //methods to set text on home frag cardView.
        public void setDesc(String descText , String titleText){
            descView = mView.findViewById(R.id.post_desc_text_view);
            descView.setText(descText);

            blogTitleView = mView.findViewById(R.id.post_title_text_view);
            blogTitleView.setText(titleText);
        }

        public void setPostImage(String url ,String thumb){
                blogImageView = mView.findViewById(R.id.post_image_view);
                RequestOptions placeHolder = new RequestOptions();
                placeHolder.placeholder(R.drawable.post_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(url).thumbnail(
                    Glide.with(context).load(thumb)
            ).into(blogImageView);
        }

        public void setUserInfo(String uName, String profilePic){
            blogAuthorName = mView.findViewById(R.id.author_name_text_view);
            blogAuthorImg = mView.findViewById(R.id.user_image_profile_view);

            blogAuthorName.setText(uName);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.post_user_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(profilePic).into(blogAuthorImg);
        }

        public void setTime(String date){
            postDate = mView.findViewById(R.id.date_of_post_text_view);
            postDate.setText(date);
        }

        public void likeCounter(int likes){
            likeCounter = mView.findViewById(R.id.post_likes_count);
            likeCounter.setText(likes);
        }
    }
}
