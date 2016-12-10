package com.example.activitytracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import static com.example.activitytracker.R.string.gps_disabled_message;

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
        //Inform the user if the GPS is disabled when the activity is created.
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Hide warning message if GPS is enabled.
            TextView textView = (TextView) findViewById(R.id.gps_disabled_warning);
            textView.setVisibility(View.GONE);
        } else {
            Toast toast = Toast.makeText(this, getString(R.string.gps_disabled_message), Toast.LENGTH_LONG);
            toast.show();
        }

        //Register the ContentObserver to listen to changes in the database.
        getContentResolver().registerContentObserver(LocationProviderContract.LOCATION_URI, true,
                myObserver);

        //Register two local broadcast receivers to listen to local broadcasts that will be sent
        //when the GPS is disabled or enabled.
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsDisabledBroadcastReceiver,
                new IntentFilter("gps_disabled_intent"));
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsEnabledBroadcastReceiver,
                new IntentFilter("gps_enabled_intent"));


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
            updateDistanceToday();
            updateDailyAverage();
            updateVerticalDistanceToday();
            updateWeekGraph();
        }
    }

    //This class is a broadcast receiver that will be notified when the GPS is disabled.
    private BroadcastReceiver gpsDisabledBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast message that tells the user that the GPS is disabled and should be turned on.
            Toast toast = Toast.makeText(context, getString(gps_disabled_message),
                    Toast.LENGTH_LONG);
            toast.show();
            //Show warning message if GPS is enabled.
            TextView textView = (TextView) findViewById(R.id.gps_disabled_warning);
            textView.setVisibility(View.VISIBLE);
        }
    };

    //This class is a broadcast receiver that will be notified when the GPS is enabled.
    private BroadcastReceiver gpsEnabledBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast message that tells the user that the GPS is disabled and should be turned on.
            Toast toast = Toast.makeText(context, getString(R.string.gps_enabled_message),
                    Toast.LENGTH_LONG);
            toast.show();
            //Hide warning message if GPS is enabled.
            TextView textView = (TextView) findViewById(R.id.gps_disabled_warning);
            textView.setVisibility(View.GONE);
        }
    };

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
            updateWeekGraph();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("G53MDP", "onServiceDisconnected");
            locationServiceBinder = null;
        }
    };

    private void updateWeekGraph() {
        //Here a third party library is used. It is called android-graphview and is available under
        //an Apache 2 license. More info can be found at http://www.android-graphview.org/support/
        GraphView graphView = (GraphView) findViewById(R.id.week_graph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 2),
                new DataPoint(1, 7),
                new DataPoint(2, 2),
                new DataPoint(3, 4),
                new DataPoint(4, 2)
        });
        Viewport viewport = graphView.getViewport();
        //Set the boundaries of the Y-axis.
        viewport.setMinY(0);
        viewport.setMaxY(15);
        viewport.setYAxisBoundsManual(true);
        //Set the distance between the bars.
        series.setSpacing(50);
        //Set a title.
        graphView.setTitle(getString(R.string.graph_title));
        //Change the colour of the bar depending on the height.
        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                if (data.getY() < 3) {
                    //Red for short distance.
                    return Color.rgb(255, 69, 0);
                } else if (data.getY() < 6) {
                    //Yellow for medium distance.
                    return Color.rgb(255, 255, 51);
                } else {
                    //Green for long distance.
                    return Color.rgb(0, 128, 0);
                }
            }
        });
        graphView.addSeries(series);
    }

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
        Log.d("G53MDP", "MainActivity onDestroy");
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            //serviceConnection is set to null in order to avoid memory leaks.
            serviceConnection = null;
        }
    }
}
