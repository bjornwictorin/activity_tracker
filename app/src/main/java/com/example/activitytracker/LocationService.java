package com.example.activitytracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
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
        //Register the locationsListener, which will listen to the GPS updates.
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
        Log.d("G53MDP", "onBind");
        return binder;
    }

    public class MyBinder extends Binder {
        //Place the methods that should be callable from activities here.
        void test() {
            Log.d("G53MDP", "test method in LocationService called");
        }
        double distanceToday() {
            return distanceTodayFunction();
        }
    }

    private double distanceTodayFunction() {
        //Calculate the distance that the device has moved during the specified day.
        //Fetch all locations that were recorded today from the database.
        String[] projection = {LocationProviderContract._ID, LocationProviderContract.LONGITUDE,
                LocationProviderContract.LATITUDE};
        String selection = " date(" + LocationProviderContract.TIMESTAMP + ") = date(CURRENT_TIMESTAMP)";
        Cursor cursor = getContentResolver().query(LocationProviderContract.LOCATION_URI,
                projection, selection, null, null);
        //Print all today's longitudes to the screen.
        Log.d("G53MDP", "Cursor count: " + cursor.getCount());
        //Calculate the distance for today.
        double distanceToday = 0;
        Location startLocation = new Location("");
        Location endLocation = new Location("");
        //At least two points are needed to calculate a distance, hence the comparison > 1.
        if (cursor.getCount() > 1) {
            cursor.moveToFirst();
            startLocation.setLongitude(cursor.getFloat(1));
            startLocation.setLatitude(cursor.getFloat(2));
            for (int i = 1; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                endLocation.setLongitude(cursor.getFloat(1));
                endLocation.setLatitude(cursor.getFloat(2));
                double distance = startLocation.distanceTo(endLocation);
                Log.d("G53MDP", "distance: " + distance);
                distanceToday += distance;
                startLocation = new Location(endLocation);
            }
        }
        cursor.close();
        return distanceToday;
    }
}
