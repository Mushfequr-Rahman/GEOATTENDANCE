package com.example.mustafizur.geoattendance;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Data_Parser {

    private static final String TAG=Data_Parser.class.getSimpleName();
    private HashMap<String,String> get_Places(JSONObject googlePlaceJson) {
        Log.d(TAG,"get_Places");
        HashMap<String, String> googlePlacesMap = new HashMap<>();
        String place_Name = "--NA--";
        String vicinity = "--NA--";
        String latitude = "";
        String longitude = "";
        String reference = "";
        String icon="";



        try {
            if (!googlePlaceJson.isNull("name")) {
                place_Name=googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity"))
            {
                vicinity=googlePlaceJson.getString("vicinity");
            }
            latitude=googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude=googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference=googlePlaceJson.getString("reference");
            icon=googlePlaceJson.getString("icon");

            googlePlacesMap.put("place_name",place_Name);
            googlePlacesMap.put("vicinity",vicinity);
            googlePlacesMap.put("latitude",latitude);
            googlePlacesMap.put("longitude",longitude);
            googlePlacesMap.put("reference",reference);
            googlePlacesMap.put("icon",icon);

        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return googlePlacesMap;

    }

    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray)
    {
        Log.d(TAG,"getPlaces");
        int count=jsonArray.length();
        List<HashMap<String,String>> placeList= new ArrayList<>();
        HashMap<String,String> place_Map=null;
        for(int i=0;i<count;i++)
        {
            try {
                place_Map = get_Places((JSONObject) jsonArray.get(i));
                placeList.add(place_Map);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return placeList;

    }
    public List<HashMap<String,String>> parse(String jsonData)
    {
        Log.d(TAG,"parse");
        JSONArray jsonArray=null;
        JSONObject jsonObject;
        try{
            jsonObject= new JSONObject(jsonData);
            jsonArray= jsonObject.getJSONArray("results");
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }


}
