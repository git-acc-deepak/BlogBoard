package com.deepak.android.blogboard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<User> userList;
    private List<BlogPostModel> postList;
    private FirebaseFirestore db;
    private Context mContext;

    public NotificationAdapter(Context mContext, List<User> userList, List<BlogPostModel> postList) {
        this.userList = userList;
        this.postList = postList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_item, parent, false);
        db = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String postId = postList.get(position).BlogPostsId;

        //setting blog author image.
        String userImg = userList.get(position).getImage();
        Glide.with(mContext).asBitmap().load(userImg).placeholder(R.drawable.post_user_placeholder)
                .into(holder.blogAuthorImage);
        //setting notification details
        String nd = postList.get(position).getTitle();
        holder.notificationDetails.setText(nd);

        holder.notificationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("blog_post_id", postId);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView blogAuthorImage;
        TextView notificationDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //views initialization
            blogAuthorImage = itemView.findViewById(R.id.user_id_for_notification);
            notificationDetails = itemView.findViewById(R.id.notification_details);

        }
    }
}
