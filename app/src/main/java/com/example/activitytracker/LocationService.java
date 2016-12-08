package com.example.activitytracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
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
        //Register the locationsListener, which will listen to the GPS updates.
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, locationListener);
        } catch (SecurityException e) {
            Log.d("G53MDP", e.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public class MyBinder extends Binder {
        //Place the methods that should be callable from activities here.
        void test() {
            Log.d("G53MDP", "test method in LocationService called");
        }
    }
}
