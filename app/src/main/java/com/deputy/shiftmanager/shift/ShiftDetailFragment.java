/**
 * A fragment representing a single Shift detail screen.
 * This fragment is either contained in a {@link ShiftListActivity}
 * in two-pane mode (on tablets) or a {@link ShiftDetailActivity}
 * on handsets.
 */

package com.deputy.shiftmanager.shift;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.Util.date;
import com.deputy.shiftmanager.shift.model.Shift;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShiftDetailFragment extends Fragment implements OnMapReadyCallback {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */
    private Shift.ShiftItem shiftItem;

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

            //ITEM_MAP is no longer used after implementing networking with retrofit
            //mItem = Shift.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            shiftItem = Shift.SHIFT_LIST.get(Integer.valueOf(getArguments().getString(ARG_ITEM_ID)) - 1);

            Activity activity = this.getActivity();

            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)
                    activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(String.valueOf(shiftItem.id));
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker for the start and end shift coordinates
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add a marker in Sydney and move the camera
        LatLng startPoint = new LatLng(Double.parseDouble(shiftItem.startLatitude),
                Double.parseDouble(shiftItem.startLongitude));
        LatLng endPoint = new LatLng(Double.parseDouble(shiftItem.endLatitude),
                Double.parseDouble(shiftItem.endLongitude));
        googleMap.addMarker(new MarkerOptions().position(startPoint).title("Start"));
        googleMap.addMarker(new MarkerOptions().position(endPoint).title("End"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 13));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shift_detail, container, false);

        // Show the content as text in a TextView.
        if (shiftItem != null) {
            TextView shiftDetail = ((TextView) rootView.findViewById(R.id.fragment_shift_detail));

            shiftDetail.setText("ID: " + shiftItem.id  + lineBreak);
            shiftDetail.append("Start Date: " + date.formatStringDate(shiftItem.start, getContext()
            ) + lineBreak);
            if (shiftItem.end.equals("")) shiftDetail.append("End Date: Shift in progress" + lineBreak);
            else shiftDetail.append("End Date: " + date.formatStringDate(shiftItem.end, getContext()
            ) + lineBreak);
            //Uncomment to debug coordinates
            /*shiftDetail.append("Start Latitude: " +  shiftItem.startLatitude + lineBreak);
            shiftDetail.append("Start Longitude: " +  shiftItem.startLongitude + lineBreak);
            shiftDetail.append("End Latitude: " +  shiftItem.endLatitude + lineBreak);
            shiftDetail.append("End Longitude: " +  shiftItem.endLongitude + lineBreak);*/


        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}
