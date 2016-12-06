package com.example.activitytracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Björn on 2016-12-06.
 */

public class MyLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
        Log.d("G53MDP", "onLocationChanged " + location.getLatitude() + " " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("G53MDP", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("G53MDP", "onProviderEnabled " + provider);
    }

    public void onProviderDisabled(String provider) {
        Log.d("G53MDP", "onProviderDisabled " + provider);
    }
}
