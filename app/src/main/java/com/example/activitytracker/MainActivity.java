package com.example.activitytracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private LocationService.MyBinder locationServiceBinder = null;
    //The handler is needed to let myObserver make changes to the UI.
    private Handler h = new Handler();
    private MyObserver myObserver = new MyObserver(h);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start the location service.
        startLocationService();

        //Register the ContentObserver to listen to changes in the database.
        getContentResolver().registerContentObserver(LocationProviderContract.LOCATION_URI, true,
                myObserver);

        //Set up the handling of clicks on the switch that turns logging on and off.
        final Switch logSwitch = (Switch) findViewById(R.id.log_switch);
        logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    logSwitch.setText(R.string.turn_logging_off);
                    //Turn the logging on by starting the location service.
                    startLocationService();
                } else {
                    logSwitch.setText(R.string.turn_logging_on);
                    //Turn the logging off by stopping the location service.
                    stopLocationService();
                }
            }
        });
    }

    public void updateDistanceToday() {
        TextView textView = (TextView) findViewById(R.id.distance_today);
        double distanceTodayInMetres = locationServiceBinder.distanceToday();
        double distanceTodayInKilometres = distanceTodayInMetres / 1000;
        //Only show two decimal places.
        String distanceFormatted = String.format("%.2f", distanceTodayInKilometres);
        textView.setText(getString(R.string.distance_today) + " " + distanceFormatted + " km");
    }

    public void updateDailyAverage() {
        TextView textView = (TextView) findViewById(R.id.daily_average_last_week);
        double averageInMetres = locationServiceBinder.dailyAverageLastWeek();
        double averageInKilometres = averageInMetres / 1000;
        //Only show two decimal places.
        String distanceFormatted = String.format("%.2f", averageInKilometres);
        textView.setText(getString(R.string.daily_average_last_week) +
                " " + distanceFormatted + " km");
    }

    public void updateVerticalDistanceToday() {
        TextView textView = (TextView) findViewById(R.id.vertical_distance_today);
        double verticalDistanceInMetres = locationServiceBinder.verticalDistanceToday();
        //Only show two decimal places.
        String distanceFormatted = String.format("%.2f", verticalDistanceInMetres);
        textView.setText(getString(R.string.vertical_distance_today) +
                " " + distanceFormatted + " m");
    }

    //The purpose of this class is to update the distance values showed in the activity only when
    // data has changed.
    private class MyObserver extends ContentObserver {
        public MyObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("G53MDP", "MyObserver onChange");
            updateDistanceToday();
            updateDailyAverage();
            updateVerticalDistanceToday();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("G53MDP", "onServiceConnected");
            locationServiceBinder = (LocationService.MyBinder) service;
            //Update the displayed values of the distances as soon as the activity is connected to
            // the content provider.
            updateDistanceToday();
            updateDailyAverage();
            updateVerticalDistanceToday();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("G53MDP", "onServiceDisconnected");
            locationServiceBinder = null;
        }
    };

    private void startLocationService() {
        Log.d("G53MDP", "startLocationService");
        //Start the location service.
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        //Bind to the location service.
        this.bindService(new Intent(this, LocationService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void stopLocationService() {
        //Unbind from the service.
        if (serviceConnection != null) {
            Log.d("G53MDP", "unbinding from service");
            unbindService(serviceConnection);
            //locationServiceBinder = null;
        }
        //Stop the service.
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        stopService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            //serviceConnection is set to null in order to avoid memory leaks.
            serviceConnection = null;
        }
    }
}
