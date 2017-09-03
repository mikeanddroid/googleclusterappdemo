package com.mike.givemewingzz.mapsclusterdemo.model.data;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Random;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Results extends RealmObject implements ClusterItem {

    private static final String TAG = Results.class.getSimpleName();

    @SerializedName("formatted_address")
    private String formattedAddress;

    private Geometry geometry;

    private String icon;

    @SerializedName("id")
    @PrimaryKey
    private String locationID;

    @SerializedName("name")
    private String locationName;

    @SerializedName("opening_hours")
    private OpeningHours openingHours;

    @SerializedName("place_id")
    private String placeID;

    private String rating;

    private String reference;

    private RealmList<Photos> photos;

    @Ignore
    private Random mRandom = new Random(1984);

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public RealmList<Photos> getPhotos() {
        return photos;
    }

    public void setPhotos(RealmList<Photos> photos) {
        this.photos = photos;
    }

    @Override
    public LatLng getPosition() {

        Double lat = getGeometry().getPlaceLocation().getLatitude();
        Double lon = getGeometry().getPlaceLocation().getLongitude();

        Log.i(TAG, "Lat : " + lat + "Lon : " + lon);

        return new LatLng(lat, lon);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }

}
