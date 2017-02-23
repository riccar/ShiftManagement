package com.deputy.shiftmanager.shift;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.adapter.RecyclerViewAdapter;
import com.deputy.shiftmanager.shift.model.Shift;
import com.deputy.shiftmanager.shift.model.Shift.ShiftItem;
import com.deputy.shiftmanager.shift.data.DBContract;
import com.deputy.shiftmanager.shift.data.DBHelper;
import com.deputy.shiftmanager.shift.network.GetShifts;
import com.deputy.shiftmanager.shift.network.StartStopShift;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


/**
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ShiftListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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



    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
       if (isNetworkAvailable()) {
           GetShifts getShifts = new GetShifts(this);
           //TODO: Update list of shift every few seconds. Add a preference to query refresh rate
           //TODO: Avoid getting shifts from the server when this activity starts (or changed horizontally)
           getShifts.execute("GET", "/shifts");
       } else {
            //If no network is available show the shifts from DB
           SQLiteDatabase db;
           DBHelper dbHelper = new DBHelper(this);
           db = dbHelper.getReadableDatabase();

           String[] fields =  {DBContract.Shifts._ID, DBContract.Shifts.COLUMN_NAME_COL1,
                   DBContract.Shifts.COLUMN_NAME_COL2, DBContract.Shifts.COLUMN_NAME_COL3,
                   DBContract.Shifts.COLUMN_NAME_COL4, DBContract.Shifts.COLUMN_NAME_COL5,
                   DBContract.Shifts.COLUMN_NAME_COL6, DBContract.Shifts.COLUMN_NAME_COL7};


           Cursor cursor = db.query(DBContract.Shifts.TABLE_NAME, fields,
                   null, //Condition
                   null, //Value for condition
                   null,null,null); //GroupBy, Having and OrderBy are not used.

           //cursor.moveToFirst();
           ArrayList<ShiftItem> shiftList = new ArrayList<>();
           //Shift shift = new Shift();
           while (cursor.moveToNext()) {
               ShiftItem shiftItem = new ShiftItem(cursor.getString(0),cursor.getString(1),
                       cursor.getString(2),cursor.getString(3),cursor.getString(4),
                       cursor.getString(5),cursor.getString(6),cursor.getString(7));
               //Add shift to list that populates Recycle View
               shiftList.add(shiftItem);
               //Add shift to HasMap so it can be accessed from the Details View.
               Shift.addItem(shiftItem);
           }
           cursor.close();
           db.close();

           setupRecyclerView((RecyclerView) recyclerView, shiftList);

       }


    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<ShiftItem> shiftItems) {

        recyclerView.setAdapter(new RecyclerViewAdapter(shiftItems, this));
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
                //getMobileLocation();
                StartStopShift startStopShift = new StartStopShift(recyclerView, this);
                startStopShift.execute("POST", "/shift/start", Double.toString(latitude), Double.toString(longitude));
                //Refresh the list of shift to show started shift in list
                GetShifts getShifts = new GetShifts(this);
                getShifts.execute("GET", "/shifts");
            } else {
                Snackbar.make(recyclerView, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }

            return true;
        }

        if (id == R.id.menu_end_shift) {
            if (isNetworkAvailable()) {
                //getMobileLocation();
                StartStopShift startStopShift = new StartStopShift(recyclerView, this);
                startStopShift.execute("POST", "/shift/end", Double.toString(latitude), Double.toString(longitude));
                //Refresh the list of shift to show details of ended shift
                GetShifts getShifts = new GetShifts(this);
                getShifts.execute("GET", "/shifts");
            } else {
                Snackbar.make(recyclerView, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
