package com.deepak.android.blogboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

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


    public PostAdapter(List<User> userList, List<BlogPostModel> postList) {
        this.userList = userList;
        this.postList = postList;
    }

    //this method is responsible for inflating the views.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_post, parent,false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mContext = parent.getContext();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            currentUserId = user.getUid();
        }
        return new ViewHolder(view);
    }

    //this method binds views to layout
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolderCalled");

        holder.setIsRecyclable(false);

        String postsId = postList.get(position).BlogPostsId;

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
        String userName = userList.get(position).getName();
        String userImage = userList.get(position).getImage();
        holder.setUserInfo(userName, userImage);

        //post time
        /*long time = postList.get(position).getTimestamp().getTime();
        String date = DateFormat.format("dd-MM-yyyy", time).toString();
        holder.setTime(date);*/

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,CommentsActivity.class);
                mContext.startActivity(intent);
            }
        });

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
        ImageView deleteButton;
        ImageView editButton;

        TextView descView;
        TextView blogTitleView;
        ImageView blogImageView;
        CircleImageView blogAuthorImage;
        TextView blogAuthorName;
        TextView postDate;

        CardView parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //views initialization
            likeButton = itemView.findViewById(R.id.like_post_button);
            comment_button = itemView.findViewById(R.id.view_comments_button);
            deleteButton = itemView.findViewById(R.id.post_delete_button);
            editButton = itemView.findViewById(R.id.post_edit_button);
            descView = itemView.findViewById(R.id.post_desc_text_view);
            blogTitleView = itemView.findViewById(R.id.post_title_text_view);
            blogImageView = itemView.findViewById(R.id.post_image_view);
            blogAuthorImage = itemView.findViewById(R.id.user_image_profile_view);
            blogAuthorName = itemView.findViewById(R.id.author_name_text_view);
            postDate = itemView.findViewById(R.id.date_of_post_text_view);
            parentLayout = itemView.findViewById(R.id.parent_layout_blog);
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
