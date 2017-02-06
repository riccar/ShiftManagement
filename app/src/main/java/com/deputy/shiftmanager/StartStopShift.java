package com.deputy.shiftmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ricardo on 5/02/2017.
 * Handle the POST start and stop shifts using AsyncTask
 */

public class StartStopShift extends AsyncTask<String, Void, Boolean> {

    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    HttpsURLConnection urlConnection = null;
    BufferedReader reader = null;

    // Will contain the raw JSON response as a string.
    String jsonStr = null;

    private final String LOG_TAG = com.deputy.shiftmanager.ShiftListActivity.ShiftController.class.getSimpleName();
    //call: /shift/start or /shift/stop
    private String call;
    private View mContextView;
    private Context mContext;

    public StartStopShift(View mContext, Context context) {
        this.mContextView = mContext;
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            final String DEPUTY_API_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc";
            final String CHARSET = "UTF-8";
            final String CONTENT_TYPE = "application/json";
            final String SHA1 = "d4e7430f1534a12df46cedd1ac369935436dbb94  -";
            String method = params[0];
            call = params[1];


            URL url = new URL(DEPUTY_API_URL + call);
            Log.v(LOG_TAG, "Built URL " + url);
            // Create the request and open the connection
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Authorization", "Deputy " + SHA1);
            urlConnection.setRequestProperty("Accept-Charset", CHARSET);
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE);
            urlConnection.setRequestMethod(method);

            //Log.v(LOG_TAG, "POST Code " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage() + " " + urlConnection.getErrorStream());

            //Get device location and current time
            String latitude = params[2];
            String longitude = params[3];

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date());

            JSONObject JSONQuery = new JSONObject();
            //"2017-02-17T06:37:57+00:00"
            try {
                JSONQuery.put("time", nowAsISO);
                JSONQuery.put("latitude", latitude);
                JSONQuery.put("longitude", longitude);


            } catch (JSONException e) {
                Log.v(LOG_TAG, e.getMessage());
            }

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(JSONQuery.toString());
            writer.flush();
            writer.close();
            os.close();


            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return false;
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
                return false;
            }
            jsonStr = buffer.toString();

            Log.v(LOG_TAG, "JSON String: " + jsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attempting
            // to parse it.
            return false;
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

        //Return true if the response doesn't include the word "Nope" which means the
        //the shift was created successfully
        return jsonStr.indexOf("Nope") < 0;



    }//end doInBackground

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        String message;
        if (result) {
            if (call == "/shift/start") {
                message = mContext.getString(R.string.shift_start_success);
            } else {
                message = mContext.getString(R.string.shift_stop_success);
            }
        } else {
            if (call == "/shift/start") {
                message = mContext.getString(R.string.shift_start_error);
            } else {
                message = mContext.getString(R.string.shift_stop_error);
            }
        }
        Snackbar.make(mContextView, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}//End AsyncTask Class

