package com.example.findme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FindMeHelper {

    public final static String CREATE_OPTION = "create";
    public final static String AUTH_OPTION = "authenticate";
    public final static String FRIEND_OPTION = "friend";
    public final static String SUCCESS = "success";
    public final static String FAILURE = "failure";
    
    private Context context;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    
    private final static FindMeHelper instance = new FindMeHelper();
    
    public static FindMeHelper getInstance(){
        return instance;
    }
    
    private FindMeHelper(){
        
    }
    
    private TelephonyManager teleManager;

    public TelephonyManager getTeleManager() {
        return teleManager;
    }

    public void setTeleManager(TelephonyManager teleManager) {
        this.teleManager = teleManager;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    
    
    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
    
    

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void saveAccountDatatoPref(Boolean data){
        
        SharedPreferences prefs = context.getSharedPreferences(FindMeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        
        prefsEditor.putBoolean("ACC_CREATED_FLAG", data);
        prefsEditor.putBoolean("CURRENT_TASK", false);
        prefsEditor.commit();
        
    }
    
    public void saveAuthCodetoPref(String data){
        
        SharedPreferences prefs = context.getSharedPreferences(FindMeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        
        prefsEditor.putString("AUTH_CODE", data);
        prefsEditor.commit();
        
    }
    
    public String getAuthCodeFromPref(){
        
        SharedPreferences prefs = context.getSharedPreferences(FindMeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        
        String value = prefs.getString("AUTH_CODE", null);
        
        return value;
    }
    
    public boolean getCurrentTaskFromPref(){
        
        SharedPreferences prefs = context.getSharedPreferences(FindMeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        
        boolean currentTask = prefs.getBoolean("CURRENT_TASK", false);
        
        return currentTask;
    }
    
    public void addMarkerToMap(String data){

        SharedPreferences prefs = context.getSharedPreferences(FindMeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        
        
        googleMap.clear();
        Location location = null;
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if(location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if(location == null){
            location = new Location("");
            location.setLatitude(0.0);
            location.setLongitude(0.0);
        }
        
        LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                myLoc, 16));

        googleMap.addMarker(new MarkerOptions()
        .title("My Location")
        .position(myLoc));
        
        
        try {
            JSONObject obj = new JSONObject(data);
            
            if(obj.has("friends")){
                JSONArray frnds = obj.getJSONArray("friends");
                
                for(int i=0; i < frnds.length(); i++){
                    JSONObject user = frnds.getJSONObject(i);
                    String username = user.getString("username");
                    
                    Object loc = user.get("location");
                    
                    
                    if(loc instanceof JSONArray){
                        Double lng = ((JSONArray)loc).getDouble(1);
                        Double lat = ((JSONArray)loc).getDouble(0);

                        Object updatedOn = user.get("last_updated");
                    
                        LatLng frndLoc = new LatLng(lat, lng);
                        
                        googleMap.addMarker(new MarkerOptions()
                        .title(username)
                        .snippet("Updated on : " + updatedOn)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .position(frndLoc));
                        
                    }
                    
                    }
                }
                
            googleMap.animateCamera( CameraUpdateFactory.zoomTo( 2.0f ) );    
            
        } catch (JSONException e) {
            Log.e("com.example.findme", "Parsign error" + e.getMessage());
        }
        
        prefsEditor.putBoolean("CURRENT_TASK", false);
        prefsEditor.commit();
        
    }
}
