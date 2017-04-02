/**
 * Provides some utility functions to parse date values
 */

package com.deputy.shiftmanager.shift.Util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.deputy.shiftmanager.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class date {

    private static final String LOG_TAG = date.class.getSimpleName();

    //Format any date in String according to inFormat and returns
    //a string date formatted in dd MMM yyyy HH:mm:ss
    public static String formatStringDate(String date, final Context context) {
        DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat outFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        String outDate = null;
        try {
            Date tmpDate = inFormat.parse(date);
            outDate = outFormat.format(tmpDate);
        } catch (ParseException e) {
            outDate = context.getString(R.string.date_parsing_error);
            e.printStackTrace();
        }
        return outDate;
    }
}
