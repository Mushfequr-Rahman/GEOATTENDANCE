package com.example.mustafizur.geoattendance;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download_Url
{
    private static final String TAG=Download_Url.class.getSimpleName();

    public String read_Url(String my_Url) throws IOException
    {
        String data="";
        InputStream inputStream=null;
        HttpURLConnection httpURLConnection=null;
        try{
            URL url= new URL(my_Url);
            httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream=httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            String line ="";
            while((line=br.readLine()) != null)
            {
                sb.append(line);
            }

            data=sb.toString();
            br.close();

        }catch (MalformedURLException E)
        {
            E.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }
        Log.d(TAG,"Done downloading Url");
        return data;

    }

}
