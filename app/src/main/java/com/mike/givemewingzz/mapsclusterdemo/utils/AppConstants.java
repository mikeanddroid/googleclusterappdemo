package com.mike.givemewingzz.mapsclusterdemo.utils;

import com.mike.givemewingzz.mapsclusterdemo.base.MapsClusterDemoApplication;

import java.util.ArrayList;

public class AppConstants {

    public static class API_KEYS {
        public static final String MAP_API_KEY = "AIzaSyAJUm4b8fflXMVmk0ddpX2gwOq-pUzEdeE";
        public static final String MAP_API_KEY_1 = "AIzaSyDbmpULYf-AZirfwdTiKp8MVrlNTt0JYsE";
        public static final String MAP_API_KEY_2 = "AIzaSyDQCquagcBx6Cek_39NbW-1j6qC3QCVgmo";
        public static final String MAP_API_KEY_3 = "AIzaSyDG3OO2goP5JnsnYjsJrSwa-NjVpauIcE8";
        public static final String MAP_API_KEY_4 = "AIzaSyBVujBWSEUZucdrggKVfSSVyseR4j00-1Q";
        public static final String MAP_API_KEY_5 = "AIzaSyAJUm4b8fflXMVmk0ddpX2gwOq-pUzEdeE";
        public static final String MAP_API_KEY_6 = "AIzaSyCixhPeuxzx-PHB6S1LqVaGrS8s_d3WdVk";
        public static final String MAP_API_KEY_7 = "AIzaSyDk35iL27MteP2TYK7Wes13P4knEX1sFsk";
        public static final String MAP_API_KEY_8 = "AIzaSyB_4ArrUzutNyoc24_mG5NVV0TUAP5TsZo";
    }

    public static final String BASE_URL = "https://maps.googleapis.com";
    public static final String BASE_IMAGE_URL = "https://maps.googleapis.com";

    // Intent Keys
    public static final String IMAGE_URL_KEY = "IMAGE_URL_KEY";
    public static final String LOCATION_NAME_KEY = "LOCATION_NAME_KEY";
    public static final String LOCATION_ADDRESS_KEY = "LOCATION_ADDRESS_KEY";
    public static final String LOCATION_GEO_KEY = "LOCATOIN_GEO_KEY";
    public static final String LOCATION_RATING_KEY = "LOCATION_RATING_KEY";
    public static final String LOCATION_IMAGE_REFERENCE = "LOCATION_IMAGE_REFERENCE";


    public static ArrayList<String> getApiKeys() {

        ArrayList<String> apikeys = new ArrayList<>();
        apikeys.add(API_KEYS.MAP_API_KEY);
        apikeys.add(API_KEYS.MAP_API_KEY_1);
        apikeys.add(API_KEYS.MAP_API_KEY_2);
        apikeys.add(API_KEYS.MAP_API_KEY_3);
        apikeys.add(API_KEYS.MAP_API_KEY_4);
        apikeys.add(API_KEYS.MAP_API_KEY_5);
        apikeys.add(API_KEYS.MAP_API_KEY_6);
        apikeys.add(API_KEYS.MAP_API_KEY_7);
        apikeys.add(API_KEYS.MAP_API_KEY_8);

        return apikeys;

    }

    public String getCurrentApiKey() {

        Prefs prefs = Prefs.with(MapsClusterDemoApplication.getInstance());

        if (prefs.isSuccess()) {
            return prefs.getCurrentApiKey();
        }
        return "";
    }

}
