package com.deputy.shiftmanager.shift.rest;

/**
 * Created by Ricardo on 27/02/2017.
 * Retrofit builder class
 */

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc/";
    private static Retrofit retrofit = null;


    public static Retrofit getClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        //setting log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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
}
