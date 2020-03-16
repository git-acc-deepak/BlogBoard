package com.deepak.android.blogboard;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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
public class HomeFragment extends Fragment {

    private RecyclerView listView;
    private List<BlogPostModel> blogList;
    private List<User> userList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar mProgress;
    private PostAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mProgress = view.findViewById(R.id.loading_posts);

        //array list to hold blog list items
        blogList = new ArrayList<>();
        userList = new ArrayList<>();

        //to recycle view list using recycler listView.
        listView = view.findViewById(R.id.list_view);

        blogRecyclerAdapter = new PostAdapter(getContext(), userList, blogList );
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(blogRecyclerAdapter);
        listView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            db = FirebaseFirestore.getInstance();

            listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    boolean reachedLast = !recyclerView.canScrollVertically(1);
                    if (reachedLast){
                        loadMoreBlog();
                    }
                }

            });
        /*
        retrieving data from the database on fire store.
        snapshot listener will help to retrieve data in real time.
         */
            Query firstQuery = db.collection("Posts");
            firstQuery.orderBy("timestamp", Query.Direction.DESCENDING).limit(5)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    mProgress.setVisibility(View.VISIBLE);
                    if (!queryDocumentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad){

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            blogList.clear();
                            userList.clear();

                        }

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                            if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                //Assigning Id to each post before retrieving
                                String postsID = documentChange.getDocument().getId();
                                //data conversion to blog Post model
                                final BlogPostModel blogPostModel = documentChange.getDocument().toObject(BlogPostModel.class).withId(postsID);

                                String blogUserId = documentChange.getDocument().getString("user_id");
                                db.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()){

                                             User user = task.getResult().toObject(User.class);

                                            // adding post to list.
                                            if (isFirstPageFirstLoad) {

                                                userList.add(user);
                                                blogList.add(blogPostModel);

                                            }else {

                                                userList.add(0,user);
                                                blogList.add(0,blogPostModel);
                                            }

                                            blogRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            });
            mProgress.setVisibility(View.INVISIBLE);
        }
            return view;
    }


    // method to run second query.
    public void loadMoreBlog(){

        Query secondQuery = db.collection("Posts");

        secondQuery.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .startAfter(lastVisible)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mProgress.setVisibility(View.VISIBLE);
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            String postsId = documentChange.getDocument().getId();
                            //data conversion to blog Post model
                            final BlogPostModel blogPostModel = documentChange.getDocument().toObject(BlogPostModel.class).withId(postsId);
                            // adding post to list.
                            String blogUserId = documentChange.getDocument().getString("user_id");
                            db.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()){

                                        User user = task.getResult().toObject(User.class);

                                        // adding post to list.
                                        userList.add(user);
                                        blogList.add(blogPostModel);

                                        blogRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(getContext(),"That is all we have right now",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mProgress.setVisibility(View.INVISIBLE);
    }

}
