package com.example.trickle.controller.geo;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;

public class GeofenceErrorMessages {

    private GeofenceErrorMessages() {
    }
    // All came from here https://stackoverflow.com/questions/58660913/can-anyone-explain-me-what-happened-with-geofenceerrormessages-class
    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown geofence error";
        }
    }
}