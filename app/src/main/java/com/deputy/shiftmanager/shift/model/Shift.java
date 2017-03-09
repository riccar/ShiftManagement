package com.deputy.shiftmanager.shift.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Shift class to model the shifts.
 */
public class Shift {

    /**
     * A map of shift items, by ID.
     */
    public static final Map<String, ShiftItem> ITEM_MAP = new HashMap<>();

    public static void addItem(ShiftItem item) {
        ITEM_MAP.put(item.id, item);
    }

    public static ArrayList<ShiftItem> SHIFT_LIST = new ArrayList<>();

// --Commented out by Inspection START (10/03/2017 8:04 AM):
//    public ArrayList<ShiftItem> getResults() {
//        return SHIFT_LIST;
//    }
// --Commented out by Inspection STOP (10/03/2017 8:04 AM)

    public static void addShift(ShiftItem item) {

        SHIFT_LIST.add(Integer.valueOf(item.id) -1, item);
    }

    /**
     * A ShiftItem representing one shift.
     */
    public static class ShiftItem {
        public final String id;
        public final String start;
        public final String end;
        public final String startLatitude;
        public final String startLongitude;
        public final String endLatitude;
        public final String endLongitude;
        public final String image;



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
