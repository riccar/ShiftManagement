package com.deputy.shiftmanager.shift.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.deputy.shiftmanager.shift.model.Shift;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private final Context context;

    public DBHelper(Context appContext) {
        super(appContext, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
        context = appContext;
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
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();

        //db.execSQL("delete from " + DBContract.Shifts.TABLE_NAME);
        //db.delete(DBContract.Shifts.TABLE_NAME,null,null);
        String query = "insert or replace into " +
                DBContract.Shifts.TABLE_NAME + " (" + DBContract.Shifts._ID + "," +
            DBContract.Shifts.COLUMN_NAME_COL1 + "," + DBContract.Shifts.COLUMN_NAME_COL2 + "," +
            DBContract.Shifts.COLUMN_NAME_COL3 + "," + DBContract.Shifts.COLUMN_NAME_COL4 + "," +
            DBContract.Shifts.COLUMN_NAME_COL5 + "," + DBContract.Shifts.COLUMN_NAME_COL6 + "," +
            DBContract.Shifts.COLUMN_NAME_COL7 +  ") values (?,?,?,?,?,?,?,?)";

        for (Shift.ShiftItem shiftItem : shifts) {

            db.execSQL(query, new String[]{String.valueOf(shiftItem.id), shiftItem.start,shiftItem.end,
                    shiftItem.startLatitude, shiftItem.startLongitude, shiftItem.endLatitude,
                    shiftItem.endLongitude, shiftItem.image});
        }
        db.close();

    }

    public List<Shift.ShiftItem> getShiftsFromDB() {

        SQLiteDatabase db;
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getReadableDatabase();

        String[] fields =  {DBContract.Shifts._ID, DBContract.Shifts.COLUMN_NAME_COL1,
                DBContract.Shifts.COLUMN_NAME_COL2, DBContract.Shifts.COLUMN_NAME_COL3,
                DBContract.Shifts.COLUMN_NAME_COL4, DBContract.Shifts.COLUMN_NAME_COL5,
                DBContract.Shifts.COLUMN_NAME_COL6, DBContract.Shifts.COLUMN_NAME_COL7};


        Cursor cursor = db.query(DBContract.Shifts.TABLE_NAME, fields,
                null, //Condition
                null, //Value for condition
                null,null, DBContract.Shifts._ID + " DESC"); //GroupBy, Having and OrderBy

        ArrayList<Shift.ShiftItem> shiftList = new ArrayList<>();

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
