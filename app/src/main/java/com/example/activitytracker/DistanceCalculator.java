package com.example.activitytracker;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Björn on 2016-12-11.
 */

//This class carries out distance calculations, and gets its information by querying the
//content provider.
class DistanceCalculator {
    //This activity is used to access the content provider.
    private Activity instantiatingActivity = null;

    DistanceCalculator(Activity activity) {
        instantiatingActivity = activity;
    }

    double distanceToday() {
        return distancePerDay(0);
    }

    //Calculates the average distance per day for the last 7 days. If a day has a distance
    // of 0, it will not be taken into account in the calculation of the average.
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
        if (nonZeroDays == 0) {
            return 0;
        }
        return totalWeekDistance / nonZeroDays;
    }

    double verticalDistanceToday() {
        return verticalDistancePerDay(0);
    }

    double[] distancePerDaySevenDays() {
        //Fill an array with the distances for the last 7 days. Distances in km.
        double[] lastWeekDistances = new double[7];
        for (int i = 0; i < 7; i++) {
            //Have to be in reverse order in order to plot the latest day last in the graph.
            lastWeekDistances[i] = distancePerDay(6 - i) / 1000;
        }
        return lastWeekDistances;
    }

    //Returns a list containing all latitudes and longitudes logged daysAgo days ago.
    ArrayList<LatLng> getCoordinatesPerDay(int daysAgo) {
        //The list to return.
        ArrayList<LatLng> coordinatesToday = new ArrayList<>();
        //Query the content provider.
        String[] projection = {LocationProviderContract.LATITUDE, LocationProviderContract.LONGITUDE};
        String selection = " date(" + LocationProviderContract.TIMESTAMP + ") = date(CURRENT_TIMESTAMP, \"-" +
            daysAgo + " day\")";
        Cursor cursor = instantiatingActivity.getContentResolver().
                query(LocationProviderContract.LOCATION_URI, projection, selection, null, null);
        //Add the latitudes and longitudes to the list.
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            coordinatesToday.add(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
            for (int i = 1; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                coordinatesToday.add(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
            }
            cursor.close();
        }
        return coordinatesToday;
    }

    //Returns a list containing all timestamps logged daysAgo days ago.
    ArrayList<String> getTimestampsPerDay(int daysAgo) {
        //The list to return.
        ArrayList<String> timestampsToday = new ArrayList<>();
        //Query the content provider.
        String[] projection = {LocationProviderContract.TIMESTAMP};
        String selection = " date(" + LocationProviderContract.TIMESTAMP + ") = date(CURRENT_TIMESTAMP, \"-" +
                daysAgo + " day\")";
        Cursor cursor = instantiatingActivity.getContentResolver().
                query(LocationProviderContract.LOCATION_URI, projection, selection, null, null);
        //Add the timestamps to the list.
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            timestampsToday.add(cursor.getString(0));
            for (int i = 1; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                timestampsToday.add(cursor.getString(0));
            }
            cursor.close();
        }
        return timestampsToday;
    }

    private double distancePerDay(int daysAgo) {
        //Calculate the distance that the device has moved during the specified day.
        //Which day the data should be retrieved for is decided by the daysAgo parameter.
        //Fetch all locations that were recorded today from the database.
        String[] projection = {LocationProviderContract._ID, LocationProviderContract.LONGITUDE,
                LocationProviderContract.LATITUDE};
        String selection = " date(" + LocationProviderContract.TIMESTAMP +
                ") = date(CURRENT_TIMESTAMP, \"-" + daysAgo + " day\")";
        Cursor cursor = instantiatingActivity.getContentResolver().query(LocationProviderContract.LOCATION_URI,
                projection, selection, null, null);
        //Calculate the distance for today.
        double distanceToday = 0;
        Location startLocation = new Location("");
        Location endLocation = new Location("");
        //At least two points are needed to calculate a distance, hence the comparison > 1.
        if (cursor != null && cursor.getCount() > 1) {
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
            cursor.close();
        }
        return distanceToday;
    }

    private double verticalDistancePerDay(int daysAgo) {
        //Calculates the vertical distance that the device has moved during the specified day.
        //Which day the data should be retrieved for is decided by the daysAgo parameter.
        //The vertical distance is based in the altitude measurements from the GPS.
        String[] projection = {LocationProviderContract.ALTITUDE};
        String selection = " date(" + LocationProviderContract.TIMESTAMP +
                ") = date(CURRENT_TIMESTAMP, \"-" + daysAgo + " day\")";
        Cursor cursor = instantiatingActivity.getContentResolver().
                query(LocationProviderContract.LOCATION_URI, projection, selection, null, null);
        //Calculate the vertical distance for today.
        double verticalDistance = 0;
        //At least two points are needed to calculate a distance, hence the comparison > 1.
        if (cursor != null && cursor.getCount() > 1) {
            cursor.moveToFirst();
            double startAltitude = cursor.getDouble(0);
            double endAltitude;
            for (int i = 1; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                endAltitude = cursor.getDouble(0);
                //Add the absolute value of the distance to the total altitude. The absolute is used
                //since otherwise it would subtract altitude when you walk downhill.
                double tempDistance = Math.abs(endAltitude - startAltitude);
                verticalDistance += tempDistance;
                startAltitude = endAltitude;
            }
            cursor.close();
        }
        return verticalDistance;
    }
}
