package com.deepak.android.blogboard;

import java.util.Date;

public class CommentsModel {

    private String message, user_id;
    private Date timestamp;

    public  CommentsModel(){}

    public CommentsModel(String message, String user_id, Date timestamp) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getUser_id() {
        return user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }


}
