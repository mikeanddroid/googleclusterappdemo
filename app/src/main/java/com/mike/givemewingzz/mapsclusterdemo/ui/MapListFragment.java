package com.mike.givemewingzz.mapsclusterdemo.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.maps.android.clustering.ClusterManager;
import com.mike.givemewingzz.mapsclusterdemo.R;
import com.mike.givemewingzz.mapsclusterdemo.adapters.LocationListAdapter;
import com.mike.givemewingzz.mapsclusterdemo.adapters.realmutils.RealmResultsAdapter;
import com.mike.givemewingzz.mapsclusterdemo.adapters.realmutils.ResultsAdapter;
import com.mike.givemewingzz.mapsclusterdemo.model.UIHandler;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Location;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Results;
import com.mike.givemewingzz.mapsclusterdemo.presenter.ActionPresenter;
import com.mike.givemewingzz.mapsclusterdemo.presenter.ActionPresenterImplementation;
import com.mike.givemewingzz.mapsclusterdemo.presenter.SearchInteractorImplementation;
import com.mike.givemewingzz.mapsclusterdemo.service.ApiCall.FetchBBVAData;
import com.mike.givemewingzz.mapsclusterdemo.service.OttoHelper;
import com.mike.givemewingzz.mapsclusterdemo.service.RealmController;
import com.mike.givemewingzz.mapsclusterdemo.utils.Prefs;
import com.squareup.otto.Subscribe;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.IMAGE_URL_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_ADDRESS_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_GEO_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_NAME_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_RATING_KEY;

/**
 * Created by GiveMeWingzz on 8/25/2017.
 */
public class MapListFragment extends Fragment implements LocationListAdapter.EventListener, UIHandler, ResultsAdapter.EventListener {

    public static final String TAG = MapListFragment.class.getSimpleName();

    private RecyclerView recyclerView;

    private LocationListAdapter locationListAdapter;
    protected Realm realm;

    private ProgressDialog progressDialog;
    private ActionPresenter presenter;

    private ResultsAdapter resultsAdapter;

    ClusterManager<Location> locationClusterManager;
    private Random random = new Random(1984);

    public static MapListFragment newInstance() {
        return new MapListFragment();
    }

//    private class LocationRenderer extends DefaultClusterRenderer<Location> {
//
//        private final IconGenerator mIconGenerator = new IconGenerator(MapsClusterDemoApplication.getInstance());
//        private final IconGenerator mClusterIconGenerator = new IconGenerator(MapsClusterDemoApplication.getInstance());
//        private final ImageView mImageView;
//        private final ImageView mClusterImageView;
//        private final int mDimension;
//
//        public LocationRenderer(Context context, GoogleMap map_abs_layout, ClusterManager<Location> clusterManager) {
//            super(context, map_abs_layout, clusterManager);
//        }
//
//        public LocationRenderer() {
//            super(MapsClusterDemoApplication.getInstance(), getMap(), locationClusterManager);
//        }
//
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        realm = RealmController.with(getActivity()).getRealm();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data");
        progressDialog.setTitle("Please wait");
        presenter = new ActionPresenterImplementation(this, new SearchInteractorImplementation());

        setupRecyclerView();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // refresh the realm instance
                RealmController.with(getActivity()).refresh();

