package com.deepak.android.blogboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter to people's list.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<User> mPeople;
    private Context mContext;

    public UsersAdapter(Context mContext, List<User> mPeople) {
        this.mPeople = mPeople;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_user_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /* setting data on the list. */
        Log.d("UserAdapter", "onBindViewHolder called.");
        final String userName = mPeople.get(position).getName();
        holder.mUserName.setText(userName);
        final String image = mPeople.get(position).getImage();
        Glide.with(mContext).asBitmap().load(image)
                .placeholder(R.drawable.post_user_placeholder).into(holder.mUserImage);
        //user id
        final String userId = mPeople.get(position).UserId;
        //setting OnClickEvent to take to Message Activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SendMessageActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPeople.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView mUserImage;
        TextView mUserName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserImage = itemView.findViewById(R.id.people_image);
            mUserName = itemView.findViewById(R.id.people_name);
        }
    }
}
