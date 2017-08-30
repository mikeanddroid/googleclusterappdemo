package com.mike.givemewingzz.mapsclusterdemo.presenter;

import android.util.Log;

import com.mike.givemewingzz.mapsclusterdemo.base.MapsClusterDemoApplication;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.service.ApiCall.FetchBBVAData;
import com.mike.givemewingzz.mapsclusterdemo.service.RealmController;

import io.realm.Realm;

public class SearchInteractorImplementation implements SearchInteractor, FetchBBVAData.OnResultsComplete {

    protected Realm realm;
    private static final String TAG = SearchInteractorImplementation.class.getSimpleName();

    @Override
    public void findRequest(final OnSearchFinished listener) {

        realm = RealmController.with(MapsClusterDemoApplication.getInstance()).getRealm();

        FetchBBVAData fetchBBVAData = new FetchBBVAData();
        fetchBBVAData.call(this);

    }

    @Override
    public void onResultsFetched(OnSearchFinished listener, BaseModel model) {
        Log.d(TAG, " SearchInteractorImplementation Results : Size : " + model.getStatus());
        listener.onFinished(model);
    }

    @Override
    public void onResultsQueryLimit(String errorMessage) {

    }
}
