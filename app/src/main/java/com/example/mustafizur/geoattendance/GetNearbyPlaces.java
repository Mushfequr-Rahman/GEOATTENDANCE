package com.example.mustafizur.geoattendance;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GetNearbyPlaces extends AsyncTask<Object,String,String> {

    String Google_Places_data;
    GoogleMap mMap;
    String url;
    Marker marker;
    Context context;
    private final String TAG=GetNearbyPlaces.class.getSimpleName();

    GetNearbyPlaces(Context current_context)
    {
        this.context=current_context;
    }
    @Override
    protected void onPostExecute(String s) {
       List<HashMap<String,String>> nearby_place_list=null;
       Data_Parser data_parser = new Data_Parser();
       nearby_place_list=data_parser.parse(s);
       ShowNearbyPlaces(nearby_place_list);

    }

    @Override
    protected String doInBackground(Object... objects) {
        mMap=(GoogleMap) objects[0];
        url= (String) objects[1];

        Download_Url download_url=new Download_Url();
        try{
            Google_Places_data=download_url.read_Url(url);
        }catch(IOException e)
        {
            e.printStackTrace();
        }


         return Google_Places_data;
    }

    public void ShowNearbyPlaces(List<HashMap<String,String>> nearby_Places_list)
    {
        for(int i=0;i<nearby_Places_list.size();i++)
        {
            MarkerOptions markerOptions=new MarkerOptions();
            HashMap<String,String> googlePlaces= nearby_Places_list.get(i);
            String place_name= googlePlaces.get("place_name");
            String vicinity=googlePlaces.get("vicinity");
            double lat= Double.parseDouble(googlePlaces.get("latitude"));
            double lng=Double.parseDouble(googlePlaces.get("longitude"));
            String src="https://x1.xingassets.com/assets/frontend_minified/img/users/nobody_m.original.jpg";
            String url=googlePlaces.get("icon");
            LatLng latLng= new LatLng(lat,lng);
            markerOptions.position(latLng);
            markerOptions.title(vicinity);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapfromView(get_profile_image(src),place_name,url)));
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

            if(i==8)
            {
                break;
            }



        }
    }

    private Bitmap getMarkerBitmapfromView(Bitmap bitmap,String name,String url)
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp= Bitmap.createBitmap(300,150,conf);
        Canvas Canvas1= new Canvas(bmp);

        Paint color=new Paint();
        color.setTextSize(30);
        color.setColor(Color.RED );

        Canvas1.drawBitmap(get_profile_image(url),0,0,color);
        Canvas1.drawText(name,30,40,color);

        return bmp;

       /*
        View view=((LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_view_marker,null);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        */
       // return returnedBitmap;
    }

   private Bitmap get_profile_image(String url)  {
       Bitmap current_image=null;
       AsyncTask<String,Void,Bitmap> download_image= new AsyncTask<String, Void, Bitmap>() {
           Bitmap result=null;
           @Override
           protected Bitmap doInBackground(String... strings) {
               return GetBitmapfromUrl(strings[0]);
           }

           @Override
           protected void onPostExecute(Bitmap bitmap) {
                 result=bitmap;
           }


       };

       try {
           current_image= download_image.execute(url).get();
       } catch (InterruptedException e) {
           e.printStackTrace();
       } catch (ExecutionException e) {
           e.printStackTrace();
       }

       return current_image;
   }

  private  Bitmap GetBitmapfromUrl(String src)
  {
      try {
          Log.d(TAG,src);
          URL url = new URL(src);
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          connection.setDoInput(true);
          connection.connect();
          InputStream input = connection.getInputStream();
          Bitmap myBitmap = BitmapFactory.decodeStream(input);
          return myBitmap;
      } catch (IOException e) {
          e.printStackTrace();
          return null;
      }
  }

}
