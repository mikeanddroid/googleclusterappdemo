package com.mike.givemewingzz.mapsclusterdemo.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mike.givemewingzz.mapsclusterdemo.R;
import com.mike.givemewingzz.mapsclusterdemo.base.MapsClusterDemoApplication;
import com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants;
import com.mike.givemewingzz.mapsclusterdemo.utils.Prefs;
import com.squareup.picasso.Picasso;

import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.IMAGE_URL_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_ADDRESS_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_GEO_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_IMAGE_REFERENCE;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_NAME_KEY;
import static com.mike.givemewingzz.mapsclusterdemo.utils.AppConstants.LOCATION_RATING_KEY;

public class MapLocationDetails extends AppCompatActivity {

    private static final String TAG = MapLocationDetails.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_details);

        if (Build.VERSION.SDK_INT >= 21) {

            // Trans //
            // Transition for Landing page when it slides in.
            Slide slide = new Slide();
            slide.setDuration(1000);
            getWindow().setEnterTransition(slide);

        }

        final String locationImageUrl = getIntent().getStringExtra(IMAGE_URL_KEY);
        final String locationGeo = getIntent().getStringExtra(LOCATION_GEO_KEY);
        final String locationName = getIntent().getStringExtra(LOCATION_NAME_KEY);
        final String locationAddress = getIntent().getStringExtra(LOCATION_ADDRESS_KEY);
        final String locationRating = getIntent().getStringExtra(LOCATION_RATING_KEY);
        String locationImageReference = getIntent().getStringExtra(LOCATION_IMAGE_REFERENCE);

        Log.i(TAG, " Location Geo  :  " + locationGeo != null ? locationGeo : " locationGeo Null ");
        Log.i(TAG, " Location Name :  " + locationName != null ? locationName : " locationName Null ");
        Log.i(TAG, " Location Add  :  " + locationAddress != null ? locationAddress : " locationAddress Null ");
        Log.i(TAG, " Location ImageUrl  :  " + locationImageUrl != null ? locationImageUrl : " Location Url Null ");

        if (locationImageReference != null) {
            Log.i(TAG, " Location locationImageReference  :  " + locationImageReference);
        } else {
            Log.i(TAG, " Location locationImageReference  :  " + " Location Url Null");
        }

        ImageView locationImageIv = (ImageView) findViewById(R.id.locationDetailsImageView);
        TextView locationNameTv = (TextView) findViewById(R.id.locationDetailsName);
        TextView locationGeoTv = (TextView) findViewById(R.id.locationDetailsGeo);
        TextView locationAddTv = (TextView) findViewById(R.id.locationDetailsAddress);
        TextView locationRatingTv = (TextView) findViewById(R.id.locationDetailsRating);

        Typeface roboto_light = Typeface.createFromAsset(getAssets(), "fonts/roboto_light.ttf");
        Typeface roboto_LightItalic = Typeface.createFromAsset(getAssets(), "fonts/roboto_LightItalic.ttf");
        Typeface roboto_Medium = Typeface.createFromAsset(getAssets(), "fonts/roboto_Medium.ttf");
        Typeface roboto_Regular = Typeface.createFromAsset(getAssets(), "fonts/roboto_Regular.ttf");
        Typeface roboto_Thin = Typeface.createFromAsset(getAssets(), "fonts/roboto_Thin.ttf");

        Button navigateMaps = (Button) findViewById(R.id.locationDetailsNav);

        navigateMaps.setTypeface(roboto_Regular);
        locationNameTv.setTypeface(roboto_Medium);
        locationGeoTv.setTypeface(roboto_LightItalic);
        locationAddTv.setTypeface(roboto_Thin);
        locationRatingTv.setTypeface(roboto_light);

        Prefs prefs = Prefs.with(MapsClusterDemoApplication.getInstance());

        String imageReferenceUrl = null;

        if (locationImageReference != null) {
            final String apiKey = AppConstants.API_KEYS.MAP_API_KEY_2;
            imageReferenceUrl = AppConstants.BASE_IMAGE_URL + "/maps/api/place/photo?" + locationImageReference + "&key=" + apiKey;

            Log.i(TAG, " Location Complete Image Url  :  " + imageReferenceUrl);

        }

        if (imageReferenceUrl != null) {
            Picasso.with(this).load(imageReferenceUrl).into(locationImageIv);
        } else {
            Picasso.with(this).load(locationImageUrl).into(locationImageIv);
        }

        locationGeoTv.setText("Location Geo  :  " + locationGeo != null ? locationGeo : " Location Geo Unavailable ");
        locationNameTv.setText("Location Name :  " + locationName != null ? locationName : " Location Name Unavailable ");
        locationAddTv.setText("Location Add  :  " + locationAddress != null ? locationAddress : " Location Address Unavailable ");
        locationRatingTv.setText("Location Rating  :  " + locationRating != null ? locationRating : "Location Rating Unavailable");

        navigateMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + locationGeo));
                startActivity(intent);
            }
        });
    }
}
