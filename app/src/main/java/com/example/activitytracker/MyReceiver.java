package com.example.activitytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Start the LocationService when the phone has booted.
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.startService(serviceIntent);
        Log.d("G53MDP", "MyReceiver tried to start the service.");
    }
}