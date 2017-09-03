package com.mike.givemewingzz.mapsclusterdemo.model.data;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by GiveMeWingzz on 9/2/2017.
 */

public class Photos extends RealmObject {

    @SerializedName("height")
    private int imageHeight;

    @SerializedName("width")
    private int imageWidth;

    @SerializedName("photo_reference")
    private String photoReference;

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }
}
