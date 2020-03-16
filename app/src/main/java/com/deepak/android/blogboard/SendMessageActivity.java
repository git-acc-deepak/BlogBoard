package com.deepak.android.blogboard;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * purpose: view and chat messages with the user.
 * a recycler view to view messages
 * two layout for messages items one for sender and one for receiver.
 */
public class SendMessageActivity extends AppCompatActivity {

    private static final String TAG = "SendMessageActivity";
    private String recipientId;
    private String recipientName;
    private EditText messageInput;
    private ImageView sendMessageBtn;
    private FirebaseFirestore db;
    private String senderId;
    private RecyclerView mMessagesListView;
    private List<MessageModel> messagesList;
    private List<User> userList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        Log.d(TAG, "onCreate: send message activity created.");

        //toolbar init.
        Toolbar mToolbar = findViewById(R.id.message_toolbar);
        setSupportActionBar(mToolbar);

        //checking intent extras from the previous activity.
        if (getIntent().hasExtra("userId") && getIntent().hasExtra("userName")) {
            recipientId = getIntent().getStringExtra("userId");
            recipientName = getIntent().getStringExtra("userName");
        }

        //adapter and message list init.
        messagesList = new ArrayList<>();
        userList = new ArrayList<>();
        messageAdapter = new MessageAdapter(SendMessageActivity.this, messagesList, userList);
        mMessagesListView = findViewById(R.id.chat_messages_recyclerview);
        mMessagesListView.setLayoutManager(new LinearLayoutManager(SendMessageActivity.this));
        mMessagesListView.setAdapter(messageAdapter);
        mMessagesListView.setHasFixedSize(true);


        Log.d(TAG, "Send Message To ID :" + recipientId + recipientName);

        //setting recipient name on the title bar.
        if (recipientId.equals(senderId)) {
            mToolbar.setTitle("Reply");
        } else {
            mToolbar.setTitle(recipientName);
        }
        //database and auth init.
        db = FirebaseFirestore.getInstance();
        senderId = FirebaseAuth.getInstance().getUid();
        Log.d(TAG, "Current UserID:" + senderId);

        //views init.
        messageInput = findViewById(R.id.message_input_text);
        sendMessageBtn = findViewById(R.id.send_message_to_chat);

        //sending process.
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageInput.getText().toString();

                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("msg", message);
                messageMap.put("from", senderId);
                messageMap.put("date", Timestamp.now());

                //checking if the message is not empty.
                if (!TextUtils.isEmpty(message)) {
                    //store the messages in recipient's Messages collection.
                    db.collection("Users/" + recipientId + "/Messages").add(messageMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    toastAMessage("Message sent");
                                    messageInput.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String text = e.getMessage();
                            toastAMessage(text);
                        }
                    });
                }
            }
        });

        //putting messages in the list
        db.collection("Users/" + recipientId + "/Messages")
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d(TAG, "Error:" + e.getMessage());
                        }
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                final MessageModel messages = doc.getDocument().toObject(MessageModel.class);
                                String senderId = doc.getDocument().getString("from");
                                db.collection("Users").document(senderId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                User user = task.getResult().toObject(User.class);
                                                messagesList.add(messages);
                                                userList.add(user);
                                                int lastMsgPosition = queryDocumentSnapshots.size() - 1;
                                                mMessagesListView.scrollToPosition(lastMsgPosition);
                                                messageAdapter.notifyDataSetChanged();

                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    // for toasting messages purpose.
    private void toastAMessage(String text) {
        Toast.makeText(SendMessageActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
