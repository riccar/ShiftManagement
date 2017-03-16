package com.deputy.shiftmanager.shift;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.adapter.RecyclerViewAdapter;
import com.deputy.shiftmanager.shift.model.Shift;
import com.deputy.shiftmanager.shift.data.DBHelper;
import com.deputy.shiftmanager.shift.rest.ApiClient;
import com.deputy.shiftmanager.shift.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.deputy.shiftmanager.shift.model.Shift.SHIFT_LIST;


/**
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ShiftListActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private final String LOG_TAG = ShiftListActivity.class.getSimpleName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static boolean TWO_PANES;
    private CoordinatorLayout coordLayout = null;
    private double longitude = 0.00000;
    private double latitude = 0.00000;
    private GoogleApiClient mGoogleApiClient;
    private RecyclerView recyclerViewShift = null;
    //define fab buttons and animations and boolean value to determine if fab icon is open
    private FloatingActionButton mFab, mFabStart, mFabStop;
    private Boolean isFabOpen = false;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        //Load fab buttons
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFabStart = (FloatingActionButton)findViewById(R.id.fabStart);
        mFabStop = (FloatingActionButton)findViewById(R.id.fabStop);
        //Load animations defined in res/anim folder
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        //Define OnClick listeners for fab buttons
        mFab.setOnClickListener(this);
        mFabStart.setOnClickListener(this);
        mFabStop.setOnClickListener(this);

        coordLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        if (findViewById(R.id.shift_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            TWO_PANES = true;
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Setting RecycleView layout
        //recyclerViewShift.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShift = (RecyclerView) findViewById(R.id.shift_list);

        //TODO: move getShift call to a headless fragment to avoid execution in UI thread
        if (isNetworkAvailable()) {

            getShifts();

        } else {
            //Show shifts from DB when no network connection is available

            List<Shift.ShiftItem> shiftList;// = new ArrayList<>();
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            shiftList = dbHelper.getShiftsFromDB();

            recyclerViewShift.setAdapter(new RecyclerViewAdapter(shiftList, this ));
            //next line commented for retrofit
            //setupRecyclerView((RecyclerView) recyclerView, shiftList);

            Snackbar.make(coordLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeLayout);

        //TODO: Fix issue at onRefresh when the layout change to dual pane for tablets
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // call your Refresh method here
                //TODO: Move into getShift the network validation and the shift returned from db
                if (isNetworkAvailable()) {

                    getShifts();

                } else {
                    //Show shifts from DB when no network connection is available

                    List<Shift.ShiftItem> shiftList;// = new ArrayList<>();
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    shiftList = dbHelper.getShiftsFromDB();

                    recyclerViewShift.setAdapter(new RecyclerViewAdapter(shiftList, getApplicationContext() ));
                    //next line commented for retrofit
                    //setupRecyclerView((RecyclerView) recyclerView, shiftList);

                    Snackbar.make(coordLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
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
                    updateShift("start");
                } else {
                    Snackbar.make(coordLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case R.id.fabStop:
                if (isNetworkAvailable()) {
                    updateShift("stop");
                } else {
                    Snackbar.make(coordLayout, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
        }
    }

    //Function to start the animation of the buttons when the fab button is open or not.
    private void animateFAB(){

        if(isFabOpen){
            mFab.startAnimation(rotate_backward);
            mFabStart.startAnimation(fab_close);
            mFabStop.startAnimation(fab_close);
            mFabStart.setClickable(false);
            mFabStop.setClickable(false);
            isFabOpen = false;
        } else {
            mFab.startAnimation(rotate_forward);
            mFabStart.startAnimation(fab_open);
            mFabStop.startAnimation(fab_open);
            mFabStart.setClickable(true);
            mFabStop.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(LOG_TAG, "Connected to Google API");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                Log.i(LOG_TAG, "Lat " + latitude + " Long " + longitude);
            } else {
                Log.i(LOG_TAG, "Last location is null ");

            }
        } else {

            //Request permission if not granted. Required since Android API 23
            int REQUEST_CODE_ASK_PERMISSIONS = 123;
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
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void updateShift(String apiCall) {
        //Getting today's date and time
        DateFormat df =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        String nowAsISO = df.format(new Date());

        JSONObject JSONQuery = new JSONObject();

        try {
            JSONQuery.put("time", nowAsISO);
            JSONQuery.put("latitude", Double.toString(latitude));
            JSONQuery.put("longitude", Double.toString(longitude));
        } catch (JSONException e) {
            Log.v(LOG_TAG, e.getMessage());
        }

        ApiInterface apiInterface =
                ApiClient.getClient().create(ApiInterface.class);
        Log.v(LOG_TAG, JSONQuery.toString());

        //Creating OkHTTP RequestBody to correctly parse JSON object as string
        RequestBody shiftData =
                RequestBody.create(MediaType.parse("text/plain"), JSONQuery.toString());

        if (apiCall.equals("start"))  {
            Call<String> call = apiInterface.startShift(shiftData);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //Due to the lack of a good response coding, use keyword from response message
                    if (response.body().indexOf("good") > 0) {
                        Snackbar.make(coordLayout, getString(R.string.shift_start_success),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(coordLayout, getString(R.string.shift_start_error),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // Log error here since request failed
                    Log.e(LOG_TAG, t.toString());
                }
            });
        }
        else { //Stop Shift
            Call<String> call = apiInterface.stopShift(shiftData);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //Due to the lack of a good response coding, use keyword from response message
                    if (response.body().indexOf("good") > 0) {
                        Snackbar.make(coordLayout, getString(R.string.shift_stop_success),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(coordLayout, getString(R.string.shift_stop_error),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // Log error here since request failed
                    Log.e(LOG_TAG, t.toString());
                }
            });
        }
    }

    //Get Shift. Executes an API call to get all the shifts and populate adapter
    private void getShifts() {
        ApiInterface apiInterface =
                ApiClient.getClient().create(ApiInterface.class);

        Call<ArrayList<Shift.ShiftItem>> call = apiInterface.getShifts();

        call.enqueue(new Callback<ArrayList<Shift.ShiftItem>>() {
            @Override
            public void onResponse(Call<ArrayList<Shift.ShiftItem>> call,
                                   Response<ArrayList<Shift.ShiftItem>> response) {

                if (response.body().size() > 0) {
                    ArrayList<Shift.ShiftItem> shifts = response.body();
                    //Assigning the list of shift to the static Shift attribute so it can be
                    //accessed from DetailFragment
                    Collections.sort(shifts);
                    SHIFT_LIST = shifts;
                    recyclerViewShift.setAdapter(new RecyclerViewAdapter(shifts, getApplicationContext()));
                    //Insert new shift in DB and update current shifts
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    dbHelper.insertShiftInDB(shifts);

                } else {
                    //TODO: Identify response for empty list and return error message
                }
            }
            @Override
            public void onFailure(Call<ArrayList<Shift.ShiftItem>> call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());
            }
        });
    }
}