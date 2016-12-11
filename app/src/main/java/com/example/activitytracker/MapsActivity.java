package com.example.activitytracker;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/*
Much of the content in this file was auto generated by Android studio, by clicking
new->activity->gallery... and then choosing "Google Maps Activity".
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DistanceCalculator calculator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        calculator = new DistanceCalculator(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Retrieve all today's coordinates and timestamps.
        ArrayList<LatLng> coordinatesToday = calculator.getCoordinatesPerDay(0);
        ArrayList<String> timeatampsToday = calculator.getTimestampsPerDay(0);
        //Mark out every tenth point on the map. Only every tenth is marked to avoid cluttering.
        //Every marker gets its timstamp as its title.
        for (int i = 0; i < coordinatesToday.size(); i += 10) {
            mMap.addMarker(new MarkerOptions().position(coordinatesToday.get(i)).title(timeatampsToday.get(i)));
        }
        //Move the camera to the last element.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinatesToday.get(coordinatesToday.size()-1), 14));
        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */


    }
}
