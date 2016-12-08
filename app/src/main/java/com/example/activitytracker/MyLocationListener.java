package com.example.activitytracker;

import android.app.Service;
import android.content.ContentValues;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Björn on 2016-12-06.
 */

public class MyLocationListener implements LocationListener {

    //Reference to the service that instanced this class. This reference is needed in order to do
    // calls on the content provider.
    Service instancingService = null;

    public MyLocationListener(Service s){
        super();
        instancingService = s;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("G53MDP", "onLocationChanged " + location.getLatitude() + " " + location.getLongitude());
        //Store coordinate data in a ContentValues object.
        ContentValues values = new ContentValues();
        values.put(LocationProviderContract.LATITUDE, location.getLatitude());
        values.put(LocationProviderContract.LONGITUDE, location.getLongitude());
        values.put(LocationProviderContract.ALTITUDE, location.getAltitude());
        //values.put(LocationProviderContract.TIMESTAMP, 0);
        //Write to database.
        Uri newLocationUri = instancingService.getContentResolver().
                insert(LocationProviderContract.LOCATION_URI, values);
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
