package com.example.angus.tweet;

import android.app.Activity;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.StatusLine;
import org.json.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;


/**
 * Created by Angus on 16/1/27.
 */
public class TwitterHelper {
    private static final String TWITTER_SEARCH =
            "https://api.twitter.com/1.1/search/tweets.json?q=";
    private static final int HTTP_STATUS_OK = 200;
    //private static byte[] buff = new byte[1024];

    public static class ApiException extends Exception{
        public ApiException(String msg){
            super(msg);
        }
        public ApiException(String msg,Throwable thr){
            super(msg,thr);
        }
    }

    public static class ParseException extends Exception{
        public ParseException(String msg,Throwable thr){
            super(msg,thr);
        }
    }

    protected static synchronized String downloadFromServer(String keyword)
            throws ApiException{
        //String url = String.format(TWITTER_SEARCH, Uri.encode(keyword));
        //String url = TWITTER_SEARCH + Uri.encode(keyword) + "&result_type=popular&count=200";
        //String url = TWITTER_SEARCH + Uri.encode(keyword) + "&count=200&result_type=popular";
        //String url = TWITTER_SEARCH + Uri.encode(keyword) + "&count=200";
        String url = TWITTER_SEARCH + Uri.encode(keyword) + "&result_type=popular";
        Log.e("finial url",url);
        try{
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestProperty("Authorization",
                    "Bearer AAAAAAAAAAAAAAAAAAAAADI2kAAAAAAA1lkIKWlBnGbg5vWwVhQ9iWrl%2B8o%3DAfGrqDg2xLd9KV6VN7vHFdrDd3KyOZncBwBZBdDHUO3Z8ZApQq");
            InputStream response = httpURLConnection.getInputStream();
            int status = httpURLConnection.getResponseCode();
            if(status != HTTP_STATUS_OK) throw new ApiException(
                    "Invalid response from api.twitter.com. Response Code :" + status);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            StringBuffer body = new StringBuffer();
            for(String line;(line=reader.readLine())!=null;){
                System.out.print(line);
                body.append(line);
            }

            //ByteArrayOutputStream content = new ByteArrayOutputStream();
            //int readCount = 0;
            //while((readCount = reader.read())!=-1)
                //content.write(buff,0,readCount);
            //return new String(content.toByteArray());
            
            return body.toString();
        }catch(Exception e){
            //TODO AUTO-GENERATRED CATCH BLOCK
            e.printStackTrace();;
            throw new ApiException("Problem using twitter api ",e);
        }
    }
}
