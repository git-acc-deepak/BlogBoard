package com.deepak.android.blogboard;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.annotations.NotNull;

/**
 * to return user id.
 */
public class UsersId {
    @Exclude
    public String UserId;

    public <T extends UsersId> T withId(@NotNull final String id) {
        this.UserId = id;
        return (T) this;
    }
}
