package com.deepak.android.blogboard;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView notificationsRV;
    private List<BlogPostModel> itemDetail;
    private List<User> userDetail;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NotificationAdapter notificationAdapter;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        //init lists, recycler view and its adapter.
        itemDetail = new ArrayList<>();
        userDetail = new ArrayList<>();
        notificationsRV = view.findViewById(R.id.notifications_list);
        notificationAdapter = new NotificationAdapter(getActivity(), userDetail, itemDetail);
        notificationsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationsRV.setHasFixedSize(true);
        notificationsRV.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();
        db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        itemDetail.clear();
                        userDetail.clear();
                        if (e != null) {
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                                if (dc.getType() == DocumentChange.Type.ADDED) {

                                    DocumentSnapshot ds = dc.getDocument();
                                    String postsId = ds.getId();
                                    final BlogPostModel blogPostModel = dc.getDocument().toObject(BlogPostModel.class).withId(postsId);
                                    //getting user details from post.
                                    String blogUserId = dc.getDocument().getString("user_id");
                                    db.collection("Users").document(blogUserId).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        User user = task.getResult().toObject(User.class);
                                                        // adding notification to list.
                                                        userDetail.add(user);
                                                        itemDetail.add(blogPostModel);
                                                        notificationAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                }

                            }
                        }

                    }
                });

        return view;
    }

}
