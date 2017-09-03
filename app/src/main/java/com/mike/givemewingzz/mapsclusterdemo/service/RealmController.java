package com.mike.givemewingzz.mapsclusterdemo.service;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Results;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmController {

    private static RealmController instance;
    private final Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {

        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {

        return instance;
    }

    public Realm getRealm() {

        return realm;
    }

    //Refresh the realm istance
    public void refresh() {
        realm.refresh();
    }

    //clear all objects from Book.class
    public void clearAll() {
        realm.beginTransaction();
        realm.delete(BaseModel.class);
        realm.delete(Results.class);
        realm.commitTransaction();
    }

    //find all objects in the Book.class
    public RealmResults<Results> getResults() {
        return realm.where(Results.class).findAll();
    }

    //query a single item with the given id
    public BaseModel getBaseModel() {
        return realm.where(BaseModel.class).findFirst();
    }

    //query example
    public RealmResults<Results> queriedResults() {
        return realm.where(Results.class)
                .contains("formatted_address", "formattedAddress")
                .or()
                .contains("title", "Realm")
                .findAll();

    }
}
