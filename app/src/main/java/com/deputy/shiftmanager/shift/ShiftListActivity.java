package com.deputy.shiftmanager.shift;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ShiftListActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static boolean TWO_PANES;

    private View recyclerView;
    private double longitude = 0.00000;
    private double latitude = 0.00000;
    private final String LOG_TAG = ShiftListActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;// = new GoogleApiClient.Builder(this);
    private RecyclerView recyclerViewShift = null;// = (RecyclerView) findViewById(R.id.shift_list);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recyclerView = findViewById(R.id.shift_list);
        assert recyclerView != null;
        //setupRecyclerView((RecyclerView) recyclerView);

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

            Snackbar.make(recyclerView, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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
                    Shift.SHIFT_LIST = shifts;
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

    @Override
    public void onConnected(Bundle connectionHint) {
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
        } else {
            Log.e(LOG_TAG, Manifest.permission.ACCESS_FINE_LOCATION + " Permission NOT granted");

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_start_shift) {
            if (isNetworkAvailable()) {

                updateShift("start");

            } else {
                Snackbar.make(recyclerView, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return true;
        }

        if (id == R.id.menu_end_shift) {
            if (isNetworkAvailable()) {

                updateShift("stop");

            } else {
                Snackbar.make(recyclerView, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateShift(String apiCall) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //DateFormat df = DateFormat.getDateTimeInstance();// new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(tz);
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
                        Snackbar.make(recyclerView, getString(R.string.shift_start_success),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(recyclerView, getString(R.string.shift_start_error),
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
                        Snackbar.make(recyclerView, getString(R.string.shift_stop_success),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(recyclerView, getString(R.string.shift_stop_error),
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
}