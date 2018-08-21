package com.example.mustafizur.geoattendance;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

class GeofenceTransitionService extends IntentService {

        private static final String TAG = GeofenceTransitionService.class.getSimpleName();

        public static final int GEOFENCE_NOTIFICATION_ID = 0;

        private final IBinder GeoUpdateBinder = new myLocalBinder();
        private  int geoFenceTransition;

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference getRef = database.getReference("TIME LOGS");
        DatabaseReference ref;

        public GeofenceTransitionService() {
            super(TAG);
        }
        private  Employee EmployeeHold;
        private Location mCurrentLocation;
        private boolean isBound = false;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
         Bundle extras = intent.getExtras();
         if(extras != null) {
             EmployeeHold = (Employee) extras.get("Key3");
             mCurrentLocation = (Location) extras.get("Key4");
             Log.d(TAG, "Employee info released");
             ref = getRef.child(EmployeeHold.getDate());
         }


          isBound= true;
        return GeoUpdateBinder;
    }


    @Override
        protected void onHandleIntent(Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            Log.e(TAG,"Intent recieved");

            //mCurrentLocation= Client.getLastLocation();
        if(isBound== true && mCurrentLocation != null) {
            Log.d(TAG, String.format("Current location, Laitude: %d, Longitude: %d", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
            //Updating Service for Updating Location




            // Handling errors
            if ( geofencingEvent.hasError() ) {
                String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
                Log.e( TAG, errorMsg );
                return;
            }

             geoFenceTransition = geofencingEvent.getGeofenceTransition();
            // Check if the transition type is of interest
            // geoFenceTransition=1;


            if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geoFenceTransition ==Geofence.GEOFENCE_TRANSITION_DWELL  ) {
                // Get the geofence that were triggered
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                Log.e(TAG,"Trigger Succesfull");



                String geofenceTransitionDetails = getGeofenceTransitionDetails(geoFenceTransition, triggeringGeofences );
                Log.e(TAG,geofenceTransitionDetails);

                // Send notification details as a String
                sendNotification( geofenceTransitionDetails );
            }
            else
            {
                Log.e(TAG,"Transition unsuccesful");
                Log.e(TAG, String.valueOf(geoFenceTransition));


            }
        }


        private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
            // get the ID of each geofence triggered
            ArrayList<String> triggeringGeofencesList = new ArrayList<>();
            for ( Geofence geofence : triggeringGeofences ) {
                triggeringGeofencesList.add( geofence.getRequestId() );
                Log.e(TAG,"Created Geofence trigger list");
            }

            String status = null;
            Log.d(TAG,"Updating GeoFence Updated Conditions");

            if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ){
                status = "Entering ";
                Toast.makeText(this,"You have entered the workplace",Toast.LENGTH_LONG).show();
                EmployeeHold.UpdateTime(ref);
                //EmployeeHolder.UpdateDate();

            }
            else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
                Toast.makeText(this, "You have left the workplace", Toast.LENGTH_SHORT).show();
                EmployeeHold.UpdateExitTime(ref);
                status = "Exiting ";

            }
            else if(geoFenceTransition==Geofence.GEOFENCE_TRANSITION_DWELL) {
                Toast.makeText(this, "You were in the workplace", Toast.LENGTH_SHORT).show();
                status = "You were already in the workplace";
            }
            return status + TextUtils.join( ", ", triggeringGeofencesList);
        }

        private void sendNotification( String msg ) {
            Log.i(TAG, "sendNotification: " + msg );

            // Intent to start the main Activity
            Intent notificationIntent = GeoAttendance.makeNotificationIntent(getApplicationContext(), msg);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MapsActivity.class);
            //stackBuilder.addParentStack(GeoAttendance.class);
            stackBuilder.addNextIntent(notificationIntent);
            Intent intent = new Intent(this,MapsActivity.class);
            //PendingIntent notificationPendingIntent =PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            //Notification notification = createNotification(msg,notificationPendingIntent);


            // Creating and sending Notification
            NotificationManager notificatioMng =
                    (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );

           // notificatioMng.notify(
           //         GEOFENCE_NOTIFICATION_ID,
           //         createNotification(msg, notificationPendingIntent));



            Log.i(TAG,"Notification Sent");

        }


        // Create notification
        private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder
                    .setSmallIcon(R.drawable.ic_action_location)
                    .setColor(Color.RED)
                    .setContentTitle(msg)
                    .setContentIntent(notificationPendingIntent)
                    .setAutoCancel(true);
            return notificationBuilder.build();
        }


        private static String getErrorString(int errorCode) {
            switch (errorCode) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GeoFence not available";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "Too many GeoFences";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "Too many pending intents";
                default:
                    return "Unknown error.";
            }
        }

        public class myLocalBinder extends Binder
        {
            GeofenceTransitionService getService()
            {
                return GeofenceTransitionService.this;

            }



        }

        public int getGeofenceID()
        {
            return geoFenceTransition;
        }



    }

