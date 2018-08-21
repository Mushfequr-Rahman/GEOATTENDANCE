package com.example.mustafizur.geoattendance;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Admin_Map extends FragmentActivity implements OnMapReadyCallback,
GoogleMap.OnMarkerClickListener
{

    private GoogleMap mMap;
    private  static  final String TAG = Admin_Map.class.getSimpleName();
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.
            getReferenceFromUrl("https://geo-attendance-f0731.firebaseio.com/TIME LOGS/2018/05/12/Zahid/Tracking info");


    private Token mtoken= new Token();
    TrackingDatabase mTrackingDatabase = new TrackingDatabase(this);



    private List<Employee> EmployeeList;
    private List<Location> MarkerList;
    //private Tracker mTracker = new Tracker();
    private Button Refresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Admin_Map);
        mapFragment.getMapAsync(this);
        Refresh=findViewById(R.id.Admin_Button);
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateLocationLists();

            }
        });



    }

    private void UpdateLocationLists() {
        Toast.makeText(this,"Updating Lists",Toast.LENGTH_SHORT).show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        ArrayList<HashMap<String,String>> Tracking_List = mTrackingDatabase.GetAllData();

        for(HashMap T: Tracking_List)
        {
            if(T!=null) {
                mtoken.AddPoints(new LatLng( Double.parseDouble((String)T.get("Latitude")), Double.parseDouble( (String) T.get("Longitude"))));
                Log.d(TAG,String.format("Tracking Data:" +Double.parseDouble((String)T.get("Latitude"))+"   " +Double.parseDouble( (String) T.get("Longitude"))));
                Tracker mtracker = new Tracker(Double.parseDouble((String)T.get("Latitude")), Double.parseDouble( (String) T.get("Longitude")), (String) T.get("Name"), (String) T.get("Time"));
                mtoken.DrawLine(mMap, mtracker);
            }
            else
            {
                Log.d(TAG, "Database is empty at this point" );
            }
        }








    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    class Token
    {
        private ArrayList<LatLng> points= new ArrayList<LatLng>();
        private Polyline line;

        public Token()
        {

        }

        public void AddPoints(LatLng latlang)
        {

                points.add(latlang);


        }

        public void DrawLine(GoogleMap gmap,Tracker t)

        {
            mMap.clear();
            LatLng markerpoint ;
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            for(int i=0;i<points.size();i++)
            {
                LatLng point =points.get(i);
                options.add(point);
                markerpoint=point;
            }
            line = gmap.addPolyline(options);
            t.CreateMarker(gmap);

        }


    }
}
