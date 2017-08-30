package com.mike.givemewingzz.mapsclusterdemo.presenter;

import android.util.Log;

import com.mike.givemewingzz.mapsclusterdemo.model.UIHandler;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;


public class ActionPresenterImplementation implements ActionPresenter, SearchInteractor.OnSearchFinished {

    private static final String TAG = ActionPresenterImplementation.class.getSimpleName();
    private UIHandler uiHandler;
    private SearchInteractor searchInteractor;

    public ActionPresenterImplementation(UIHandler uiHandler, SearchInteractor searchInteractor) {
        this.uiHandler = uiHandler;
        this.searchInteractor = searchInteractor;
    }

    @Override
    public void onItemClicked(final int position) {
        if (uiHandler != null) {
            // Todo : Add implementation based on the list element
        }

    }

    @Override
    public void onResultsFetch() {

        if (uiHandler != null) {
            uiHandler.showProgress();
        }

        searchInteractor.findRequest(this);

    }

    @Override
    public void onResume() {
        if (uiHandler != null) {
//            uiHandler.showProgress();
            Log.d(TAG, " API onFinished MAP: MVP : onResume");
        }
    }

    @Override
    public void onDestroy() {
        uiHandler = null;
    }

    @Override
    public void onFinished(BaseModel items) {
        if (uiHandler != null) {

            Log.d(TAG, " API onFinished MAP: MVP : Status : " + items.getStatus());

            uiHandler.setItems(items);
            uiHandler.onDataComplete();
            uiHandler.hideProgress();
        }
    }

    @Override
    public void onQueryLimit() {
        if (uiHandler != null) {
            uiHandler.hideProgress();
            uiHandler.onQueryExceeded();
        }
    }
}
