package com.example.findme;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class FindMeActivity extends Activity
{
    
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    
    private static final String TAG = "com.example.findme";

    String SENDER_ID = "46907679678";
    
    /** Called when the activity is first created. */
    // Google Map
    private GoogleMap googleMap;
    private LocationManager locationManager;
    
    private GoogleCloudMessaging gcm;
    private Context context;

    private String regid;

 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        TelephonyManager tele = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
        
        FindMeHelper.getInstance().setTeleManager(tele);
        FindMeHelper.getInstance().setContext(this);
        FindMeHelper.getInstance().setLocationManager(locationManager);
        
        context = getApplicationContext();
            
            //Check account status
            initializeAccount();
            
            Log.i(TAG, "GCM Code running");
            //Initialize google messaging service
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            
            // Loading map
            initilizeMap();
            
            //Get data
            findFriends();
            
            Intent i= new Intent(this, MyService.class);
            startService(i);
            
 
    }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        Log.d(TAG, "Entering getRegistrationId >>");
        final SharedPreferences prefs = this.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        Log.i(TAG, "red id  :: " + registrationId);
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        Log.d(TAG, "Exiting getRegistrationId <<");
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            Log.e(TAG, "Could not get package name: " + e.getMessage());
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // Send the registration ID to server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to app.
                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

        }.execute(null, null, null);
    }
    
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {

        
          
         
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                
                String token = params[0];
                
                
                CallDispatcher caller = new CallDispatcher();
                
                boolean currentTask = FindMeHelper.getInstance().getCurrentTaskFromPref();
                
                while(currentTask){
                    currentTask = FindMeHelper.getInstance().getCurrentTaskFromPref();
                }
                
                Log.d(TAG, ""+"Sending registration id...");
                String code = FindMeHelper.getInstance().getAuthCodeFromPref();
                caller.resigterNotification(code, token);
                
                return null;
            }

        }.execute(regid);
        

    }
    
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = this.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    private void findFriends(){
            
            AsyncTaskRunner runner = new AsyncTaskRunner();            
            
            runner.execute(FindMeHelper.FRIEND_OPTION);
        
    }
    
    private void initializeAccount() {
        SharedPreferences prefs = this.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        
        prefsEditor.putBoolean("CURRENT_TASK", true);
        prefsEditor.commit();
        
        boolean isSet = prefs.getBoolean("ACC_CREATED_FLAG", false);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        if(!isSet){          
           
            runner.execute(FindMeHelper.CREATE_OPTION);
            
        }
        else{
            runner.execute(FindMeHelper.AUTH_OPTION);
        }
        
    }
    
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            
            FindMeHelper.getInstance().setGoogleMap(googleMap);
            
            Location location = null;
            
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }else{
                
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Log.d("com.example.findme", "GPS :: "+location);
                }
                if(location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Log.d("com.example.findme", "NTWRK :: "+location);
                }
                if(location == null){
                    Log.d("com.example.findme", "Could not obtain user current location");
                    location = new Location("");
                    location.setLatitude(0.0);
                    location.setLongitude(0.0);
                }
                
                LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());

                googleMap.addMarker(new MarkerOptions()
                .title("My Location")
                .position(myLoc));

            }
        }
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
        findFriends();
    }
}
