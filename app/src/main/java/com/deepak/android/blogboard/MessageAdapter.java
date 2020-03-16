package com.deepak.android.blogboard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * adapter class for message list recycler view in the sendActivity layout.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final String TAG = "MessageAdapter";
    private Context mContext;
    private List<MessageModel> messagesList;
    private String currentUserID;
    private List<User> userList;

    //default constructor to receive data from the model class object.
    public MessageAdapter(Context mContext, List<MessageModel> messagesList, List<User> userList) {
        this.mContext = mContext;
        this.messagesList = messagesList;
        this.userList = userList;
    }

    //to generate the layout for each item.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_item, parent, false);
        currentUserID = FirebaseAuth.getInstance().getUid();
        Log.d(TAG, "onCreateViewHolder: " + currentUserID);
        return new ViewHolder(view);
    }

    //to bind the data to each item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //getting data from the server.
        String fromId = messagesList.get(position).getFrom();
        String message = messagesList.get(position).getMsg();
        String userImageUrl = userList.get(position).getImage();
        String userName = userList.get(position).getName();

        //setting data to the view
        if (fromId.equals(currentUserID)) {
            holder.sentLL.setVisibility(View.VISIBLE);
            holder.myMessage(message);
        } else {
            holder.receivedRL.setVisibility(View.VISIBLE);
            holder.receivedMessage(message, userImageUrl, userName);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    //to hold or define the data to be bind on the messages items.
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextSent;
        private TextView mTextReceived;
        private TextView mName;
        private CircleImageView mImageReceiver;
        private LinearLayout sentLL;
        private RelativeLayout receivedRL;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageReceiver = itemView.findViewById(R.id.messenger_id_received);
            mTextReceived = itemView.findViewById(R.id.message_received_txt_view);
            mTextSent = itemView.findViewById(R.id.message_sent_txt_view);
            mName = itemView.findViewById(R.id.message_user_name);
            sentLL = itemView.findViewById(R.id.sent_message_ll);
            receivedRL = itemView.findViewById(R.id.received_message_ll);
        }

        //layout for current user messages.
        public void myMessage(String message) {
            Log.d(TAG, "myMessage: send message called");
            mTextSent.setText(message);
        }

        //layout for received messages
        public void receivedMessage(String message, String userImageUrl, String userName) {
            Log.d(TAG, "receivedMessage: received message called");

            Glide.with(mContext).asBitmap()
                    .load(userImageUrl)
                    .placeholder(R.drawable.post_user_placeholder)
                    .into(mImageReceiver);

            mName.setText(userName);
            mTextReceived.setText(message);
        }
    }
}
