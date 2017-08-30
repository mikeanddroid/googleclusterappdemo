package com.mike.givemewingzz.mapsclusterdemo.model;

import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;

public interface UIHandler {

    void showProgress();

    void hideProgress();

    void setItems(BaseModel items);

    void onDataComplete();

    void onQueryExceeded();

    void showMessage(String message);

}
