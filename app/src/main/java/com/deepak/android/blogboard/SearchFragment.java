package com.deepak.android.blogboard;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private FirebaseFirestore db;
    private ArrayList<BlogPostModel> listPost;
    private ArrayList<User> postUser;
    private PostAdapter blogRecyclerAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView mSearchView = view.findViewById(R.id.search_view_text);
        RecyclerView mSearchResult = view.findViewById(R.id.search_result_list);

        listPost = new ArrayList<>();
        postUser = new ArrayList<>();

        blogRecyclerAdapter = new PostAdapter(getContext(), postUser, listPost);
        mSearchResult.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mSearchResult.setAdapter(blogRecyclerAdapter);

        db = FirebaseFirestore.getInstance();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery(newText);
                return false;
            }
        });

        return view;
    }

    private void searchQuery(String query) {

        if (query.length() > 0) {
            query = query.substring(0, 1).toUpperCase() + query.substring(1).toLowerCase();
            final String finalQuery = query;
            db.collection("Posts").whereGreaterThanOrEqualTo("title", query)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                System.err.println("Listen failed:" + e);
                                return;
                            }
                            if (queryDocumentSnapshots != null) {
                                postUser.clear();
                                listPost.clear();

                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    final BlogPostModel blogPostModel = doc.toObject(BlogPostModel.class);

                                    String userID = doc.getString("user_id");
                                    if (userID != null) {
                                        db.collection("Users").document(userID).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    User user = task.getResult().toObject(User.class);
                                                    if (blogPostModel.getTitle() != null && blogPostModel.getTitle().contains(finalQuery)) {
                                                        listPost.add(blogPostModel);
                                                        postUser.add(user);
                                                    }
                                                    blogRecyclerAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }

                                }
                            }
                        }
                    });
        }
    }

}
