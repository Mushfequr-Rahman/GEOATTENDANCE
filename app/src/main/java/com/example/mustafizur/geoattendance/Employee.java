package com.example.mustafizur.geoattendance;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public  class Employee implements Parcelable {

    private String mEmployeeID;
    private String name;
    private Location currentLocation;
    private Calendar MyCalendar;








    public Employee()
    {
        mEmployeeID="00000000";
        name=null;


    }

    public Employee(String Employee_ID, String Employee_name)
    {
        mEmployeeID=Employee_ID;
        name=Employee_name;
        setMyCalendar();
    }

    public Employee(DatabaseReference ref)
    {

    }

    protected Employee(Parcel in) {
        mEmployeeID = in.readString();
        name = in.readString();
        currentLocation = in.readParcelable(Location.class.getClassLoader());
        setMyCalendar();
    }

    public static final Creator<Employee> CREATOR = new Creator<Employee>() {
        @Override
        public Employee createFromParcel(Parcel in) {
            return new Employee(in);
        }

        @Override
        public Employee[] newArray(int size) {
            return new Employee[size];
        }
    };

    public void UpdateDatabase(DatabaseReference ref)
    {
      String Id=ref.push().getKey();
      ref.setValue(name);

    }
    public void UpdateDate(DatabaseReference ref)
    {
        String alpha = getDate();
        ref.child(name).child("Logs").child("Date").setValue(alpha);

    }

    public void UpdateTime(DatabaseReference ref)
    {
        //String Id=ref.push().getKey();
        String holder = getDate();
        ref.child(name).child("Logs").child("Start time").setValue(getTime());

    }
    public void UpdateExitTime(DatabaseReference ref)
    {

        ref.child(name).child("Logs").child("End time").setValue(getTime());

    }

    private void setMyCalendar()
    {

        //TimeZone timeZone= new TimeZone.getTimeZone("Asia/Dhaka");

        //MyCalendar= new GregorianCalendar(timeZone,Locale.CANADA);
          MyCalendar = new GregorianCalendar();


    }


    public  String getName()
    {
        return this.name;
    }

    public   String getDate()
    {
      SimpleDateFormat date_format = new SimpleDateFormat("yyyy/MM/dd");
      String holder;

      holder=  (date_format.format(MyCalendar.getTime()));
      return holder;
    }

    public String getTime()
    {
       // Date date = MyCalendar.getTime();
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");

        return time_format.format(MyCalendar.getTime());

    }

    public void setLocation(Location location)
    {
        currentLocation=location;

    }
    public Tracker setTracker()
    {
        Tracker t = new Tracker(currentLocation.getLatitude(),currentLocation.getLongitude(),name,getTime());
        return t;
    }

    public void UpdateLocation(DatabaseReference ref,Tracker tracker)

    {
        ref.child(name).child("Tracking info").setValue(tracker);


    }


    public String getUsername() {
        return this.mEmployeeID;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mEmployeeID);
        dest.writeString(name);
        dest.writeParcelable(currentLocation,0);

    }
}
