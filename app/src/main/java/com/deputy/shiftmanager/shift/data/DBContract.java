/**
 * A DB Contract class defining all the tables names, fields and data types
 */

package com.deputy.shiftmanager.shift.data;

import android.provider.BaseColumns;

class DBContract {


    public static final  int    DATABASE_VERSION    = 2;
    public static final String DATABASE_NAME       = "ShiftManager.db";
    private static final String TEXT_TYPE           = " TEXT";
    //Types not in used. Uncomment when required
    //private static final String INT_TYPE            = " INTEGER";
    //private static final String REAL_TYPE           = " REAL";
    //private static final String NUMERIC_TYPE        = " NUMERIC";
    private static final String COMMA_SEP           = ",";


    // To prevent someone from accidentally instantiating the contract class,
    // give it a private and empty constructor.
    private DBContract() {}

    /************
     * Table Shifts
     ***********/
    public static abstract class Shifts implements BaseColumns {
        public static final String TABLE_NAME       = "Shifts";
        public static final String COLUMN_NAME_COL1 = " start";
        public static final String COLUMN_NAME_COL2 = " end";
        public static final String COLUMN_NAME_COL3 = " startLatitude";
        public static final String COLUMN_NAME_COL4 = " startLongitude";
        public static final String COLUMN_NAME_COL5 = " endLatitude";
        public static final String COLUMN_NAME_COL6 = " endLongitude";
        public static final String COLUMN_NAME_COL7 = " image";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL2 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL3 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL5 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL6 + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_COL7 + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

}
