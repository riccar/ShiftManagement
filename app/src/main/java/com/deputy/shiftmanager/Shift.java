package com.deputy.shiftmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Shift {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<ShiftItem> ITEMS = new ArrayList<ShiftItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, ShiftItem> ITEM_MAP = new HashMap<String, ShiftItem>();

    //private static final int COUNT = 25;

    /*static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createShiftItem(i));
        }
    }*/

    public static void addItem(ShiftItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static ShiftItem createShiftItem(String id, String start, String end, String startLatitude,
                                             String startLongitude, String endLatitude, String endLongitude,
                                             String image) {
        //return new ShiftItem(String.valueOf(position), "Item " + position, makeDetails(position));
        return new ShiftItem(id, start, end, startLatitude, startLongitude, endLatitude, endLongitude, image);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
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
