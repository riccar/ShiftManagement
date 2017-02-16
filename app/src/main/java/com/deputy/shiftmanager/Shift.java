package com.deputy.shiftmanager;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class Shift {

    /**
     * An array of Shift items.
     */
    //private static final List<ShiftItem> ITEMS = new ArrayList<>();

    /**
     * A map of shift items, by ID.
     */
    public static final Map<String, ShiftItem> ITEM_MAP = new HashMap<>();

    public static void addItem(ShiftItem item) {
        //ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A ShiftItem representing a one shift.
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
