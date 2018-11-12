package com.learn.myapplication;

import java.io.File;

/**
 * Created on 10/16/17.
 */

public class UserProfile {

    String imageName;

    File image;

    String message;


    public String getImageName() {
        return imageName;
    }

    public UserProfile setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public File getImage() {
        return image;
    }

    public UserProfile setImage(File image) {
        this.image = image;
        return this;
    }
}
