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

    public BlogPostModel(String user_id, String image_url, String desc, String title, String thumbnail, Date timestamp ,String search) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.thumbnail = thumbnail;
        this.title = title;
        this.timestamp = timestamp;
        this.search = search;
    }

    //getter and setters
    public String getTitle() {
        return title;
    }


    public String getUser_id() {
        return user_id;
    }


    public String getImage_url() {
        return image_url;
    }


    public String getDesc() {
        return desc;
    }


    public String getThumbnail() {
        return thumbnail;
    }


    public Date getTimestamp() {
        return timestamp;
    }


    public String getSearch() {
        return search;
    }


}
