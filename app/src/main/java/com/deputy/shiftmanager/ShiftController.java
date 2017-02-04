package com.deputy.shiftmanager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ricardo on 3/02/2017.
 */

/**
 * An asynchronous task that handles the API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class ShiftController extends AsyncTask<String, Void, String[]> {

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
    protected String[] doInBackground(String... params) {

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

                if (call == "/shift/start" || call == "/shift/end") {
                    Location location = null;
                    LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                    if (mContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }

                    double longitude = -33.8459829;//location.getLongitude();
                    double latitude = 152.1546899;//location.getLatitude();


                    JSONObject JSONQuery = new JSONObject();

                    try {
                        JSONQuery.put("time", "2017-02-17T06:37:57+00:00");
                        JSONQuery.put("latitude", Double.toString(longitude));
                        JSONQuery.put("longitude", Double.toString(latitude));


                    } catch (JSONException e) {
                        Log.v(LOG_TAG, "POST Code " + e.getMessage());
                    }

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("time", "2017-02-17T06:35:57+00:00")
                            .appendQueryParameter("latitude", Double.toString(longitude))
                            .appendQueryParameter("longitude", Double.toString(latitude));
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(JSONQuery.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                }

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

        //TODO: Return HERE JSON response

        // This will only happen if there was an error getting or parsing the request.
        return null;
    }//end doInBackground


}
