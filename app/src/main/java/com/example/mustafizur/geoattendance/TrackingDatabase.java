package com.example.mustafizur.geoattendance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;


public class TrackingDatabase extends SQLiteOpenHelper
{



    private static Calendar mCalendar = new GregorianCalendar();
    private static String GetDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
        return dateFormat.format(mCalendar.getTime());

    }
    private static final String DatabaseName ="LOCATION_UPDATES.db";
    private static final String TableName =  "Updated_Locations";
    private static final String COL_1 = "Entry";
    private static final String COL_2 = "Name";
    private static final String COL_3 ="Latitude";
    private static final String COL_4 = "Longitude";
    private static final String COL_5 = "Time";
    private static final String COL_6= "Place";
    private static final String COL_7 = "updateStatus";



    private static final String SQL_Entry=  String.format("CREATE TABLE %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT,%s TEXT NOT NULL );",
            TableName,
            COL_1,
            COL_2,
            COL_3,COL_4,COL_5,COL_6,COL_7);




    public TrackingDatabase(Context context) {
        super(context, DatabaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_Entry);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TableName);
        onCreate(db);
        }

    public boolean insertData(String name,double latitude,double longitude,String time,String place)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,latitude);
        contentValues.put(COL_4,longitude);
        contentValues.put(COL_5,time);
        contentValues.put(COL_6,place);
        contentValues.put(COL_7,"no");

        long result = db.insert(TableName,null,contentValues);
        if(result==-1)
            return false;
        else {

            db.close();
            return true;
        }
    }

    public ArrayList<HashMap<String,String>> GetAllData() {

        ArrayList<HashMap<String,String>> Tracking_List= new ArrayList<HashMap<String,String>>();
        String selectQuery = "SELECT  * FROM Updated_Locations where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Entry", cursor.getString(0));
                map.put("Name", cursor.getString(1));
                map.put("Latitude", String.valueOf(cursor.getDouble(2)));
                map.put("Longitude",String.valueOf(cursor.getDouble(3)));
                map.put("Time",cursor.getString(4));
                map.put("Place",cursor.getString(5));
                Tracking_List.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return Tracking_List;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */

    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM Updated_Locations where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Entry", cursor.getString(0));
                map.put("Name", cursor.getString(1));
                map.put("Latitude", cursor.getString(2));
                map.put("Longitude", cursor.getString(3));
                map.put("Time", cursor.getString(4));
                map.put("Place", cursor.getString(5));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }


    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync needed\n";
        }
        return msg;
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM Updated_Locations where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update Updated_Locations set updateStatus = '"+ status +"' where Entry="+"'"+ id +"'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
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

    public void CreateFile()
    {
        //create(this,"Locations.json",composeJSONfromSQLite());

    }
}




