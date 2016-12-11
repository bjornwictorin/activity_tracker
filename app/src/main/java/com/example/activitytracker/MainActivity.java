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

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.activitytracker.R.string.gps_disabled_message;

public class MainActivity extends AppCompatActivity {
    private LocationService.MyBinder locationServiceBinder = null;
    //The handler is needed to let myObserver make changes to the UI.
    private Handler h = new Handler();
    private MyObserver myObserver = new MyObserver(h);
    private DistanceCalculator calculator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start the location service.
        startLocationService();
        //Create a DistanceCalculator object that can be used to calculate distances.
        calculator = new DistanceCalculator(this);
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
        double distanceTodayInMetres = calculator.distanceToday();
        double distanceTodayInKilometres = distanceTodayInMetres / 1000;
        //Only show two decimal places.
        String distanceFormatted = String.format(Locale.ENGLISH, "%.2f", distanceTodayInKilometres);
        textView.setText(getString(R.string.distance_today) + " " + distanceFormatted + " km");
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
            updateWeekGraph();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("G53MDP", "onServiceDisconnected");
            locationServiceBinder = null;
        }
    };

    private void updateWeekGraph() {
        //Here a third party library is used for graph plotting. It is called android-graphview,
        // and is available under an Apache 2 license. More info can be found at
        // http://www.android-graphview.org/support/

        //Array containing the distances from the last 7 days.
        double[] lastWeekDistances = calculator.distancePerDaySevenDays();

        GraphView graphView = (GraphView) findViewById(R.id.week_graph);
        //A series containing the distances from the last 7 days.
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>();
        for (int i = 0; i < 7; i++) {
            series.appendData(new DataPoint(i, lastWeekDistances[i]), true, 7);
        }

        //Array to hold the dates of the last 7 days.
        final Date dateArray[] = new Date[7];
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -7);
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DATE, 1);
            dateArray[i] = calendar.getTime();
        }

        //Set the X-label values to be the first letter of each weekday. Based on the example code at
        //http://www.android-graphview.org/labels-and-label-formatter/
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    int pos = (int) value;
                    //Returns the first letter of the weekday that corresponds to the date in
                    // dateArray at the index pos.
                    return (new SimpleDateFormat("EE", Locale.ENGLISH).format(dateArray[pos]).substring(0, 1));
                } else {
                    return super.formatLabel(value, false);
                }
            }
        });
        Viewport viewport = graphView.getViewport();
        //Set the boundaries of the X-axis.
        viewport.setMinX(0);
        viewport.setMaxX(6);
        viewport.setXAxisBoundsManual(true);
        graphView.getGridLabelRenderer().setNumHorizontalLabels(7);
        //Set the boundaries of the Y-axis.
        viewport.setMinY(0);
        double maxDistance = maxDistance(lastWeekDistances);
        viewport.setMaxY(maxDistance + 2);
        viewport.setYAxisBoundsManual(true);
        //Set the distance between the bars.
        series.setSpacing(50);
        //Set a title.
        graphView.setTitle(getString(R.string.graph_title));
        //Change the colour of the bars depending on the height.
        //Based on the example code at http://www.android-graphview.org/bar-chart/
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
                    return Color.rgb(50, 205, 50);
                }
            }
        });

        //Remove the old data to make room for the updated data.
        graphView.removeAllSeries();
        //Add the updated data to the graph.
        graphView.addSeries(series);
    }

    private double maxDistance(double[] distances) {
        double max = Double.MIN_VALUE;
        for (double distance : distances) {
            if (distance > max) {
                max = distance;
            }
        }
        return max;
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

    public void onMoreInfoClick(View v) {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
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
