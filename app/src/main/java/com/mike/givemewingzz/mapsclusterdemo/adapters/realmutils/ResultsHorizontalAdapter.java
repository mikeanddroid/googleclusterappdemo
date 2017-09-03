package com.mike.givemewingzz.mapsclusterdemo.adapters.realmutils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.mike.givemewingzz.mapsclusterdemo.R;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Geometry;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Location;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Results;
import com.mike.givemewingzz.mapsclusterdemo.model.data.ViewPort;
import com.mike.givemewingzz.mapsclusterdemo.service.RealmController;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by GiveMeWingzz on 9/2/2017.
 */

public class ResultsHorizontalAdapter extends RealmRecyclerViewAdapter<Results> {

    private static final String TAG = ResultsAdapter.class.getSimpleName();

    final Context context;
    private Realm realm;
    private EventListener eventListener;

    public ResultsHorizontalAdapter(Context context) {
        this.context = context;
        realm = RealmController.getInstance().getRealm();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    // create new views (invoked by the layout manager)
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a new card view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_row_view, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

// get Results
        final Results results = getItem(position);

        final Geometry geometry = results.getGeometry();
        final Location location = geometry.getPlaceLocation();
        final ViewPort viewPort = geometry.getViewPort();

// cast the generic view holder to our specific one
        final MainViewHolder holder = (MainViewHolder) viewHolder;

        Log.d(TAG, " Geometry Location : lat : "
                + location.getLatitude() + ": lon : "
                + location.getLongitude());

        Log.d(TAG, " Geometry ViewPort : lat NE : "
                + viewPort.getNorthEast().getNorthEastlatitude() + ": lon SW : "
                + viewPort.getSouthWest().getSouthWestLatitude());

        String rating = results.getRating();

        holder.locationName.setText(results.getLocationName());

        if (rating != null) {

            if (!rating.equals("null")) {
                Log.d(TAG, " Location Rating : " + rating);
                holder.locationRating.setText("" + rating);
            } else {
                holder.locationRating.setText("0");
            }

        } else {
            Log.d(TAG, " Location Rating : " + "NULL");
            holder.locationRating.setText("0");
        }

        Animation animation;
        animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        holder.detailsContiner.startAnimation(animation);

        holder.locationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmResults<Results> realmResults = realm.where(Results.class).findAll();
                Results r = realmResults.get(position);
                eventListener.onItemClick(position, r);
            }
        });

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    private class MainViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        TextView locationRating;
        ConstraintLayout detailsContiner;


        MainViewHolder(View view) {
            super(view);
            locationName = (TextView) view.findViewById(R.id.horizontal_location_name);
            detailsContiner = (ConstraintLayout) view.findViewById(R.id.horizontal_view_holder);
            locationRating = (TextView) view.findViewById(R.id.horizontal_location_rating);

            Typeface roboto_Medium = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_Medium.ttf");
            locationName.setTypeface(roboto_Medium);
        }

    }

    public void swapData(BaseModel baseModel) {

        if (baseModel == null || baseModel.getResults().size() == 0) {
            return;
        }

        getRealmAdapter().updateData(baseModel.getResults());

    }

    public interface EventListener {
        void onItemClick(final int position, Results results);
    }

}