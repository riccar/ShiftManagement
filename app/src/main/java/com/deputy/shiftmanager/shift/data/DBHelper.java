package com.deputy.shiftmanager.shift.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.deputy.shiftmanager.shift.model.Shift;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    final Context mContext;

    public DBHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
        mContext = context;
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
    public void insertShiftInDB(ArrayList<Shift.ShiftItem> shifts) {
        SQLiteDatabase db;
        DBHelper dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();

        String query = "insert or replace into Shifts (" + DBContract.Shifts._ID + "," +
            DBContract.Shifts.COLUMN_NAME_COL1 + "," + DBContract.Shifts.COLUMN_NAME_COL2 + "," +
            DBContract.Shifts.COLUMN_NAME_COL3 + "," + DBContract.Shifts.COLUMN_NAME_COL4 + "," +
            DBContract.Shifts.COLUMN_NAME_COL5 + "," + DBContract.Shifts.COLUMN_NAME_COL6 + "," +
            DBContract.Shifts.COLUMN_NAME_COL7 +  ") values (?,?,?,?,?,?,?,?)";

        for (Shift.ShiftItem shiftItem : shifts) {

            db.execSQL(query, new String[]{shiftItem.id, shiftItem.start,shiftItem.end,
                    shiftItem.startLatitude, shiftItem.startLongitude, shiftItem.endLatitude,
                    shiftItem.endLongitude, shiftItem.image});
        }
        db.close();

    }

    public List<Shift.ShiftItem> getShiftsFromDB() {

        SQLiteDatabase db;
        DBHelper dbHelper = new DBHelper(mContext);
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
        ArrayList<Shift.ShiftItem> shiftList = new ArrayList<>();
        //Shift shift = new Shift();
        while (cursor.moveToNext()) {
            Shift.ShiftItem shiftItem = new Shift.ShiftItem(cursor.getString(0),cursor.getString(1),
                    cursor.getString(2),cursor.getString(3),cursor.getString(4),
                    cursor.getString(5),cursor.getString(6),cursor.getString(7));
            //Add shift to list that populates Recycle View
            shiftList.add(shiftItem);
            //Add shift to SHIFT_LIST so it can be accessed from the Details View.
            //If device has no network before app starts, Shift.SHIFT_LIST will be empty

            Shift.addShift(shiftItem);
        }
        cursor.close();
        db.close();

        return shiftList;
    }
}
