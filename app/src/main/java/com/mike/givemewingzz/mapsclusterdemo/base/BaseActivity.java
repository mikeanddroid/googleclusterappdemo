package com.mike.givemewingzz.mapsclusterdemo.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mike.givemewingzz.mapsclusterdemo.R;
import com.mike.givemewingzz.mapsclusterdemo.model.data.BaseModel;
import com.mike.givemewingzz.mapsclusterdemo.presenter.SearchInteractor;
import com.mike.givemewingzz.mapsclusterdemo.service.ApiCall.FetchSearchedLocations;
import com.mike.givemewingzz.mapsclusterdemo.service.OttoHelper;
import com.mike.givemewingzz.mapsclusterdemo.ui.ClusterMapFragment;
import com.mike.givemewingzz.mapsclusterdemo.ui.MapListFragment;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.squareup.otto.Subscribe;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener, MaterialSearchBar.OnSearchActionListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private Fragment mapFragment;
    private Fragment listFragment;

    private ClusterMapFragment clusterMapFragment;
    private FloatingActionMenu actionMenu;

    public static final String IN_LIST_TAG = "IN_LIST_TAG";
    public static final String IN_MAP_TAG = "IN_MAP_TAG";
    public static final String IN_SEARCH_TAG = "IN_SEARCH_TAG";
    public static final String MAIN_ACTION_TAG = "MAIN_ACTION_TAG";

    @BindView(R.id.mapFragment)
    ViewGroup mapBaseHolder;

    @BindView(R.id.listFragment)
    ViewGroup listBaseHolder;

    @BindView(R.id.searchBar)
    MaterialSearchBar materialSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        setContentView(R.layout.activity_base);

        ButterKnife.bind(this);

        // Start Search bar

        materialSearchBar.setHint("Search places e.g. starbucks");
        materialSearchBar.setSpeechMode(true);

        //enable searchbar callbacks
        materialSearchBar.setOnSearchActionListener(this);
        //Inflate menu and setup OnMenuItemClickListener
        materialSearchBar.inflateMenu(R.menu.menu);
        materialSearchBar.getMenu().setOnMenuItemClickListener(this);

        materialSearchBar.setCardViewElevation(10);

        // End Search Bar

        // Setup floating action button
        setupFloatingActionButton();
        // End //

        clusterMapFragment = ClusterMapFragment.newInstance();
        FragmentTransaction clusterT = getSupportFragmentManager().beginTransaction();
        clusterT.replace(R.id.mapFragment, clusterMapFragment).commit();

        // Create list instance
        listFragment = MapListFragment.newInstance();
        FragmentTransaction listFragmentTransaction = getSupportFragmentManager().beginTransaction();
        listFragmentTransaction.replace(R.id.listFragment, listFragment).commit();

        setSlideAnimation(mapBaseHolder);
        setFadeAnimation(mapBaseHolder);

        materialSearchBar.addTextChangeListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, getClass().getSimpleName() + "beforeTextChanged : " + materialSearchBar.getText());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    Log.d(TAG, getClass().getSimpleName() + "onTextChanged : " + s);
                } else {
                    Log.d(TAG, getClass().getSimpleName() + "onTextChanged : Length less than 3 : Get detault search " + materialSearchBar.getText());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 3) {

                    Log.d(TAG, getClass().getSimpleName() + "afterTextChanged : " + materialSearchBar.getText());

                    FetchSearchedLocations fetchSearchedLocations = new FetchSearchedLocations();
                    fetchSearchedLocations.call(s.toString().trim(), new FetchSearchedLocations.OnResultsComplete() {
                        @Override
                        public void onResultsFetched(SearchInteractor.OnSearchFinished listener, BaseModel baseModel) {

//                            MapListFragment.newInstance().setItems(baseModel);

                            clusterMapFragment.fetchMapData(baseModel);
                            clusterMapFragment.onDataComplete();
                        }

                        @Override
                        public void onResultsQueryLimit(String errorMessage) {

                            clusterMapFragment.onQueryExceeded();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BaseActivity.this);
                            alertDialogBuilder.setMessage("Query Limit Exceeded");
                            alertDialogBuilder.setCancelable(false);

                            alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    BaseActivity.this.finish();
                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();

                            if (!alertDialog.isShowing()) {
                                if (!isFinishing()) {

                                    if (alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    alertDialog.show();

                                }
                            }

                        }
                    });

                } else {
                    Log.d(TAG, getClass().getSimpleName() + "afterTextChanged : Length less than 3 : Get detault search " + materialSearchBar.getText());
                }

            }
        });

        // Hide List Fragment by default
        getSupportFragmentManager().beginTransaction().hide(listFragment).commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // UnRegister Otto
        OttoHelper.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Register Otto
        OttoHelper.register(this);
        super.onResume();
    }

    @Subscribe
    public void onResultSuccess(FetchSearchedLocations.SeachedSuccessEvent successEvent) {
        Log.d(TAG, "onResultSuccess : " + TAG + " : Status : " + successEvent.getBaseModel().getStatus());
//        clusterMapFragment.fetchMapData(successEvent.getBaseModel());
    }

    @Subscribe
    public void onResultFailure(FetchSearchedLocations.SearchedFailureEvent failureEvent) {
        Log.d(TAG, "onResultSuccess  : " + TAG + " : Status : " + failureEvent);
        Toast.makeText(this, "BaseActivity : Failed to retrieve data .. \n" + failureEvent.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_places);

        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.getTag().equals(IN_LIST_TAG)) {

            setSlideAnimation(mapBaseHolder);
            setFadeAnimation(mapBaseHolder);

            if (actionMenu.isOpen()) {
                actionMenu.close(true);
            }

            getSupportFragmentManager().beginTransaction().hide(clusterMapFragment).commit();
            getSupportFragmentManager().beginTransaction().show(listFragment).commit();

        } else if (v.getTag().equals(IN_MAP_TAG)) {

            setSlideAnimation(listBaseHolder);
            setFadeAnimation(listBaseHolder);

            if (actionMenu.isOpen()) {
                actionMenu.close(true);
            }

            getSupportFragmentManager().beginTransaction().show(clusterMapFragment).commit();
            getSupportFragmentManager().beginTransaction().hide(listFragment).commit();
        }

    }

    private void setSlideAnimation(ViewGroup container) {

        if (Build.VERSION.SDK_INT >= 21) {

            // Shared element transitions
            // getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_transition));
            // End //

            // Trans //
            // Transition for Landing page when it slides in.
            TransitionManager.beginDelayedTransition(container,
                    new TransitionSet()
                            .addTransition(new Slide(Gravity.END).setDuration(500)));
            // Trans //

        } else {

            TransitionManager.beginDelayedTransition(container,
                    new TransitionSet()
                            .addTransition(new Slide(Gravity.END).setDuration(500)));

        }

    }

    private void setFadeAnimation(ViewGroup container) {

        if (Build.VERSION.SDK_INT >= 21) {

            // Trans //
            // Transition for Landing page when it slides in.
            TransitionManager.beginDelayedTransition(container,
                    new TransitionSet()
                            .addTransition(new Fade().setDuration(1000)));
            // Trans //

        } else {

            TransitionManager.beginDelayedTransition(container,
                    new TransitionSet()
                            .addTransition(new Fade().setDuration(1000)));

        }

    }

    private void setupFloatingActionButton() {

        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.ic_settings_black_48dp);
        imageView.setTag(MAIN_ACTION_TAG);
        imageView.setOnClickListener(this);

        FloatingActionButton actionButton = new FloatingActionButton.Builder(this).setContentView(imageView).build();

        ImageView mapsView = new ImageView(this);
        mapsView.setBackgroundResource(R.drawable.common_google_signin_btn_icon_light);

        ImageView arrangeInGrid = new ImageView(this);
        arrangeInGrid.setBackgroundResource(R.drawable.ic_dashboard_black_24dp);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        SubActionButton inMap = itemBuilder.setContentView(mapsView).build();
        SubActionButton inList = itemBuilder.setContentView(arrangeInGrid).build();

        inMap.setPadding(4, 4, 4, 4);
        inList.setPadding(4, 4, 4, 4);

        inMap.setTag(IN_MAP_TAG);
        inList.setTag(IN_LIST_TAG);

        inMap.setOnClickListener(this);
        inList.setOnClickListener(this);

        actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(inMap)
                .addSubActionView(inList)
                .attachTo(actionButton)
                .build();

        // End of setup

    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        String s = enabled ? "enabled" : "disabled";
        Toast.makeText(BaseActivity.this, "Search " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {

    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.VIEW_INVISIBLE:
                materialSearchBar.disableSearch();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
