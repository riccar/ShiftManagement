package com.deputy.shiftmanager.shift.rest;

/**
 * Created by Ricardo on 27/02/2017.
 * Retrofit builder class
 */

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.adapter.RecyclerViewAdapter;
import com.deputy.shiftmanager.shift.data.DBHelper;
import com.deputy.shiftmanager.shift.model.Shift;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.deputy.shiftmanager.shift.model.Shift.SHIFT_LIST;

public class ApiClient extends AppCompatActivity {

    private static final String BASE_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc/";
    private static Retrofit retrofit = null;
    private static final String LOG_TAG = ApiClient.class.getSimpleName();

    public static Retrofit getClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        //setting log level
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // more interceptors can be added here

        // add logging as last interceptor
        httpClient.addInterceptor(logging);


        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static void updateShift(String apiCall, Double latitude, Double longitude,
                                   final CoordinatorLayout rootView, final Context context) {

        //mCoordLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);

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
                        Snackbar.make(rootView, context.getString(R.string.shift_start_success),
                               Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(rootView, context.getString(R.string.shift_start_error),
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
                        Snackbar.make(rootView, context.getString(R.string.shift_stop_success),
                               Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(rootView, context.getString(R.string.shift_stop_error),
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
    public static void getShifts(final RecyclerView recyclerViewShift, final Context context) {
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

                    //RecyclerView recyclerViewShift = (RecyclerView) findViewById(R.id.shift_list);
                    recyclerViewShift.setAdapter(new RecyclerViewAdapter(shifts, context));
                    //Insert new shift in DB and update current shifts
                    DBHelper dbHelper = new DBHelper(context);
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
