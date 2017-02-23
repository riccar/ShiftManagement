package com.deputy.shiftmanager.shift.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.deputy.shiftmanager.shift.model.Shift;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.Shifts.CREATE_TABLE);

    }

    // Method is called during an upgrade of the database and when DB version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContract.Shifts.DELETE_TABLE);
        onCreate(db);
    }

    //Inserts new shift in a database so it's available when device has no network connection
    public void insertShiftInDB(SQLiteDatabase db, Shift.ShiftItem shiftItem) {


        String query = "insert or replace into Shifts (" + DBContract.Shifts._ID + "," +
                DBContract.Shifts.COLUMN_NAME_COL1 + "," + DBContract.Shifts.COLUMN_NAME_COL2 + "," +
                DBContract.Shifts.COLUMN_NAME_COL3 + "," + DBContract.Shifts.COLUMN_NAME_COL4 + "," +
                DBContract.Shifts.COLUMN_NAME_COL5 + "," + DBContract.Shifts.COLUMN_NAME_COL6 + "," +
                DBContract.Shifts.COLUMN_NAME_COL7 +  ") values (?,?,?,?,?,?,?,?)";


        db.execSQL(query, new String[]{shiftItem.id, shiftItem.start,shiftItem.end,
                shiftItem.startLatitude, shiftItem.startLongitude, shiftItem.endLatitude,
                shiftItem.endLongitude, shiftItem.image});




    }
}
