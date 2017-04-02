/**
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */

package com.deputy.shiftmanager.shift;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.adapter.RecyclerViewAdapter;
import com.deputy.shiftmanager.shift.model.Shift;
import com.deputy.shiftmanager.shift.data.DBHelper;
import com.deputy.shiftmanager.shift.rest.ApiClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class ShiftListActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private final String LOG_TAG = ShiftListActivity.class.getSimpleName();

    //Whether or not the activity is in two-pane mode, i.e. running on a tablet device
    public static boolean TWO_PANES;
    private CoordinatorLayout coordinatorLayout = null;
    private double longitude = 0.00000;
    private double latitude = 0.00000;
    private GoogleApiClient googleApiClient;
    private RecyclerView recyclerViewShift = null;
    //define fab buttons and animations and boolean value to determine if fab icon is open
    private FloatingActionButton fab, fabStart, fabStop;
    private Boolean isFabOpen = false;
    private Animation fabOpenAnim,fabCloseAnim,rotateForwardAnim,rotateBackwardAnim;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        //Load fab buttons
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabStart = (FloatingActionButton)findViewById(R.id.fabStart);
        fabStop = (FloatingActionButton)findViewById(R.id.fabStop);
        //Load animations defined in res/anim folder
        fabOpenAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabCloseAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotateForwardAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotateBackwardAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        //Define OnClick listeners for fab buttons
        fab.setOnClickListener(this);
        fabStart.setOnClickListener(this);
        fabStop.setOnClickListener(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        if (findViewById(R.id.shift_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            TWO_PANES = true;
        }

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Setting RecycleView layout

        recyclerViewShift = (RecyclerView) findViewById(R.id.shift_list);

        //TODO: move getShift call to a headless fragment to avoid execution in UI thread
        if (isNetworkAvailable()) {

            ApiClient.getShifts(recyclerViewShift, coordinatorLayout,  getApplicationContext());

        } else {
            //Show shifts from DB when no network connection is available

            List<Shift.ShiftItem> shiftList;
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            shiftList = dbHelper.getShiftsFromDB();

            recyclerViewShift.setAdapter(new RecyclerViewAdapter(shiftList, this));
            //next line commented for retrofit
            //setupRecyclerView((RecyclerView) recyclerView, shiftList);

            Snackbar.make(coordinatorLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeLayout);

        //TODO: Fix issue at onRefresh when the layout change to dual pane for tablets
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                if (isNetworkAvailable()) {

                    ApiClient.getShifts(recyclerViewShift, coordinatorLayout, getApplicationContext());

                } else {
                    //Show shifts from DB when no network connection is available
                    List<Shift.ShiftItem> shiftList;
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    shiftList = dbHelper.getShiftsFromDB();

                    recyclerViewShift.setAdapter(new RecyclerViewAdapter(shiftList,
                            getApplicationContext()));

                    Snackbar.make(coordinatorLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fabStart:
                if (isNetworkAvailable()) {
                    ApiClient.updateShift("start", latitude, longitude, coordinatorLayout, this);
                } else {
                    Snackbar.make(coordinatorLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case R.id.fabStop:
                if (isNetworkAvailable()) {
                    ApiClient.updateShift("stop", latitude, longitude, coordinatorLayout, this);
                } else {
                    Snackbar.make(coordinatorLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
        }
    }

    /** Starts the animation of the buttons when the fab button is open and closed.*/
    private void animateFAB(){

        if(isFabOpen){
            fab.startAnimation(rotateBackwardAnim);
            fabStart.startAnimation(fabCloseAnim);
            fabStop.startAnimation(fabCloseAnim);
            fabStart.setClickable(false);
            fabStop.setClickable(false);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotateForwardAnim);
            fabStart.startAnimation(fabOpenAnim);
            fabStop.startAnimation(fabOpenAnim);
            fabStart.setClickable(true);
            fabStop.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }

    /** Triggered when the GoogleAPI connection is stabilised.
     * Saves the current Latitude and Longitude coordinates
     * */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(LOG_TAG, "Connected to Google API");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if (lastLocation != null) {
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                Log.i(LOG_TAG, "Lat " + latitude + " Long " + longitude);
            } else {
                Log.i(LOG_TAG, "Last location is null ");

            }
        } else {

            //Request permission if not granted. Required since Android API 23
            int REQUEST_CODE_ASK_PERMISSIONS = 10;
            ActivityCompat.requestPermissions( this,
                    new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    REQUEST_CODE_ASK_PERMISSIONS);
            Log.e(LOG_TAG, Manifest.permission.ACCESS_COARSE_LOCATION + " Permission NOT granted");

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(LOG_TAG, "Google API connection Error: " + result.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int x) {

    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}