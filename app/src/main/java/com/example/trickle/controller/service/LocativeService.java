package com.example.trickle.controller.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.trickle.controller.R;
import com.example.trickle.controller.geo.GeofenceErrorMessages;
import com.example.trickle.controller.model.Geofences;
import com.example.trickle.controller.notification.NotificationManager;
import com.example.trickle.controller.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LocativeService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_REQUEST_IDS = "requestId";
    public static final String EXTRA_GEOFENCE = "geofence";
    public static final String EXTRA_ACTION = "action";
    private static final String TAG = "GEO";

    private final List<Geofence> mGeofenceListsToAdd = new ArrayList<Geofence>();
    private List<String> mGeofenceListsToRemove = new ArrayList<String>();

    protected GoogleApiClient mGoogleApiClient;

    private Action mAction;
    private PendingIntent mGeofencePendingIntent;

    public enum Action implements Serializable {
        ADD,
        REMOVE
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        mAction = (Action) intent.getSerializableExtra(EXTRA_ACTION);

        if (mAction == Action.ADD) {
            ArrayList<Geofences.Geofence> geofences = (ArrayList<Geofences.Geofence>) intent.getSerializableExtra(EXTRA_GEOFENCE);
            for (Iterator<Geofences.Geofence> iterator = geofences.iterator(); iterator.hasNext(); ) {
                Geofences.Geofence newGeofence = iterator.next();
                Geofence googleGeofence = newGeofence.toGeofence();
                if (googleGeofence != null) {
                    mGeofenceListsToAdd.add(googleGeofence);
                }
            }
        } else if (mAction == Action.REMOVE) {
            mGeofenceListsToRemove = Arrays.asList(intent.getStringArrayExtra(EXTRA_REQUEST_IDS));
        }
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mAction == Action.ADD) {
            if (mGeofenceListsToAdd.size() > 0) {
                GeofencingRequest request = getGeofencingRequest(mGeofenceListsToAdd);
            }
        } else if (mAction == Action.REMOVE) {
            if (mGeofenceListsToRemove.size() > 0) {
                PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceListsToRemove);
                result.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                        } else {
                            String errorMessage = GeofenceErrorMessages.getErrorString(LocativeService.this, status.getStatusCode());
                            Log.e(TAG, errorMessage);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) { }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection bad");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent == null) {
            Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
            mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;
    }

    private static GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(0);
        builder.addGeofences(geofenceList);
        return builder.build();
    }
}