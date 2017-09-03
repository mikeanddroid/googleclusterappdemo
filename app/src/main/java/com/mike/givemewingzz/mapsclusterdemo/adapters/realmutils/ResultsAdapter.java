package com.mike.givemewingzz.mapsclusterdemo.adapters.realmutils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mike.givemewingzz.mapsclusterdemo.R;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Geometry;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Location;
import com.mike.givemewingzz.mapsclusterdemo.model.data.Results;
import com.mike.givemewingzz.mapsclusterdemo.model.data.ViewPort;
import com.mike.givemewingzz.mapsclusterdemo.service.RealmController;
import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmResults;

public class ResultsAdapter extends RealmRecyclerViewAdapter<Results> {

    private static final String TAG = ResultsAdapter.class.getSimpleName();

    final Context context;
    private Realm realm;
    private EventListener eventListener;

    public ResultsAdapter(Context context) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_row_staggerred_layout, parent, false);
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
        holder.txFormattedAddress.setText(results.getFormattedAddress());
        holder.locationGeo.setText("Latitude : \n" + location.getLatitude() + "\n\nLongitude : \n" + location.getLongitude());
        Picasso.with(context).load(results.getIcon()).into(holder.locationImage);

        if (rating != null) {

            if (!rating.equals("null")) {
                holder.locationRating.setText("Location Rating  :  " + results.getRating());
            } else {
                holder.locationRating.setText("Location Rating Not Available");
            }

        } else {
            holder.locationRating.setText("Location Rating Not Available");
        }

        Animation animation;
        animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        holder.detailsContiner.startAnimation(animation);

        holder.detailsContiner.setOnClickListener(new View.OnClickListener() {
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
        TextView txFormattedAddress;
        ImageView locationImage;
        TextView locationGeo;
        TextView locationName;
        TextView locationRating;
        LinearLayout detailsContiner;


        MainViewHolder(View view) {
            super(view);
            txFormattedAddress = (TextView) view.findViewById(R.id.formatted_address);
            locationName = (TextView) view.findViewById(R.id.locationName);
            locationGeo = (TextView) view.findViewById(R.id.locationGeo);
            locationImage = (ImageView) view.findViewById(R.id.locationIcon);
            detailsContiner = (LinearLayout) view.findViewById(R.id.locationContainer);
            locationRating = (TextView) view.findViewById(R.id.locationRating);
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
