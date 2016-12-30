package com.example.activitytracker;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Björn on 2016-12-06.
 */

class MyLocationListener implements LocationListener {

    //Reference to the service that instanced this class. This reference is needed in order to do
    // calls on the content provider.
    private Service instancingService = null;

    MyLocationListener(Service s){
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
        //Send local broadcast to notify the user that the GPS is enabled. The broadcast receiver
        //is located in the main activity.
        LocalBroadcastManager bcManager =
                LocalBroadcastManager.getInstance(instancingService.getApplicationContext());
        Intent intent = new Intent("gps_enabled_intent");
        bcManager.sendBroadcast(intent);
    }

    public void onProviderDisabled(String provider) {
        Log.d("G53MDP", "onProviderDisabled " + provider);
        //Send local broadcast to notify the user that the GPS is disabled. The broadcast receiver
        //is located in the main activity.
        LocalBroadcastManager bcManager =
                LocalBroadcastManager.getInstance(instancingService.getApplicationContext());
        Intent intent = new Intent("gps_disabled_intent");
        bcManager.sendBroadcast(intent);
    }
}
