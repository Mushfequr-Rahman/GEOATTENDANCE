package com.example.mustafizur.geoattendance;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class GeoAttendance extends AppCompatActivity
{

    private EditText username;
    private EditText userpassword;
    private TextView Info;
    private Button login;
    public static  Employee EmployeeHolder;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("TIME LOGS");
    DatabaseReference Employeeref =ref.child("Employees");



    private static final int Notification_ID =10056;








    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, GeoAttendance.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        username= findViewById(R.id.editText);
        userpassword= findViewById(R.id.editText2);
        Info= findViewById(R.id.Textview);
        login= findViewById(R.id.button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(username.getText().toString(),userpassword.getText().toString());
            }
        });
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                int MY_PERMISSIONS_REQUEST_READ_CONTACTS=0;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        SaveContacts(get_Android_Contacts());
        makeNotificationIntent(this,NOTIFICATION_MSG);

    }

    private  void validate(String username,String Password )
    {
        if((username.equals("alpha")) && (Password.equals("beta") ))
        {
            Intent intent= new Intent(GeoAttendance.this,MapsActivity.class);
             startActivity(intent);
             EmployeeHolder = new Employee(username,Password);
             intent.putExtra("Key",EmployeeHolder.getName());
             intent.putExtra("Key2",EmployeeHolder.getUsername());
             //EmployeeHolder.UpdateDatabase(ref);
             //  EmployeeHolder.UpdateDate(ref);
             //EmployeeHolder.UpdateTime(ref);
             //EmployeeHolder.UpdateExitTime(ref);


        }
        else if((username.equals("Admin"))&&(Password.equals("Gamma")))
        {
            Intent AdminIntent = new Intent(GeoAttendance.this,Admin_Map.class);
            startActivity(AdminIntent);
        }
        else
        {
            Info.setText("Wrong Username or Password,Try again");
        }

    }

    //Retrieving Contacts from the user
    public class Android_Contact
    {
        public String android_Contact_name ="";
        public String android_Contact_number="";
        public int android_Contact_ID= 0;
    }

    public ArrayList<Android_Contact> get_Android_Contacts()
    {
        ArrayList<Android_Contact>  Contacts_Array = new ArrayList<Android_Contact>();

        Cursor Android_Contacts_Cursor = null;
        ContentResolver contentResolver = getContentResolver();
        try{
            Android_Contacts_Cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        }catch (Exception ex)
        {
            Log.e("Error on Contact ",ex.getMessage());
        }

        //Getting All Contacts

       if(Android_Contacts_Cursor.getCount() >0)
       {
           while(Android_Contacts_Cursor.moveToNext())
           {
               Android_Contact android_contact =new Android_Contact();
                String Contact_ID =  Android_Contacts_Cursor.getString(Android_Contacts_Cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String Contact_Display_Name = Android_Contacts_Cursor.getString(Android_Contacts_Cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));


                // Setting the values

               android_contact.android_Contact_name= Contact_Display_Name;


               //Getting the phone number
               int hasPhonenumber = Integer.parseInt(Android_Contacts_Cursor.getString(Android_Contacts_Cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
               if(hasPhonenumber>0)
               {
                   Cursor PhoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                   ,null
                   ,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                           ,new String[] {Contact_ID}
                           ,null);


                   while(PhoneCursor.moveToNext())
                   {
                       String phoneNumber = PhoneCursor.getString(PhoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                       android_contact.android_Contact_number = phoneNumber;
                   }
                   PhoneCursor.close();

               }
               Contacts_Array.add(android_contact);


           }
       }

       return Contacts_Array;

    }

    public void SaveContacts(ArrayList<Android_Contact> androidContacts)
    {
        String Contacts_json = new Gson().toJson(androidContacts);
        //TODO: Sync JSON FILE WITH ONLINE SERVER
        Log.d("Contactss",Contacts_json);
    }


}
