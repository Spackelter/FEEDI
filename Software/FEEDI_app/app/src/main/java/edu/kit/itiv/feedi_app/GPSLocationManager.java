package edu.kit.itiv.feedi_app;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static android.content.Context.LOCATION_SERVICE;

public class GPSLocationManager {

    //"global" instance of the class
    private static GPSLocationManager instance = null;

    //Context
    private Context myContext;

    //calling activity
    private Activity myCallingActivity;

    //Elements to update the location
    private LocationManager myLocationManager;
    private LocationListener myLocationListener;
    private boolean gPSLocationFound = false;
    private double currentPosLatitude = 0;
    private double currentPosLongitude = 0;

    //default constructor
    private GPSLocationManager(Context context, Activity callingActivity){

        myContext = context;
        myCallingActivity = callingActivity;

        myLocationManager = (LocationManager) myContext.getSystemService(LOCATION_SERVICE);
        myLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double newLat = location.getLatitude();
                double newLong = location.getLongitude();

                if(newLat==0 && newLong == 0){
                    gPSLocationFound = false;
                    Intent notifyGpsPositionLost = new Intent();
                    notifyGpsPositionLost.setAction(Const.INTENT_ACTION_NOTIFY_GPS_POSITION_LOST);
                    myContext.sendBroadcast(notifyGpsPositionLost);
                    return;
                }

                currentPosLatitude = newLat;
                currentPosLongitude = newLong;
                gPSLocationFound = true;

                Intent notifyNewGpsPosition = new Intent();
                notifyNewGpsPosition.setAction(Const.INTENT_ACTION_NOTIFY_NEW_GPS_POSITION);
                myContext.sendBroadcast(notifyNewGpsPosition);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        //REMARK: MAYBE MODIFIY THE MIN TIME AND MIN DISTANCE TO NOT MAKE THE PROGRAM CHECK TOO OFTEN EITHER
        if (ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(callingActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {

            //minTime in milliseconds, minDistance in meters
            myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, myLocationListener);

        }
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, myLocationListener);

    }




    //To access a "global" instance of BleManager from anywhere
    public static GPSLocationManager getInstance(Context context, Activity callingActivity) {

        if (instance == null) {
            instance = new GPSLocationManager(context, callingActivity);
        }
        return instance;

    }

    //getters & setters
    public boolean getGpsLocationFound(){
        return gPSLocationFound;
    }

    public double getCurrentPosLatitude(){
        return currentPosLatitude;
    }

    public double getCurrentPosLongitude(){
        return currentPosLongitude;
    }

}
