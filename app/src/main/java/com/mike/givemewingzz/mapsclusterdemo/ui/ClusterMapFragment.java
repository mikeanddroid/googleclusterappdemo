package com.mike.givemewingzz.mapsclusterdemo.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.mike.givemewingzz.mapsclusterdemo.R;
import com.mike.givemewingzz.mapsclusterdemo.base.AbsBaseFragment;
import com.mike.givemewingzz.mapsclusterdemo.base.MapsClusterDemoApplication;
import com.mike.givemewingzz.mapsclusterdemo.model.UIHandler;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Results;
import com.mike.givemewingzz.mapsclusterdemo.presenter.ActionPresenter;
import com.mike.givemewingzz.mapsclusterdemo.presenter.ActionPresenterImplementation;
import com.mike.givemewingzz.mapsclusterdemo.presenter.SearchInteractor;
import com.mike.givemewingzz.mapsclusterdemo.presenter.SearchInteractorImplementation;
import com.mike.givemewingzz.mapsclusterdemo.service.ApiCall.FetchBBVAData;
import com.mike.givemewingzz.mapsclusterdemo.service.OttoHelper;
import com.mike.givemewingzz.mapsclusterdemo.service.RealmController;
import com.mike.givemewingzz.mapsclusterdemo.utils.Prefs;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.IMAGE_URL_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_ADDRESS_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_GEO_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_NAME_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_RATING_KEY;


/**
 * Created by GiveMeWingzz on 8/29/2017.
 */

public class ClusterMapFragment extends AbsBaseFragment implements UIHandler, ClusterManager.OnClusterClickListener<Results>, ClusterManager.OnClusterInfoWindowClickListener<Results>, ClusterManager.OnClusterItemClickListener<Results>, ClusterManager.OnClusterItemInfoWindowClickListener<Results> {

    private ClusterManager<Results> mClusterManager;
    public static final String TAG = ClusterMapFragment.class.getSimpleName();

    // Presenters and model
    private ActionPresenter presenter;
    private ProgressDialog progressDialog;
    private BaseModel mapBaseModel;

    protected Realm realm;

    private GoogleMap googleMapView;

