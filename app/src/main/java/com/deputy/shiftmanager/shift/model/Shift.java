package com.deputy.shiftmanager.shift.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Shift class to model the shifts.
 */
public class Shift {



    public static ArrayList<ShiftItem> SHIFT_LIST = new ArrayList<>();

// --Commented out by Inspection START (10/03/2017 8:04 AM):
//    public ArrayList<ShiftItem> getResults() {
//        return SHIFT_LIST;
//    }
// --Commented out by Inspection STOP (10/03/2017 8:04 AM)

    public static void addShift(ShiftItem item) {

        SHIFT_LIST.add(Integer.valueOf(item.id) -1, item);
    }

    public static ShiftItem getShift(Integer index) {

        return SHIFT_LIST.get(index);

    }


    /**
     * A ShiftItem representing one shift.
     */
    public static class ShiftItem implements Comparable<ShiftItem> {

        public final String id;
        public final String start;
        public final String end;
        public final String startLatitude;
        public final String startLongitude;
        public final String endLatitude;
        public final String endLongitude;
        public final String image;

        @Override
        public int compareTo(@NonNull ShiftItem shift) {

            /* For Ascending order*/
            //return this.this.id - shift.id;

            /* For Descending order do like this */
            return Integer.valueOf(shift.id) - Integer.valueOf(this.id);
        }

        public ShiftItem(String id, String start, String end, String startLatitude,
                         String startLongitude, String endLatitude, String endLongitude,
                         String image) {
            this.id = id;
            this.start = start;
            this.end = end;
            this.startLatitude = startLatitude;
            this.startLongitude = startLongitude;
            this.endLatitude = endLatitude;
            this.endLongitude = endLongitude;
            this.image = image;

        }



    }//End Class ShiftItem


}
