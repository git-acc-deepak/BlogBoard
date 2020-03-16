package com.deepak.android.blogboard;
/**
 * model class to retrieve user data.
 */
public class User extends UsersId {

        public String image;
        public String name ;
    public String bio;


        public User(){}

    public User(String image, String name, String bio) {
            this.image = image;
            this.name = name;
        this.bio = bio;

        }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

}