                // get all persisted objects
                // create the helper adapter and notify data set changes
                // changes will be reflected automatically
                setRealmAdapter(RealmController.with(getActivity()).getResults());
            }
        }, 400);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        // Register Otto
        OttoHelper.register(this);

        presenter.onResume();

        BaseModel model = null;
        if (!Prefs.with(getActivity()).getPreLoad()) {
            model = RealmController.with(getActivity()).getBaseModel();

            if (model != null) {
                Log.d(TAG, "onResume : Map List : Status : Pre-loaded CACHED " + model.getStatus());
            } else {
                Log.d(TAG, "onResume : Map List : Status : Pre-loaded NOT CACHED : Network Call");
                setResultsData();
            }

        } else {
            Log.d(TAG, "onResume : Map List : Status : NETWORK CALL");
            // Create default request for fetching bbva details
            setResultsData();
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        // UnRegister Otto
        OttoHelper.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Subscribe
    public void onResultSuccess(FetchBBVAData.SuccessEvent successEvent) {
        Log.d(TAG, "onResultSuccess : MapList : Status : " + successEvent.getBaseModel().getStatus());
        setItems(successEvent.getBaseModel());
    }

    @Subscribe
    public void onResultFailure(FetchBBVAData.FailureEvent failureEvent) {
        Log.d(TAG, " onResultFailure : Base Model : Status : " + failureEvent.getErrorMessage());

        onQueryExceeded();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Query Limit Exceeded");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        if (isVisible()) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            if (alertDialog != null) {
                alertDialog.show();
            }
        }

    }

    private void setupRecyclerView() {
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        resultsAdapter = new ResultsAdapter(getActivity());
        recyclerView.setAdapter(resultsAdapter);
    }

    private void setResultsData() {
        presenter.onResultsFetch();
        Prefs.with(getActivity()).setPreLoad(true);
    }

    @Override
    public void onItemClick(final int position, Results results) {

        // Navigate to details view
        Intent detailsIntent = new Intent(getActivity(), MapLocationDetails.class);
        detailsIntent.putExtra(IMAGE_URL_KEY, results.getIcon());
        detailsIntent.putExtra(LOCATION_GEO_KEY, results.getGeometry().getPlaceLocation().toString());
        detailsIntent.putExtra(LOCATION_NAME_KEY, results.getLocationName());
        detailsIntent.putExtra(LOCATION_ADDRESS_KEY, results.getFormattedAddress());
        detailsIntent.putExtra(LOCATION_RATING_KEY, results.getRating());
        getActivity().startActivity(detailsIntent);

        presenter.onItemClicked(position);

    }

    @Override
    public void showProgress() {

        if (isVisible()) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.hide();
            }

            if (progressDialog != null) {
                progressDialog.show();
            }
        }

    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    @Override
    public void onDataComplete() {
        progressDialog.dismiss();
    }

    @Override
    public void onQueryExceeded() {
        progressDialog.dismiss();
    }

    @Override
    public void setItems(final BaseModel items) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                RealmList<Results> resultsRealmList = null;
                if (items != null) {
                    Log.d(TAG, " Fragment : Base Model : MVP : Status : " + items.getStatus());
                    resultsRealmList = items.getResults();
                }

                RealmResults<Results> realmResults = realm.where(Results.class).findAll();

                if (resultsRealmList != null) {
                    for (Results results : resultsRealmList) {
                        Log.d(TAG, " Fragment : Results Model : MVP : address : " + results.getFormattedAddress());

                        final String locationTitle = results.getLocationName();
                        final String rating = results.getRating();
                        final String formattedAddress = results.getFormattedAddress();

                        final String imageUrl = results.getIcon();

                        Log.d(TAG, " Fragment : Results Model : Location Icon : " + imageUrl);
                        Log.d(TAG, " Fragment : Results Model : Location Title : " + locationTitle);
                        Log.d(TAG, " Fragment : Results Model : Location Rating : " + rating);
                        Log.d(TAG, " Fragment : Results Model : Location Address : " + formattedAddress);

                    }
                }

                setRealmAdapter(realmResults);
                progressDialog.hide();
            }
        }, 500);

    }

    public void setRealmAdapter(RealmResults<Results> realmResults) {

        RealmResultsAdapter realmResultsAdapter = new RealmResultsAdapter(realmResults);
        resultsAdapter.setEventListener(this);
        resultsAdapter.setRealmAdapter(realmResultsAdapter);
        resultsAdapter.notifyDataSetChanged();

    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
