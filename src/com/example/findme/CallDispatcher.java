package com.example.findme;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;


public class CallDispatcher {

    /*To be moved into a properties file with encryption.*/
    protected final String username = "xyz@gmail.com";
    protected final String password = "123456789";
    protected final String server_addrs = "http://54.186.15.10:3000";
    
    private static final String TAG = "com.example.findme";
    
    /**
     * Function to register the user in back-end server.
     * 
     * @return authentication code or FAILURE
     */
    public String createUser(){
        Log.d(TAG, "Entering createUser() <<");
        HttpClient client = new DefaultHttpClient();
        
        TelephonyManager tele = FindMeHelper.getInstance().getTeleManager();
        
        String loginValue = null;
        String passValue = null;
        String uidValue = null;
        JSONObject jobj = new JSONObject();
        
        try {
             loginValue    = username; //issues with @ conversion
             passValue  = URLEncoder.encode(password, "UTF-8");
             uidValue   = URLEncoder.encode(tele.getDeviceId(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.e(TAG, "Encoding error : " + e1.getMessage());
        }

        
        HttpPost postRequest = new HttpPost(server_addrs+"/user/create");
        
        try {
            jobj.put("username", loginValue);
            jobj.put("password", passValue);
            jobj.put("uuid", uidValue);
            StringEntity se = new StringEntity(jobj.toString());

            postRequest.setEntity(se);

            postRequest.setHeader("Content-Type", "application/json");
            
            HttpResponse response = client.execute(postRequest);
            
            HttpEntity res = response.getEntity();
            String responseString = EntityUtils.toString(res, "UTF-8");
            JSONObject obj = new JSONObject(responseString);
            
            if(obj.has("auth_token")){
                return obj.getString("auth_token");
            }
            else{
                String msg = obj.getString("error");
                
                if(msg.contains("Email already exists")){
                    return authenticate();
                }
            }
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        }
        Log.d(TAG, "Exiting createUser() >>");
        return FindMeHelper.FAILURE;
    }
    
    /**
     * Function to fetch the location of all friends of the user
     * 
     * @param auth_code
     * @return List or friends or FAILURE message
     */
    public String fetchFriends(String auth_code){
        Log.d(TAG, "Entering fetchFriends() <<");
        HttpClient client = new DefaultHttpClient();
        
        String auth_token = null;
        
        try {
            auth_token    = URLEncoder.encode(auth_code, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        
        Uri.Builder builder = Uri.parse(server_addrs).buildUpon();
        builder.path("/friends/location/all");      
        builder.appendQueryParameter("auth_token", auth_token);
        
        String url = builder.build().toString();
        
        HttpGet getRequest = new HttpGet(url);

        
        try {
            
            HttpResponse response = client.execute(getRequest);
            
            HttpEntity res = response.getEntity();
            String responseString = EntityUtils.toString(res, "UTF-8");
            JSONObject obj = new JSONObject(responseString);

            if(obj.has("friends")){
                Log.d(TAG, "Exiting fetchFriends() >>");
                return responseString;
            }
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        }
        Log.d(TAG, "Exiting fetchFriends() >>");
        return FindMeHelper.FAILURE;
    }
    
    /**
     * Authenticate user to back-end server.
     * 
     * @return authentication key or FAILURE message
     */
    public String authenticate(){
        Log.d(TAG, "Entering authenticate() <<");
        HttpClient client = new DefaultHttpClient();
        
        TelephonyManager tele = FindMeHelper.getInstance().getTeleManager();
        
        String loginValue = null;
        String passValue = null;
        String uidValue = null;
        JSONObject jobj = new JSONObject();
        
        try {
             loginValue    = username;
             passValue  = URLEncoder.encode(password, "UTF-8");
             uidValue   = URLEncoder.encode(tele.getDeviceId(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.e(TAG, "Parsing error");
        }

        
        HttpPost postRequest = new HttpPost(server_addrs + "/user/authenticate");
        
        try {
            jobj.put("username", loginValue);
            jobj.put("password", passValue);
            jobj.put("uuid", uidValue);
            StringEntity se = new StringEntity(jobj.toString());

            postRequest.setEntity(se);

            postRequest.setHeader("Content-Type", "application/json");
            
            HttpResponse response = client.execute(postRequest);
            
            HttpEntity res = response.getEntity();
            String responseString = EntityUtils.toString(res, "UTF-8");
            JSONObject obj = new JSONObject(responseString);
            
            if(obj.has("auth_token")){
                Log.d(TAG, "Exiting authenticate() >>");
                return obj.getString("auth_token");
            }
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        }
        Log.d(TAG, "Exiting authenticate() >>");
        return FindMeHelper.FAILURE;
        
    }
    
    /**
     * Function to update user location to back-end.
     * 
     * @param authentication code
     * @param log
     * @param lat
     * @return SUCCESS or FAILURE message.
     */
    public String updateLocation(String code, String log, String lat){
       Log.d(TAG, "Entering updateLocation() <<");
       HttpClient client = new DefaultHttpClient();
       
        
        String auth_code = null;
        String longitute = null;
        String latitude = null;
        JSONObject jobj = new JSONObject();
        
        try {
            auth_code    = URLEncoder.encode(code, "UTF-8");
            longitute  = URLEncoder.encode(log, "UTF-8");
            latitude   = URLEncoder.encode(lat, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.e(TAG, "Parsing error");
        }

        
        HttpPost postRequest = new HttpPost(server_addrs + "/user/location/update");
        
        try {
            jobj.put("auth_token", auth_code);
            jobj.put("latitude", latitude);
            jobj.put("longitude", longitute);
            StringEntity se = new StringEntity(jobj.toString());

            postRequest.setEntity(se);

            postRequest.setHeader("Content-Type", "application/json");
            
            HttpResponse response = client.execute(postRequest);
            
            HttpEntity res = response.getEntity();
            String responseString = EntityUtils.toString(res, "UTF-8");
            JSONObject obj = new JSONObject(responseString);
            
            if(obj.has("nearby")){
                Log.d(TAG, "Exiting updateLocation() >>");
                return FindMeHelper.SUCCESS;
            }
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
        }
        Log.d(TAG, "Exiting updateLocation() >>");
        return FindMeHelper.FAILURE;
        
    }
    
    /**
     * Function to register for push notification.
     * 
     * @param code
     * @param token
     * @return SUCCESS or FAILURE message.
     */
    public String resigterNotification(String code, String token){
        Log.d(TAG, "Entering resigterNotification() <<");
        HttpClient client = new DefaultHttpClient();
        
         
         String auth_code = null;
         String device_push_token = null;
         JSONObject jobj = new JSONObject();
         
         try {
             auth_code    = URLEncoder.encode(code, "UTF-8");
             device_push_token  = URLEncoder.encode(token, "UTF-8");
         } catch (UnsupportedEncodingException e1) {
             Log.e(TAG, "Parsing error");
         }

         
         HttpPost postRequest = new HttpPost(server_addrs + "/user/update");
         
         try {
             jobj.put("auth_token", auth_code);
             jobj.put("device_push_token", device_push_token);
             StringEntity se = new StringEntity(jobj.toString());

             postRequest.setEntity(se);

             postRequest.setHeader("Content-Type", "application/json");
             
             HttpResponse response = client.execute(postRequest);
             
             HttpEntity res = response.getEntity();
             String responseString = EntityUtils.toString(res, "UTF-8");
             JSONObject obj = new JSONObject(responseString);
             Log.d("com.example.findme", responseString);
             if(obj.has("success")){
                 Log.d(TAG, "Exiting resigterNotification() >>");
                 return FindMeHelper.SUCCESS;
             }
             
         } catch (UnsupportedEncodingException e) {
             Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
         } catch (ClientProtocolException e) {
             Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
         } catch (IOException e) {
             Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
         } catch (JSONException e) {
             Log.e(TAG, "Failed to retrive information from server! :: " + e.getMessage());
         }
         Log.d(TAG, "Exiting resigterNotification() >>");
         return FindMeHelper.FAILURE;
         
     }
    
}
