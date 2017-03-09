package com.deputy.shiftmanager.shift.network;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.adapter.RecyclerViewAdapter;
import com.deputy.shiftmanager.shift.data.DBHelper;
import com.deputy.shiftmanager.shift.model.Shift;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ricardo on 22/02/2017.
 */

class GetShifts extends AsyncTask<String, Void, Shift.ShiftItem[]> {

    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    private HttpsURLConnection urlConnection = null;
    private BufferedReader reader = null;
    // Will contain the raw JSON response as a string.
    private String jsonStr = null;

    private final String LOG_TAG = GetShifts.class.getSimpleName();

    private final Activity mContext;

    private GetShifts(Activity context) {
        mContext = context;
    }


    @Override
    protected Shift.ShiftItem[] doInBackground(String... params) {

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

            //Log.v(LOG_TAG, "POST Code " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage() + " " + urlConnection.getErrorStream());
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {


                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder buffer = new StringBuilder();

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
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();


            } else {
                Log.d(LOG_TAG, urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage() + " " + urlConnection.getErrorStream());
            }

            Log.d(LOG_TAG, "JSON String: " + jsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
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

    private Shift.ShiftItem[] getShiftItemsFromJson(String shiftsJsonStr) throws JSONException {


        JSONArray shiftArray = new JSONArray(shiftsJsonStr);
        int totalShifts = shiftArray.length();

        Shift.ShiftItem shiftItemArr[] = new Shift.ShiftItem[totalShifts];

        for (int i = 0; i < totalShifts; i++) {

            //Get the JSON object representing the shift
            JSONObject shiftJsonObj = shiftArray.getJSONObject(i);
            Shift.ShiftItem shitItem = new Shift.ShiftItem(shiftJsonObj.getString("id"),
                    shiftJsonObj.getString("start"), shiftJsonObj.getString("end"),
                    shiftJsonObj.getString("startLatitude"), shiftJsonObj.getString("startLongitude"),
                    shiftJsonObj.getString("endLatitude"), shiftJsonObj.getString("endLongitude"),
                    shiftJsonObj.getString("image"));
            shiftItemArr[i] = shitItem;
        }
        return shiftItemArr;
    }

    @Override
    protected void onPostExecute(Shift.ShiftItem[] result) {
        if (result != null) {

            //Creating the list of ShiftItems to populate RecycleView adapter
            List<Shift.ShiftItem> shiftList;// = new ArrayList<ShiftItem>();
            shiftList = Arrays.asList(result);

            //create a recycle view and set the adapter
            //shift_list is the ID of the RecycleView view defined in shift_list.xml
            RecyclerView recyclerView = (RecyclerView) mContext.findViewById(R.id.shift_list);
            recyclerView.setAdapter(new RecyclerViewAdapter(shiftList, mContext));

            //Adding the shifts so they can be found in ShiftDetailFragment
            SQLiteDatabase db;
            DBHelper dbHelper = new DBHelper(mContext);
            db = dbHelper.getWritableDatabase();

            for (Shift.ShiftItem aResult : result) {
                Shift.addItem(aResult);
                //Below function was refactored, so it's commented since this class is not in use
                //dbHelper.insertShiftInDB(db, aResult);
            }

            //updateShiftsDB(ShiftItem[] result);

        }
    }
}//End AsyncTask Class
