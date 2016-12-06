package com.example.activitytracker;

import android.net.Uri;

/**
 * Created by Björn on 2016-12-06.
 */

public class LocationProviderContract {
    public static final String AUTHORITY = "com.example.activitytracker.LocationContentProvider";
    public static final Uri LOCATION_URI =Uri.parse("content://" + AUTHORITY + "/locations");
    public static final String _ID = "_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String TIMESTAMP = "timestamp";
}
