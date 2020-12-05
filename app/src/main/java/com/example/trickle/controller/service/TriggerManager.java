package com.example.trickle.controller.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.controller.AudioController;
import com.example.trickle.controller.controller.NetworkController;
import com.example.trickle.controller.model.EventType;
import com.example.trickle.controller.model.Geofences;
import com.example.trickle.controller.notification.NotificationManager;
import com.example.trickle.controller.utils.Constants;
import com.example.trickle.controller.utils.Preferences;
import com.google.android.gms.location.Geofence;

import java.util.Random;

import javax.inject.Inject;

import static com.example.trickle.controller.TrickleBatteryManagerApplication.getAppContext;
import static com.example.trickle.controller.TrickleBatteryManagerApplication.getApplication;

public class TriggerManager {

    @Inject
    SharedPreferences mPreferences;

    @Inject
    NotificationManager mNotificationManager;

    private boolean wifiGeofenceToggle = false;
    private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getAppContext());

    NetworkController networkController = NetworkController.getInstance(getAppContext());
    AudioController audioController = AudioController.getInstance(getAppContext());

    public TriggerManager() {
        ((TrickleBatteryManagerApplication) getApplication()).inject(this);
    }

    public void triggerTransition(Geofences.Geofence fence, int transitionType) {
        String additionalNotification = "";
        wifiGeofenceToggle = preferences.getBoolean("wifiGeofenceToggleEnabled", false);
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            if (wifiGeofenceToggle) {
                networkController.toggleWiFi(true);
                additionalNotification += " , wifi turned on";
            }
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            if (wifiGeofenceToggle) {
                networkController.toggleWiFi(false);
                additionalNotification += ", wifi turned off";
            }
        }
    }

    @Nullable
    private EventType getEventType(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return EventType.ENTER;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return EventType.EXIT;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return EventType.ENTER;
            default:
                return null;
        }

    }
}
