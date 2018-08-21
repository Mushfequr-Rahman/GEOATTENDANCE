package com.example.mustafizur.geoattendance;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mustafizur.geoattendance.GeofenceTransitionService.myLocalBinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
//import com.loopj.android.http.*;
//import android.Parcelable.AbstractSafe

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,

        ActivityCompat.OnRequestPermissionsResultCallback, ResultCallback<Status> {


    private GoogleMap mMap;
    //private TimeDatabase timeDatabase = new TimeDatabase(this);

    private static final int MY_PERMISSON_REQUEST_CODE = 1996;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;
    private final int GEOFENCE_REQUEST_CODE = 100;
    public static final String TAG = GeoAttendance.class.getSimpleName();
    int PROXIMITY_RADIUS=1000;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mPermissonDenied = false;
    private Geofence mgeofence;
    private GeofencingClient mGeofencinglient;
    private PendingIntent geoPendingIntent;
    private GeofencingRequest mGeofencerequest;
    private Marker mCurrent;
    private LocationListener mLocationListner;
    private Button CheckIn;

    GeofenceTransitionService AttendanceService;
    boolean isBound = false;


    private LatLng office = new LatLng(23.813607, 90.433987);
    private String Start = null;
    private String End = null;
    private TrackingDatabase trackingDatabase = new TrackingDatabase(this);


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 5000;
    private static int displacement = 10;
    // = new Employee("0001","Zahid");
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("TIME LOGS");
    private Employee EmployeeHolder;
    DatabaseReference Fireref;


    @Override
    public void onResult(@NonNull Status status) {

    }


    //DatabaseReference ref;
    //GeoAttendance geoattend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        CheckIn = findViewById(R.id.button1);
        CheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateDetails(mLastLocation);
            }
        });
        mGeofencinglient = LocationServices.getGeofencingClient(this);

        //checkPermission();
        //buildGoogleApiClient();


        // setUpLocation();


        //String key = "Key2";
        Intent callerIntent = getIntent();
        Bundle extras = callerIntent.getExtras();
        if (extras != null) {
            String id_holder = (String) extras.get("Key2");
            String name_holder = (String) extras.get("Key");
            EmployeeHolder = new Employee(id_holder, name_holder);
        } else {
            EmployeeHolder = new Employee("test", "test");
        }

        Fireref = ref.child(EmployeeHolder.getDate());

       boolean permisson = false;
       while(permisson==false)
       {
           permisson = checkPermission();
       }
    }

    private void UpdateDetails(Location mLastLocation) {
        RequestUpdates();
        displayLocation();
        //Intent intent= new Intent(MapsActivity.this,GeofenceTransitionService.class);
        Log.d(TAG, "Check in process started");
        UpdateTrackingDatabase();


    }

    private void UpdateTrackingDatabase() {
        boolean Updatecheck;
        Updatecheck = trackingDatabase.insertData(EmployeeHolder.getName(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), EmployeeHolder.getTime(), "null");
        if (Updatecheck == false) {
            Log.d("Tracking Update", "Update Failed");
        } else {
            Log.d("Tracking Update", "Update Successful");
        }
        EmployeeHolder.setLocation(mLastLocation);
        syncSQLiteData();
        boolean UpdateCheck;
        UpdateCheck=create(this,"locatons.json",trackingDatabase.composeJSONfromSQLite());
        if(UpdateCheck==false)
        {
            Log.d(TAG,"File Upload failed");
        }
        else
        {
            Log.d(TAG,"File Upload Succesful");
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //runtime permisson
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSON_REQUEST_CODE);
        } else {
            if (Check_play_services()) {
                buildGoogleApiClient();
                createLocationRequest();
                //displayLocation();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSON_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Check_play_services()) {
                        // buildGoogleApiClient();
                        createLocationRequest();
                        //displayLocation();

                    }

                }
            }
            break;
        }

    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location Display", "No permisson");
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Log.d("Location Display", String.format("Your location changed: %f/%f ", latitude, longitude));

            if (mCurrent != null) {
                mCurrent.remove();
            }

            mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("YOU"));
            String url= getUrl(latitude,longitude);
            Object Data_transfer[]= new Object[2];
            Data_transfer[0]=mMap;
            Data_transfer[1]=url;

            GetNearbyPlaces getNearbyPlaces= new GetNearbyPlaces(getApplicationContext());
            getNearbyPlaces.execute(Data_transfer);


        } else {
            Log.d("Location Display", String.format("Cannot access location"));

        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(displacement);

        RequestUpdates();


    }


    private void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).addApi(LocationServices.API).build();


        }


        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected())
            Log.d("Mushy", "API Client succesfully added");


    }

    @Override
    protected void onStart() {
        super.onStart();
        //buildGoogleApiClient();
        boolean UpdateCheck;

    }

    @Override
    protected void onStop() {

        super.onStop();
        //timeDatabase.insertData(EmployeeHolder.getName(),Start,End);
        /*
        boolean UpdateCheck;
        UpdateCheck = timeDatabase.insertData(EmployeeHolder.getName(),Start,End);
        if(UpdateCheck==false)
            Log.d(TAG,"Could not update");
        else
            Log.d(TAG,"Database Updated Succesfully");
    */
    }

    private boolean Check_play_services() {
        int resultcode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultcode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode)) {
                GooglePlayServicesUtil.getErrorDialog(resultcode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;


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
        //get google client ready
        buildGoogleApiClient();

        // Add a marker in Office and move the camera there

        mMap.addMarker(new MarkerOptions().position(office).title("Marker in Office"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(office));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(office, 18.0f));
        //Display Current Location


        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMylocation();

        //Create GeoFilter Around Office
        mMap.addCircle(new CircleOptions().center(office).radius(35).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(0.1f));

        //Creating GeoFilter
        // RegisterAllGeoFences();






    }

    private String getUrl(double latitude,double longitude)
    {
        StringBuilder googleApiBuilder=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleApiBuilder.append("location="+latitude+","+longitude);
        googleApiBuilder.append("&radius="+PROXIMITY_RADIUS);
        //googleApiBuilder.append("&type=restaurant");
        //googleApiBuilder.append("&keyword=a");
        googleApiBuilder.append("&key=AIzaSyBXr5bBmuPq-HZ1C1TYn-9AJVALcAyWG1E");

        Log.d(TAG,googleApiBuilder.toString());
        return googleApiBuilder.toString();
        //String sample_string="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&keyword=cruise&key=AIzaSyBXr5bBmuPq-HZ1C1TYn-9AJVALcAyWG1E";
        //Log.d(TAG,sample_string);
        //return sample_string;
    }

    private boolean LocationChanged() {
        Location CurrentLocation = null;
        if (checkPermission())
            CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        Log.d(TAG, "position: " + CurrentLocation.getLatitude() + ", " + CurrentLocation.getLongitude());
        if (CurrentLocation != mLastLocation) {

            mLastLocation = CurrentLocation;
            return true;
        } else {
            return false;
        }


    }


    private void enableMylocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //runtime permisson
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSON_REQUEST_CODE);


        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Check_play_services()) {
                //buildGoogleApiClient();
                //createLocationRequest();
                //displayLocation();

            }
            mPermissonDenied = true;
        }
    }


    private Geofence createGeoFence(LatLng latlng, float radius) {
        Log.d(TAG, "Creating GeoFence");
        return new Geofence.Builder().setRequestId("OFFICE").setCircularRegion(latlng.latitude, latlng.longitude, radius).
                setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER).build();
    }

    private GeofencingRequest createGeofencingrequest(Geofence geofence) {
        Log.d(TAG, "Getting GeoFence request");
        return new GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_ENTER).
                addGeofence(geofence).build();
    }

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "Create GeoFence pending Intent");
        if (geoPendingIntent != null) {
            Log.d("Geofence transition", "pending intent succcesful");
            displayLocation();
            return geoPendingIntent;
        } else {
            Log.d("Geofence transition", "NULL pending intent");

        }

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        Log.e("Geofence transition", "Creating new Intent");

        //intent.putExtra("Database",timeDatabase);


        bindService(intent, AttendanceConnection, Context.BIND_AUTO_CREATE);
        if (isBound == true) {
            int Cause = AttendanceService.getGeofenceID();
            EmployeeHolder.setLocation(mLastLocation);
            intent.putExtra("Key3", (Parcelable) EmployeeHolder);
            intent.putExtra("Key4", mLastLocation);
            ProcessFlags(Cause);

            // EmployeeHolder.UpdateLocation(Fireref);

        }
        return PendingIntent.getService(this, GEOFENCE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO: FIX GeoTransition Pending Intent


    }

    private void ProcessFlags(int flag)

    {
        EmployeeHolder.setLocation(mLastLocation);
        //EmployeeHolder.UpdateLocation(Fireref, EmployeeHolder.setTracker());
        if (flag == 2) {
            //timeDatabase.insertEndtime(EmployeeHolder.getTime());
            //EmployeeHolder.UpdateExitTime(Fireref);
            Log.d("UPDATES", "updated Exit");
           /*
            boolean UpdateCheck;
            UpdateCheck = timeDatabase.insertData(EmployeeHolder.getName(),Start,End);
            if(UpdateCheck==false)
                Log.d(TAG,"Could not update");
            else
                Log.d(TAG,"Database Updated Succesfully");
           */
        }
        if (flag == 1)

        {
            //timeDatabase.insertStarttime(EmployeeHolder.getTime());
            Start = EmployeeHolder.getTime();
            //EmployeeHolder.UpdateTime(Fireref);
            Log.d("UPDATES", "updated Entry");
        } else {
            Log.d("UPDATES", "update unsuccesful");
        }

    }


    private void addGeoFence(GeofencingRequest request) {
        Log.d(TAG, "Adding Geofence");
        if (checkPermission()) {
            Log.d(TAG, "ADDED Geofence");
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, request, createGeofencePendingIntent()).setResultCallback(this);
        } else {
            Toast.makeText(this, "Could not connect geofence", Toast.LENGTH_SHORT).show();

        }

    }


    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSON_REQUEST_CODE);

            return true;


        } else {
            return true;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location has changed");
    }

    /*
         public void onLocationChanged(Location location) {
             //RequestUpdates();
             //displayLocation();
             Toast.makeText(this,"Your location has changed",Toast.LENGTH_SHORT).show();
             Log.d("Location Updates","Location Changed");
             mLastLocation=location;



         }
         */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        buildGoogleApiClient();
        createLocationRequest();
        Log.d("API CONNECT", "Connect Succesful");
        mgeofence = createGeoFence(office, 35);
        mGeofencerequest = createGeofencingrequest(mgeofence);
        // addGeoFence(mGeofencerequest);

        if (checkPermission())
            mGeofencinglient.addGeofences(mGeofencerequest, createGeofencePendingIntent()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("GeoFences", "Registering Success");
                }

            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("GeoFences", "Registering Failed");
                }
            });
        else {
            Log.d(TAG, "No permisson");
        }


        //Starting Location Tracking
        setUpLocation();
        //displayLocation();

        //refresh location
        RequestUpdates();
        displayLocation();
        //Starting Location Tracking Service
        startService(new Intent(this, MyLocationService.class));
        Log.d(TAG, "Location Service being tracked");


    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d("API CONNECT", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("API CONNECT", "Connection Failed");
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "My location button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current Location", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissonDenied) {
            //showmissingPermisson();
            mPermissonDenied = false;
        }
    }

    private void RequestUpdates() {
        if (checkPermission() && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, createGeofencePendingIntent());
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,startLocationUpdates());


        } else {
            Log.d("Location Updates", "Could not connect to API");
            //Toast.makeText(this,"Could not connect to API",Toast.LENGTH_SHORT).show();
        }
    }

    private void showmissingPermisson() {

    }

    private ServiceConnection AttendanceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myLocalBinder Binder = (myLocalBinder) service;
            AttendanceService = Binder.getService();
            Log.d(TAG, "Geofence transition Service succesfully conneted");
            isBound = true;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    public void syncSQLiteData() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        ArrayList<HashMap<String, String>> Location_List = trackingDatabase.GetAllData();
        if (Location_List.size() != 0) {
            if (trackingDatabase.dbSyncCount() != 0) {
                requestParams.put("Tracking List", trackingDatabase.composeJSONfromSQLite());
                client.post("https://notepad.pw/track1234", requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "Data uploaded to SQL server");
                        Log.d(TAG, trackingDatabase.composeJSONfromSQLite());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d(TAG, "Data was not uploaded to SQL server");
                        Log.d(TAG, trackingDatabase.composeJSONfromSQLite());

                    }
                });
            }
        }


    }


    public String read(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException fileNotFound) {
            return null;
        } catch (IOException ioException) {
            return null;
        }
    }

    public boolean create(Context context, String fileName, String jsonString){
        String FILENAME = "storage.json";
        try {
            FileOutputStream fos = context.openFileOutput(fileName,Context.MODE_PRIVATE);
            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }
            fos.close();
            return true;
        } catch (FileNotFoundException fileNotFound) {
            return false;
        } catch (IOException ioException) {
            return false;
        }

    }

    public boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }



}


