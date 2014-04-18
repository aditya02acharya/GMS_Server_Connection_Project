package com.example.findme;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncTaskRunner extends AsyncTask<String, String, String>{


    @Override
    protected String doInBackground(String... params) {
        
        Log.d("com.example.findme", "Entering doInBackground >>");
        String call_name = params[0];

        CallDispatcher caller = new CallDispatcher();
        
        
        if(call_name.equals(FindMeHelper.CREATE_OPTION)){
            
            String status = caller.createUser();
            
            if(status.equals(FindMeHelper.FAILURE)){
                FindMeHelper.getInstance().saveAccountDatatoPref(false);
            }else{
                FindMeHelper.getInstance().saveAuthCodetoPref(status);
                FindMeHelper.getInstance().saveAccountDatatoPref(true);
            }
            
        }
        else if(call_name.equals(FindMeHelper.FRIEND_OPTION)){
            
            boolean currentTask = FindMeHelper.getInstance().getCurrentTaskFromPref();
            
            while(currentTask){
                currentTask = FindMeHelper.getInstance().getCurrentTaskFromPref();
            }
            
            String code = FindMeHelper.getInstance().getAuthCodeFromPref();
            
            String data = caller.fetchFriends(code);
            
            if(data.equals(FindMeHelper.FAILURE)){
                Log.e("com.example.findme", "Failed to Fetched Friends Details...");
            }else{
                Log.d("com.example.findme", "Exiting doInBackground <<");
                return data;
            }
            
        }else{
            
            String status = caller.authenticate();
            
            if(status.equals(FindMeHelper.FAILURE)){
                Log.e("com.example.findme", "Account authentication failed");
            }else{
                FindMeHelper.getInstance().saveAuthCodetoPref(status);
                FindMeHelper.getInstance().saveAccountDatatoPref(true);
            }
            
            
        }
        Log.d("com.example.findme", "Exiting doInBackground <<");
        return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        if(result != null && result.contains("friends")){
            FindMeHelper.getInstance().addMarkerToMap(result);
        }
    }

}
