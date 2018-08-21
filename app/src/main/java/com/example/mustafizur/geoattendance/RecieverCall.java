package com.example.mustafizur.geoattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecieverCall extends BroadcastReceiver {
   private static final String TAG = RecieverCall.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"App stopped restarting service");
        Intent service_intent = new Intent(context,MyLocationService.class);
        context.startService(service_intent);
    }
}
