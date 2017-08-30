package com.mike.givemewingzz.mapsclusterdemo.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mike.givemewingzz.mapsclusterdemo.R;
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
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.IMAGE_URL_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_ADDRESS_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_GEO_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_NAME_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_RATING_KEY;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, LocationListener, UIHandler {

    public static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap googleMapView;
    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private SupportMapFragment supportMapFragment;
    public static final int LOCATION_REQUEST_ID = 1001;

    protected Realm realm;
    private CameraUpdate cameraUpdate;

    private PicassoMarker picassoMarker;

    private ActionPresenter presenter;
    private ProgressDialog progressDialog;
    private BaseModel mapBaseModel;
    public List<Marker> markers;

    private class PicassoMarker implements Target {

        Marker mMarker;

        PicassoMarker(Marker marker) {
            mMarker = marker;

        }

        @Override
        public int hashCode() {
            return mMarker.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PicassoMarker) {
                Marker marker = ((PicassoMarker) o).mMarker;
                return mMarker.equals(marker);
            } else {
                return false;
            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.d("test: ", "bitmap fail");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        realm = RealmController.with(getActivity()).getRealm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching Data");
        progressDialog.setTitle("Please wait");
        presenter = new ActionPresenterImplementation(this, new SearchInteractorImplementation());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // refresh the realm instance
                RealmController.with(getActivity()).refresh();
            }
        }, 400);

        return inflater.inflate(R.layout.map_fragment, container, false);

    }

    private void getResultsData() {

        // Create default request for fetching bbva details
        FetchBBVAData fetchBBVAData = new FetchBBVAData();
        fetchBBVAData.call(new FetchBBVAData.OnResultsComplete() {
            @Override
            public void onResultsFetched(SearchInteractor.OnSearchFinished listener, BaseModel baseModel) {
                Log.d(TAG, " Fragment MAP: onResultsFetched : MVP : Status : " + baseModel.getStatus());
                fetchMapData(baseModel);
                onDataComplete();
            }

            @Override
            public void onResultsQueryLimit(String errorMessage) {

            }
        });

        Prefs.with(getActivity()).setPreLoad(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //show error dialog if Google Play Services not available
        if (!isGooglePlayServicesAvailable()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            getActivity().finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }
        FragmentManager fm = getChildFragmentManager();
        supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container_fragment_id);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container_fragment_id, supportMapFragment).commit();
            supportMapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onPause() {
        OttoHelper.unregister(this);
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        OttoHelper.register(this);
        presenter.onResume();

        BaseModel model = null;
        if (!Prefs.with(getActivity()).getPreLoad()) {
            model = RealmController.with(getActivity()).getBaseModel();

            if (model != null) {
                Log.d(TAG, "onResume : Map : Status : Pre-loaded CACHED " + model.getStatus());
            } else {
                Log.d(TAG, "onResume : Map : Status : Pre-loaded NOT CACHED : Network Call");
                getResultsData();
            }

        } else {
            Log.d(TAG, "onResume : Map : Status : NETWORK CALL");
            // Create default request for fetching bbva details
            getResultsData();
        }

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Subscribe
    public void onResultSuccess(FetchBBVAData.SuccessEvent successEvent) {
        Log.d(TAG, "onResultSuccess : Map Fragment : Status : " + successEvent.getBaseModel().getStatus());
        this.mapBaseModel = successEvent.getBaseModel();
        progressDialog.hide();
    }

    @Subscribe
    public void onResultFailure(FetchBBVAData.FailureEvent failureEvent) {

    }

    public void fetchMapData(BaseModel baseModel) {

        markers = new ArrayList<>();

        Log.d(TAG, " Fragment MAP: fetchMapData : Status : " + baseModel.getStatus());

        RealmList<Results> resultsRealmList = baseModel.getResults();

        for (Results results : resultsRealmList) {
            Log.d(TAG, " Fragment : Results Model : address : " + results.getFormattedAddress());

            Double lat = results.getGeometry().getPlaceLocation().getLatitude();
            Double lng = results.getGeometry().getPlaceLocation().getLongitude();

            final String locationTitle = results.getLocationName();
            final String rating = results.getRating();
            final String formattedAddress = results.getFormattedAddress();

            final String imageUrl = results.getIcon();

            Log.d(TAG, " Fragment : Results Model : Location Icon : " + imageUrl);

            final LatLng bbvaLatLon = new LatLng(lat, lng);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(bbvaLatLon);
            markerOptions.title(locationTitle + "\n\nRating: " + rating + "\n\nAddress:" + formattedAddress);

            markers.add(googleMapView.addMarker(markerOptions));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker m : markers) {
                picassoMarker = new PicassoMarker(m);
                Picasso.with(getActivity()).load(imageUrl).into(picassoMarker);
                builder.include(m.getPosition());
            }

            int padding = 50;

            LatLngBounds bounds = builder.build();

            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

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

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    @Override
    public void setItems(BaseModel items) {
        BaseModel baseModel = mapBaseModel;
        Log.d(TAG, " Fragment MAP: setItems : MVP : Status : " + baseModel.getStatus());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapView = googleMap;

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                enableLocationSettings();
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            enableLocationSettings();
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMapView.getUiSettings().setZoomControlsEnabled(true);
        googleMapView.getUiSettings().setCompassEnabled(true);
        googleMapView.getUiSettings().setMyLocationButtonEnabled(true);

        googleMapView.getUiSettings().setRotateGesturesEnabled(true);
        googleMapView.getUiSettings().setScrollGesturesEnabled(true);
        googleMapView.getUiSettings().setTiltGesturesEnabled(true);
        googleMapView.getUiSettings().setZoomGesturesEnabled(true);

        googleMapView.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Marker newMarker = googleMapView.addMarker(new MarkerOptions()
                        .position(latLng)
                        .snippet(latLng.toString()));

                newMarker.setTitle(newMarker.getId());
            }
        });

    }

    private void enableLocationSettings() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            mGoogleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.d(TAG, "Location error" + connectionResult.getErrorCode());
                        }
                    }).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(getActivity(), 199);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "Ex : ", e);
                            }
                            break;
                    }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMapView.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getContext());
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_ID);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_ID);
            }
            return false;
        } else {
            return true;
        }
    }

}
