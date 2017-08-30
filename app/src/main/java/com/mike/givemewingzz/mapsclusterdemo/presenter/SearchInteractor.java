package com.mike.givemewingzz.mapsclusterdemo.presenter;

import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;

public interface SearchInteractor {

    interface OnSearchFinished {
        void onFinished(BaseModel items);
        void onQueryLimit();
    }

    void findRequest(OnSearchFinished listener);

}