    public static ClusterMapFragment newInstance() {
        return new ClusterMapFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        realm = RealmController.with(getActivity()).getRealm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Show PD by default
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data");
        progressDialog.setTitle("Please wait");

        // Set presenter instance
        presenter = new ActionPresenterImplementation(this, new SearchInteractorImplementation());

        // Delay the refresh by few milliseconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // refresh the realm instance
                RealmController.with(getActivity()).refresh();
            }
        }, 400);

        return inflater.inflate(R.layout.map_abs_layout, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        FragmentManager fm = getChildFragmentManager();

//        SupportMapFragment supportMapFragment = (SupportMapFragment) fm.findFragmentById(map_abs);
//        if (supportMapFragment == null) {
//            supportMapFragment = SupportMapFragment.newInstance();
//            fm.beginTransaction().replace(R.id.map_container_fragment, supportMapFragment).commit();
//            supportMapFragment.getMapAsync(this);
//        }

    }

    @Override
    public void onPause() {
        OttoHelper.unregister(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        OttoHelper.register(this);
        presenter.onResume();

//        BaseModel model = null;
//        if (!Prefs.with(getActivity()).getPreLoad()) {
//            model = RealmController.with(getActivity()).getBaseModel();
//
//            if (model != null) {
//                Log.d(TAG, "onResume : ClusterMapFragment : Status : Pre-loaded CACHED " + model.getStatus());
//                this.mapBaseModel = model;
//            } else {
//                Log.d(TAG, "onResume : ClusterMapFragment : Status : Pre-loaded NOT CACHED : Network Call");
//                getResultsData();
//            }
//
//        } else {
//            Log.d(TAG, "onResume : ClusterMapFragment : NO PRELOAD : Status : NETWORK CALL");
//            // Create default request for fetching bbva details
//            getResultsData();
//        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (realm != null) {
            realm.close();
        }
        presenter.onDestroy();

        super.onDestroy();
    }

    @Subscribe
    public void onResultSuccess(FetchBBVAData.SuccessEvent successEvent) {
        Log.d(TAG, "onResultSuccess : ClusterMapFragment : Status : " + successEvent.getBaseModel().getStatus());
        this.mapBaseModel = successEvent.getBaseModel();
        progressDialog.hide();
    }

    @Subscribe
    public void onResultFailure(FetchBBVAData.FailureEvent failureEvent) {
        Log.d(TAG, "onResultSuccess : ClusterMapFragment : Error Message : " + failureEvent.getErrorMessage());
        Toast.makeText(getActivity().getApplicationContext(), "ClusterMapFragment : Failed to retrieve data .. \n" + failureEvent.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    private void getResultsData() {

        // Create default request for fetching bbva details
        FetchBBVAData fetchBBVAData = new FetchBBVAData();
        fetchBBVAData.call(new FetchBBVAData.OnResultsComplete() {
            @Override
            public void onResultsFetched(SearchInteractor.OnSearchFinished listener, BaseModel baseModel) {
                Log.d(TAG, " Fragment ClusterMapFragment: onResultsFetched : MVP : Status : " + baseModel.getStatus());
                fetchMapData(baseModel);
                onDataComplete();
            }

            @Override
            public void onResultsQueryLimit(String errorMessage) {
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

                if (!alertDialog.isShowing()) {
                    if (isVisible()) {

                        if (alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        alertDialog.show();

                    }
                }

            }
        });

        Prefs.with(getActivity()).setPreLoad(true);

    }

    public void fetchMapData(BaseModel baseModel) {

        this.mapBaseModel = baseModel;

        Log.d(TAG, " Fragment MAP: fetchMapData : Status : " + mapBaseModel.getStatus());

        if (googleMapView == null) {
            googleMapView = getMap();
        }

        RealmList<Results> resultsRealmList = mapBaseModel.getResults();
        ArrayList<Marker> markers = new ArrayList<>();
        for (Results results : resultsRealmList) {
            Log.d(TAG, " Fragment Map : fetchMapData : address : " + results.getFormattedAddress());

            Double lat = results.getGeometry().getPlaceLocation().getLatitude();
            Double lng = results.getGeometry().getPlaceLocation().getLongitude();

            final String locationTitle = results.getLocationName();
            final String rating = results.getRating();
            final String formattedAddress = results.getFormattedAddress();

            final String imageUrl = results.getIcon();

            Log.d(TAG, " Fragment Map : fetchMapData : Location Icon : " + imageUrl);

            final LatLng bbvaLatLon = new LatLng(lat, lng);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(bbvaLatLon);
            markerOptions.title(locationTitle + "\n\nRating: " + rating + "\n\nAddress:" + formattedAddress);

            markers.add(googleMapView.addMarker(markerOptions));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker m : markers) {

                m.hideInfoWindow();
                m.showInfoWindow();

                m.setVisible(false);
                builder.include(m.getPosition());
            }

            int padding = 50;

            LatLngBounds bounds = builder.build();

            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            googleMapView.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    /**set animated zoom camera into map_abs_layout*/
                    googleMapView.animateCamera(cameraUpdate);
                }
            });

            googleMapView.setInfoWindowAdapter(new CustomInfoWindowAdapter());

            googleMapView.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent detailsIntent = new Intent(getActivity(), MapLocationDetails.class);
                    detailsIntent.putExtra(IMAGE_URL_KEY, imageUrl);
                    detailsIntent.putExtra(LOCATION_GEO_KEY, marker.getPosition().toString());
                    detailsIntent.putExtra(LOCATION_NAME_KEY, marker.getTitle());
                    detailsIntent.putExtra(LOCATION_ADDRESS_KEY, formattedAddress);
                    detailsIntent.putExtra(LOCATION_RATING_KEY, rating);
                    getActivity().startActivity(detailsIntent);
                }
            });

        }
    }

    @Override
    public void showProgress() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }

        if (progressDialog != null) {
            progressDialog.show();
        }

    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    @Override
    public void setItems(BaseModel items) {
        BaseModel baseModel = mapBaseModel;
        Log.d(TAG, " Fragment ClusterMapFragment: setItems : MVP : Status : " + baseModel.getStatus());
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
    public void showMessage(String message) {

    }

    /**
     * Custom Adapter for getting marker content
     */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View view;

        CustomInfoWindowAdapter() {
            view = getLayoutInflater(null).inflate(R.layout.custom_marker_layout, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            BaseModel model = realm.where(BaseModel.class).findFirst();
            RealmList<Results> resultsRealmList = model.getResults();

            TextView locationNameTv = (TextView) view.findViewById(R.id.locationInfoName);
            TextView locationGeoTv = (TextView) view.findViewById(R.id.locationInfoGeo);
            TextView locationAddTv = (TextView) view.findViewById(R.id.locationInfoAddress);
            TextView locationRatingTv = (TextView) view.findViewById(R.id.locationInfoRating);

            for (Results results : resultsRealmList) {
                locationNameTv.setText("Location Name : " + results.getLocationName());
                locationGeoTv.setText("Location Geo : " + marker.getPosition());
                locationAddTv.setText("Location Address : " + results.getFormattedAddress());
                locationRatingTv.setText("Location Rating  :  " + results.getRating() != null ? results.getRating() : "Rating Not Available");
            }

            return view;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class LocationRenderer extends DefaultClusterRenderer<Results> {
        private final IconGenerator mIconGenerator = new IconGenerator(MapsClusterDemoApplication.getInstance());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(MapsClusterDemoApplication.getInstance());
        private final ImageView mImageView;
        //        private final ImageView mClusterImageView;
        private final int mDimension;

        public LocationRenderer() {
            super(MapsClusterDemoApplication.getInstance(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater(null).inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
//            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(MapsClusterDemoApplication.getInstance());
            mDimension = (int) getResources().getDimension(R.dimen.custom_map_marker_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Results results, MarkerOptions markerOptions) {
            // Draw a single results.
            // Set the info window to show their name.
            Log.d(TAG, "ClusterMapFragment : onBeforeClusterItemRendered : Status : " + results.getLocationName());
            Picasso.with(MapsClusterDemoApplication.getInstance()).load(results.getIcon()).placeholder(R.drawable.ic_action_info).into(mImageView);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(results.getLocationName());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Results> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
//            List<Drawable> drawables = new ArrayList<Drawable>(Math.min(2, cluster.getSize()));
            List<Drawable> drawables = new ArrayList<Drawable>(Math.min(2, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Results results : cluster.getItems()) {
                // Draw 4 at most.
                if (drawables.size() == 4) break;
//                Drawable drawable = getResources().getDrawable(results.profilePhoto);
                Drawable drawable = getResources().getDrawable(R.drawable.rectangle_shape);
                drawable.setBounds(0, 0, width, height);
                drawables.add(drawable);

                Picasso.with(MapsClusterDemoApplication.getInstance()).load(results.getIcon()).into(mImageView);

            }
            MultiDrawable multiDrawable = new MultiDrawable(drawables);
            multiDrawable.setBounds(0, 0, width, height);

//            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 2;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Results> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().getLocationName();
        Toast.makeText(getActivity(), cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Results> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Results item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Results item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    @Override
    protected void startDemo(GoogleMap googleMap) {

        googleMapView = getMap();

        BaseModel model = null;
        if (!Prefs.with(getActivity()).getPreLoad()) {
            model = RealmController.with(getActivity()).getBaseModel();

            if (model != null) {
                Log.d(TAG, "onResume : ClusterMapFragment : startDemo : Status : Pre-loaded CACHED " + model.getStatus());
                this.mapBaseModel = model;
            } else {
                Log.d(TAG, "onResume : ClusterMapFragment : startDemo : Status : Pre-loaded NOT CACHED : Network Call");
                getResultsData();
            }

        } else {
            Log.d(TAG, "onResume : ClusterMapFragment : startDemo : Status : NETWORK CALL");
            // Create default request for fetching bbva details
            getResultsData();
        }

        if (googleMapView == null) {
            googleMapView = googleMap;
        }

        googleMapView.moveCamera(CameraUpdateFactory.newLatLngZoom(googleMapView.getCameraPosition().target, 9.5f));

        mClusterManager = new ClusterManager<Results>(getActivity(), googleMapView);
        mClusterManager.setRenderer(new LocationRenderer());
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addItems();
                mClusterManager.cluster();
            }
        }, 1000);

    }

    private void addItems() {
        Log.d(TAG, " Fragment ClusterMapFragment: Add Items : MVP : Status : " + mapBaseModel.getStatus());
        for (Results results : mapBaseModel.getResults()) {
            Log.d(TAG, " Fragment ClusterMapFragment: Add Items : MVP : Results : " + results.getLocationName());
        }

        mClusterManager.addItems(mapBaseModel.getResults());
    }

}
