package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;



public class Geofencing implements ResultCallback{

    public static final long GEOFENCE_TIMEOUT= 24*60*60*1000;
    public static final int GEOFENCE_RADIUS= 50;
    public static final String TAG= Geofencing.class.getSimpleName();

    private Context context;
    private GoogleApiClient mClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    public Geofencing(Context context, GoogleApiClient mClient) {
        this.context = context;
        this.mClient = mClient;
        mGeofencePendingIntent= null;
        mGeofenceList= new ArrayList<>();
    }

    public void updateGeofencesList(PlaceBuffer places){
        if(places == null || places.getCount()==0)
            return;
        for(Place place: places){
            Geofence geofence= new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(place.getLatLng().latitude, place.getLatLng().longitude, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeofenceList.add(geofence);
        }
    }

    public void registerAllGeofences() {
        // Check that the API client is connected and that the list has Geofences in it
        if (mClient == null || !mClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void unregisterAllGeofences(){
        if(mClient == null && !mClient.isConnected()){
            return;
        }
        try{
            LocationServices.GeofencingApi.removeGeofences(mClient, getGeofencePendingIntent()).setResultCallback(this);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }


    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder= new GeofencingRequest.Builder()
                .addGeofences(mGeofenceList)
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER);
        GeofencingRequest geofencingRequest= builder.build();

        return geofencingRequest;
    }

    private PendingIntent getGeofencePendingIntent(){
        if(mGeofencePendingIntent != null)
            return mGeofencePendingIntent;
        PendingIntent mGeofencePendingIntent= PendingIntent.getBroadcast(context, 1, new Intent(context, GeofenceBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        return  mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence : %s",
                result.getStatus().toString()));
    }
}
