package com.example.mustafizur.geoattendance;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Tracker {

     private static  final String  TAG= Tracker.class.getSimpleName();
     double Tracking_Latitude;
     double Tracking_Longitude;
     String Tracking_Name;
     String Tracking_Time;



        Tracker()
        {

        }

        Tracker(double latiude,double longitude,String name,String time)
        {
            Tracking_Time= time;
            Tracking_Name=name;
            Tracking_Longitude= longitude;
            Tracking_Latitude=latiude;
        }



    public void CreateMarker(GoogleMap gmap)
    {
        String s = "Details:" +Tracking_Name+ "Time:"+Tracking_Time;
        LatLng L = new LatLng(Tracking_Latitude,Tracking_Longitude);
        gmap.addMarker(new MarkerOptions().position(new LatLng(Tracking_Latitude,Tracking_Longitude)).title(s));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(L));
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(L,17.0f));


        Log.d(TAG,String.format("Location: %f,%f",Tracking_Latitude,Tracking_Longitude));
    }



}
