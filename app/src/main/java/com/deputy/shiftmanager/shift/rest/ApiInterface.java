package com.deputy.shiftmanager.shift.rest;

import com.deputy.shiftmanager.shift.model.Shift;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    String SHA1 = "d4e7430f1534a12df46cedd1ac369935436dbb94  -";

    @Headers({"Content-Type: application/json", "Accept-Charset: UTF-8", "Authorization: Deputy " + SHA1})
    @GET("shifts")
    Call<ArrayList<Shift.ShiftItem>> getShifts();

    @Headers({"Content-Type: application/json", "Accept-Charset: UTF-8", "Authorization: Deputy " + SHA1})
    @POST("shift/start")
    Call<String> startShift(@Body RequestBody body);

    @Headers({"Content-Type: application/json", "Accept-Charset: UTF-8", "Authorization: Deputy " + SHA1})
    @POST("shift/end")
    Call<String> stopShift(@Body RequestBody body);


}

