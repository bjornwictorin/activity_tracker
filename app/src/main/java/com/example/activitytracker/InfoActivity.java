package com.example.activitytracker;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class InfoActivity extends AppCompatActivity {
    //The handler is needed to let myObserver make changes to the UI.
    private Handler h = new Handler();
    private MyObserver myObserver = new MyObserver(h);
    private DistanceCalculator calculator = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        calculator = new DistanceCalculator(this);
        updateDailyAverage();
        updateVerticalDistanceToday();

        //Register the ContentObserver to listen to changes in the database.
        getContentResolver().registerContentObserver(LocationProviderContract.LOCATION_URI, true,
                myObserver);
    }

    public void updateDailyAverage() {
        TextView textView = (TextView) findViewById(R.id.daily_average_last_week);
        double averageInMetres = calculator.dailyAverageLastWeek();
        double averageInKilometres = averageInMetres / 1000;
        //Only show two decimal places.
        String distanceFormatted = String.format(Locale.ENGLISH, "%.2f", averageInKilometres);
        textView.setText(getString(R.string.daily_average_last_week) +
                " " + distanceFormatted + " km");
    }

    public void updateVerticalDistanceToday() {
        TextView textView = (TextView) findViewById(R.id.vertical_distance_today);
        double verticalDistanceInMetres = calculator.verticalDistanceToday();
        //Only show two decimal places.
        String distanceFormatted = String.format(Locale.ENGLISH, "%.2f", verticalDistanceInMetres);
        textView.setText(getString(R.string.vertical_distance_today) +
                " " + distanceFormatted + " m");
    }

    //The purpose of this class is to update the distance values showed in the activity only when
    // data has changed.
    private class MyObserver extends ContentObserver {
        MyObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("G53MDP", "MyObserver onChange");
            updateDailyAverage();
            updateVerticalDistanceToday();
        }
    }
}
