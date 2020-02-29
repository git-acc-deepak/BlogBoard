package com.deepak.android.blogboard;

import java.util.Date;

/**
 * this class is used to parse the json from the fire store database.
 */
public class BlogPostModel extends BlogPostsId{

    public String user_id, image_url, desc, title, thumbnail , search;

    public Date timestamp;

    //Default constructor
    public BlogPostModel(){}

    public BlogPostModel(String user_id, String image_url, String desc, String title, String thumbnail, Date timestamp ) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.thumbnail = thumbnail;
        this.title = title;
        this.timestamp = timestamp;
    }

    //getter and setters
    String getTitle() {
        return title;
    }


    String getUser_id() {
        return user_id;
    }


    String getImage_url() {
        return image_url;
    }


    String getDesc() {
        return desc;
    }


    String getThumbnail() {
        return thumbnail;
    }


    Date getTimestamp() {
        return timestamp;
    }



    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
