package com.example.activitytracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    private LocationService.MyBinder locationServiceBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start the location service.
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        //Bind to the location service.
        this.bindService(new Intent(this, LocationService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);

        //Set up the handling of clicks on the switch that turns logging on and off.
        final Switch logSwitch = (Switch) findViewById(R.id.log_switch);
        logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    logSwitch.setText(R.string.turn_logging_off);
                    //Turn the logging on.
                    locationServiceBinder.test();
                } else {
                    logSwitch.setText(R.string.turn_logging_on);
                    //Turn the logging off.

                }
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("G53MDP", "onServiceConnected");
            locationServiceBinder = (LocationService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("G53MDP", "onServiceDisconnected");
            locationServiceBinder = null;
        }
    };
}
