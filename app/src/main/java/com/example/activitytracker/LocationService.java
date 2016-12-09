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
            return distancePerDay(0);
        }
        //Calculates the average distance per day for the last 7 days. If a day has a distance
        //of 0, it will not be taken into account in the calculation of the average.
        double dailyAverageLastWeek() {
            double totalWeekDistance = 0;
            int nonZeroDays = 0;
            double dayDistance;
            for (int i = 0; i < 7; i++) {
                dayDistance = distancePerDay(i);
                totalWeekDistance += dayDistance;
                if (dayDistance > 0) {
                    nonZeroDays++;
                }
            }
            return totalWeekDistance / nonZeroDays;
        }
        double verticalDistanceToday() {
            return verticalDistancePerDay(0);
        }
    }

    private double distancePerDay(int daysAgo) {
        //Calculate the distance that the device has moved during the specified day.
        //Which day the data should be retrieved for is decided by the daysAgo parameter.
        //Fetch all locations that were recorded today from the database.
        String[] projection = {LocationProviderContract._ID, LocationProviderContract.LONGITUDE,
                LocationProviderContract.LATITUDE};
        String selection = " date(" + LocationProviderContract.TIMESTAMP +
                ") = date(CURRENT_TIMESTAMP, \"-" + daysAgo + " day\")";
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
            startLocation.setLongitude(cursor.getDouble(1));
            startLocation.setLatitude(cursor.getDouble(2));
            for (int i = 1; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                endLocation.setLongitude(cursor.getDouble(1));
                endLocation.setLatitude(cursor.getDouble(2));
                double distance = startLocation.distanceTo(endLocation);
                distanceToday += distance;
                startLocation = new Location(endLocation);
            }
        }
        cursor.close();
        return distanceToday;
    }

    private double verticalDistancePerDay(int daysAgo) {
        //Calculates the vertical distance that the device has moved during the specified day.
        //Which day the data should be retrieved for is decided by the daysAgo parameter.
        //The vertical distance is based in the altitude measurements from the GPS.
        String[] projection = {LocationProviderContract.ALTITUDE};
        String selection = " date(" + LocationProviderContract.TIMESTAMP +
                ") = date(CURRENT_TIMESTAMP, \"-" + daysAgo + " day\")";
        Cursor cursor = getContentResolver().query(LocationProviderContract.LOCATION_URI, projection,
                selection, null, null);
        //Calculate the vertical distance for today.
        double verticalDistance = 0;
        //At least two points are needed to calculate a distance, hence the comparison > 1.
        if (cursor.getCount() > 1) {
            cursor.moveToFirst();
            double startAltitude = cursor.getDouble(0);
            double endAltitude;
            for (int i = 1; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                endAltitude = cursor.getDouble(0);
                double tempDistance = endAltitude - startAltitude;
                verticalDistance += tempDistance;
                startAltitude = endAltitude;
            }
        }
        cursor.close();
        return verticalDistance;
    }
}
