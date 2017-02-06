package com.deputy.shiftmanager;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.TextView;
import com.deputy.shiftmanager.dummy.DummyContent;
import com.deputy.shiftmanager.Shift.ShiftItem;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


/**
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ShiftListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    public View recyclerView;
    private LocationManager locationManager;
    private String provider;
    private double longitude = 0.00000;
    private double latitude = 0.00000;
    private LocationListener locationListener = null;
    private final String LOG_TAG = com.deputy.shiftmanager.ShiftListActivity.class.getSimpleName();

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
            mTwoPane = true;
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        provider = locationManager.getBestProvider(criteria, true);

        // Register the listener with the Location Manager to receive location updates
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Min Time and Distance is set to 0 for testing purposes.
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
            }
            /*Location location = locationManager.getLastKnownLocation(provider);
            longitude = location.getLongitude();
            latitude = location.getLatitude();*/
        }


    }

    void makeUseOfNewLocation(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && provider != null) {
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
       if (isNetworkAvailable()) {
           ShiftController shiftController = new ShiftController(this);
           shiftController.execute("GET", "/shifts");
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
           Shift shift = new Shift();
           while (cursor.moveToNext()) {
               ShiftItem shiftItem = new ShiftItem(cursor.getString(0),cursor.getString(1),
                       cursor.getString(2),cursor.getString(3),cursor.getString(4),
                       cursor.getString(5),cursor.getString(6),cursor.getString(7));
               //Add shift to list that populates Recycle View
               shiftList.add(shiftItem);
               //Add shift to HasMap so it can be accessed from the Details View.
               shift.addItem(shiftItem);
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

    private void showOfflineShiftsFromDB() {
        //

    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<ShiftItem> shiftItems) {

        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(shiftItems));
    }


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<ShiftItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<ShiftItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shift_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).start);


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ShiftDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        ShiftDetailFragment fragment = new ShiftDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.shift_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ShiftDetailActivity.class);
                        intent.putExtra(ShiftDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;

            public ShiftItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);

            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }


        }
    }

    public class ShiftController extends AsyncTask<String, Void, ShiftItem[]> {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        private final String LOG_TAG = ShiftController.class.getSimpleName();

        private Context mContext;

        public ShiftController(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        protected ShiftItem[] doInBackground(String... params) {

            try {
                final String DEPUTY_API_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc";
                final String CHARSET = "UTF-8";
                final String CONTENT_TYPE = "application/json";
                final String SHA1 = "d4e7430f1534a12df46cedd1ac369935436dbb94  -";
                String method = params[0];
                String call = params[1];


                URL url = new URL(DEPUTY_API_URL + call);
                Log.v(LOG_TAG, "Built URL " + url);
                // Create the request and open the connection
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.addRequestProperty("Authorization", "Deputy " + SHA1);
                urlConnection.setRequestProperty("Accept-Charset", CHARSET);
                urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE);
                urlConnection.setRequestMethod(method);

                Log.v(LOG_TAG, "POST Code " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage() + " " + urlConnection.getErrorStream());
                if (urlConnection.getResponseCode() == urlConnection.HTTP_OK) {


                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();

                    StringBuffer buffer = new StringBuffer();

                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    jsonStr = buffer.toString();


                } else {
                    Log.v(LOG_TAG, urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage() + " " + urlConnection.getErrorStream());
                }

                Log.v(LOG_TAG, "JSON String: " + jsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            //Return HERE JSON response
            //If no error is caught then, the following try is executed
            try {
                return getShiftItemsFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the request.
            return null;

        }//end doInBackground

        private ShiftItem[] getShiftItemsFromJson(String shiftsJsonStr) throws JSONException {


            JSONArray shiftArray = new JSONArray(shiftsJsonStr);
            int totalShifts = shiftArray.length();

            ShiftItem shiftItemArr[] = new ShiftItem[totalShifts];

            for (int i = 0; i < totalShifts; i++) {

                //Get the JSON object representing the shift
                JSONObject shiftJsonObj = shiftArray.getJSONObject(i);
                ShiftItem shitItem = new ShiftItem(shiftJsonObj.getString("id"),
                        shiftJsonObj.getString("start"), shiftJsonObj.getString("end"),
                        shiftJsonObj.getString("startLatitude"), shiftJsonObj.getString("startLongitude"),
                        shiftJsonObj.getString("endLatitude"), shiftJsonObj.getString("endLongitude"),
                        shiftJsonObj.getString("image"));
                shiftItemArr[i] = shitItem;
            }
            return shiftItemArr;
        }

        @Override
        protected void onPostExecute(ShiftItem[] result) {
            if (result != null) {

                //Creating the list of ShiftItems to populate RecycleView adapter
                List<ShiftItem> shiftList;// = new ArrayList<ShiftItem>();
                shiftList = Arrays.asList(result);
                setupRecyclerView((RecyclerView) recyclerView, shiftList);

                //Adding the shifts so they can be found in ShiftDetailFragment
                Shift shift = new Shift();
                for (int i = 0; i < result.length; i++) {
                    shift.addItem(result[i]);
                    insertShiftInDB(result[i]);
                }

                //updateShiftsDB(ShiftItem[] result);

            }
        }
    }//End AsyncTast Class

    private void insertShiftInDB(ShiftItem shiftItem) {
        SQLiteDatabase db;
        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        //Delete all the shifts from DB
        //db.delete(DBContract.Shifts.TABLE_NAME, null, null);
        String query = "insert or replace into Shifts (" + DBContract.Shifts._ID + "," +
                DBContract.Shifts.COLUMN_NAME_COL1 + "," + DBContract.Shifts.COLUMN_NAME_COL2 + "," +
                DBContract.Shifts.COLUMN_NAME_COL3 + "," + DBContract.Shifts.COLUMN_NAME_COL4 + "," +
                DBContract.Shifts.COLUMN_NAME_COL5 + "," + DBContract.Shifts.COLUMN_NAME_COL6 + "," +
                DBContract.Shifts.COLUMN_NAME_COL7 +  ") values (?,?,?,?,?,?,?,?)";


        db.execSQL(query, new String[]{shiftItem.id, shiftItem.start,shiftItem.end,
                shiftItem.startLatitude, shiftItem.startLongitude, shiftItem.endLatitude,
                shiftItem.endLongitude, shiftItem.image});

        db.close();

        //Insert shifts
       /* ContentValues values = new ContentValues();
        values.put(DBContract.Shifts.COLUMN_NAME_COL1, desc); //Description
        values.put(DBContract.Game.COLUMN_NAME_COL2, price); //endDate
        values.put(DBContract.Game.COLUMN_NAME_COL3, endDate); //endDate
        values.put(DBContract.Game.COLUMN_NAME_COL4, endCals); //endCalories
        values.put(DBContract.Game.COLUMN_NAME_COL5, 0); //moderated
        values.put(DBContract.Game.COLUMN_NAME_COL6, 0); //locked
        values.put(DBContract.Game.COLUMN_NAME_COL7, endHP); //End points
        values.put(DBContract.Game.COLUMN_NAME_COL8, 1); //Game started: 1 = Started

        // Insert the new game, returning the primary key value of the new row
        newRowId = db.insert(
                DBContract.Game.TABLE_NAME,
                null,
                values);*/

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
                StartStopShift startStopShift = new StartStopShift(recyclerView, this);
                startStopShift.execute("POST", "/shift/start", Double.toString(latitude), Double.toString(longitude));
                //Refresh the list of shift to show started shift in list
                ShiftController shiftController = new ShiftController(this);
                shiftController.execute("GET", "/shifts");
            } else {
                Snackbar.make(recyclerView, getString(R.string.no_network_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showOfflineShiftsFromDB();

            }

            return true;
        }

        if (id == R.id.menu_end_shift) {
            StartStopShift startStopShift = new StartStopShift(recyclerView, this);
            startStopShift.execute("POST", "/shift/end", Double.toString(latitude), Double.toString(longitude));
            //Refresh the list of shift to show details of ended shift
            ShiftController shiftController = new ShiftController(this);
            shiftController.execute("GET", "/shifts");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
