package com.example.activitytracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationService extends Service {

    private final IBinder binder = new MyBinder();
    LocationManager locationManager;
    MyLocationListener locationListener;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("G53MDP", "LocationService onCreate");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(this);
        //Register the locationListener, which will listen to the GPS updates.
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, locationListener);
        } catch (SecurityException e) {
            Log.d("G53MDP", e.toString());
        }
    }

    @Override
    public void onDestroy() {
        Log.d("G53MDP", "LocationService onDestroy");
        super.onDestroy();
        //Stop the location listener from receiving GPS coordinates.
        //Check if the app has the permission to read fine location.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
        //Set the reference to null to let the garbage collector destroy the LocationListener object.
        locationListener = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("G53MDP", "LocationService onBind");
        return binder;
    }

    public class MyBinder extends Binder {
        //Place the methods that should be callable from activities here.
    }
}
