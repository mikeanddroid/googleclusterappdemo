package com.mike.givemewingzz.mapsclusterdemo.base;

import android.app.Application;

import com.mike.givemewingzz.mapsclusterdemo.utils.DBHelper;

import io.realm.Realm;

public class MapsClusterDemoApplication extends Application {

    public static MapsClusterDemoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        // Init Realm DB
        instance = this;
        Realm.init(this);
        Realm.setDefaultConfiguration(DBHelper.getRealmConfig(this));

    }

    public static MapsClusterDemoApplication getInstance() {
        return instance;
    }

}
