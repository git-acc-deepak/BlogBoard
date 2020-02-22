package com.deepak.android.blogboard;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.annotations.NotNull;

public class BlogPostsId {
    @Exclude
    public String BlogPostsId;

    public <T extends BlogPostsId> T withId(@NotNull final String id) {
        this.BlogPostsId = id;
        return (T) this;
    }


}
