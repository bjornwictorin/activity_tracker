package com.example.activitytracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

    LocationManager locationManager;
    MyLocationListener locationListener;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("G53MDP", "LocationService onCreate");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
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
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
