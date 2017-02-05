package com.deputy.shiftmanager;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A fragment representing a single Shift detail screen.
 * This fragment is either contained in a {@link ShiftListActivity}
 * in two-pane mode (on tablets) or a {@link ShiftDetailActivity}
 * on handsets.
 */
public class ShiftDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */

    private Shift.ShiftItem mItem;

    private static final String lineBreak = System.getProperty("line.separator");

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShiftDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the content specified by the fragment

            mItem = Shift.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();

            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.id);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shift_detail, container, false);

        // Show the content as text in a TextView.
        if (mItem != null) {
            TextView shiftDetail = ((TextView) rootView.findViewById(R.id.shift_detail));//.setText(mItem.id + lineBreak + mItem.start + lineBreak + mItem.end);

            shiftDetail.setText("ID: " + mItem.id  + lineBreak);
            shiftDetail.append("Start Date: " + mItem.start + lineBreak);
            shiftDetail.append("End Date: " + mItem.end + lineBreak);
            shiftDetail.append("Start Latitude: " +  mItem.startLatitude + lineBreak);
            shiftDetail.append("Start Longitude: " +  mItem.startLongitude + lineBreak);
            shiftDetail.append("End Latitude: " +  mItem.endLatitude + lineBreak);
            shiftDetail.append("End Longitude: " +  mItem.endLongitude + lineBreak);
            shiftDetail.append("Image: " +  mItem.image + lineBreak);

        }

        return rootView;
    }
}
