package com.mike.givemewingzz.mapsclusterdemo.adapters.realmutils;

import com.mike.givemewingzz.mapsclusterdemo.model.data.Results;

import io.realm.RealmResults;

public class RealmResultsAdapter extends RealmModelAdapter<Results> {

    public RealmResultsAdapter(RealmResults<Results> resultsRealmResults) {
        super(resultsRealmResults);
    }

}
