package com.example.findme;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Custom service class to poll GPS location.
 *
 */
public class MyService extends Service implements SensorEventListener{

    private static final float kFilteringFactor = 0.1f;
    float[] accel = new float[3];
    
    private LocationManager locationManager;
    private SensorManager sensorManager;
    
    private Sensor mAccelerometer;
    
    private LocationListener locationListner;
    private boolean has_set_move = false;
    private boolean has_set_still = false;
    private int window_size = 0;
    private ArrayList<Float> val_x = new ArrayList<Float>();
    private ArrayList<Float> val_y = new ArrayList<Float>();
    private ArrayList<Float> val_z = new ArrayList<Float>();
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationListner = new MyLocationListener();
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        accel[0] = 0;
        accel[1] = 0;
        accel[2] = 0;
        
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
    }
    
    
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

      return Service.START_STICKY;
    }

    /**
     * Check accelerometer data for movement for energy efficiency and set the polling time for GPS.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER){
            return;
        }
        
        if(window_size < 20){
            val_x.add(event.values[0]);
            val_y.add(event.values[1]);
            val_z.add(event.values[2]);
            window_size++;
            return;
        }
        
        window_size = 0;
        float mean_x = mean(val_x);
        float mean_y = mean(val_y);
        float mean_z = mean(val_z);
        
        val_x.clear();
        val_y.clear();
        val_z.clear();
        
        float[] linear_acceleration = new float[3];
        
        accel[0] = (mean_x * kFilteringFactor) + (accel[0] * (1.0f - kFilteringFactor));
        accel[1] = (mean_y * kFilteringFactor) + (accel[1] * (1.0f - kFilteringFactor));
        accel[2] = (mean_z * kFilteringFactor) + (accel[2] * (1.0f - kFilteringFactor));
 
        linear_acceleration[0] = (mean_x - accel[0]);
        linear_acceleration[1] = (mean_y - accel[1]);
        linear_acceleration[2] = (mean_z - accel[2]);
 
        float magnitude = 0.0f;
        magnitude = (float)Math.sqrt(linear_acceleration[0]*linear_acceleration[0]+linear_acceleration[1]*linear_acceleration[1]+linear_acceleration[2]*linear_acceleration[2]);
        magnitude = Math.abs(magnitude);
        if(magnitude>0.5){
                
                if(!has_set_move){
                    locationManager.removeUpdates(locationListner);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, locationListner);
                    has_set_move = true;
                    has_set_still = false;
                }
            
        }
        else{
            
           if(!has_set_still){
                locationManager.removeUpdates(locationListner);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 5f, locationListner);
                has_set_still = true;
                has_set_move = false;
            }
            
            //stopService(new Intent(this, MyService.class));
        }
        
    }
    
    
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }
    
    
    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            
            SharedPreferences prefs = getSharedPreferences(FindMeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            
            String auth_code = prefs.getString("AUTH_CODE", null);
            
            
            
            if(auth_code != null){
                sendLocationUpdateToBackend(auth_code, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            }
            
        }
        public void onStatusChanged(String s, int i, Bundle b) {            
        }
        public void onProviderDisabled(String s) {
        }
        public void onProviderEnabled(String s) {           
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        if(locationListner != null){
            locationManager.removeUpdates(locationListner);
            locationListner = null;
        }
    }
    
    public float mean(ArrayList<Float> data) {
        float sum = 0;
        for (Float f : data) {
            sum += f;
        }
        return sum / data.size();
    }
    
    private void sendLocationUpdateToBackend(String token, String latitude, String longitude) {

        
        
        
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                
                String tok = params[0];
                String lat = params[1];
                String lng = params[2];
                
                
                CallDispatcher caller = new CallDispatcher();
                caller.updateLocation(tok, lng, lat);
                
                return null;
            }

        }.execute(token, latitude, longitude);
        

    }
    
}
