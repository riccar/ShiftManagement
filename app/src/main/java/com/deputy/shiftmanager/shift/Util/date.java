package com.deputy.shiftmanager.shift.Util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ricardo on 23/02/2017.
 */

public class date {

    private static final String LOG_TAG = date.class.getSimpleName();

    //Format any date in String according to inFormat and returns
    //a string date formatted in dd MMM yyyy HH:mm:ss
    public static String formatStringDate(String date) {
        DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat outFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        String outDate = null;
        try {
            Date tmpDate = inFormat.parse(date);
            outDate = outFormat.format(tmpDate);
        } catch (ParseException e) {
            Log.v(LOG_TAG, e.getMessage());
        }
        return outDate;
    }
}
