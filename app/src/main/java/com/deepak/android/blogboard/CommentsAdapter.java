package com.deepak.android.blogboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    public List<CommentsModel> commentsList;
    public List<User> commenterId;
    public Context context;

    public CommentsAdapter(List<CommentsModel> commentsList , List<User> commenterId){

        this.commentsList = commentsList;
        this.commenterId = commenterId;

    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_comment, parent, false);
        context = parent.getContext();
        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        String userName = commenterId.get(position).getName();
        String userImage = commenterId.get(position).getImage();
        holder.setUserInfo(userName, userImage);

        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private TextView commenterName;
        private ImageView commenterImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }
        public void  setUserInfo(String userName , String userImage){
            commenterName = mView.findViewById(R.id.comment_username);
            commenterImage = mView.findViewById(R.id.comment_image);

            commenterName.setText(userName);
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.post_user_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeHolder)
                    .asBitmap()
                    .load(userImage)
                    .into(commenterImage);
        }

    }

}
