package com.example.trickle.controller.model;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.example.trickle.controller.persistent.GeofenceProvider;
import com.example.trickle.controller.utils.Constants;
import com.example.trickle.controller.utils.Preferences;

import static com.example.trickle.controller.TrickleBatteryManagerApplication.getAppContext;

public class Geofences {

    public static List<Geofence> GeofenceItems = new ArrayList<Geofence>();

    public static Map<String, Geofence> GeofenceItemsMap = new HashMap<String, Geofence>();

    public void clear() { GeofenceItems.clear(); }

    public void addItem(Geofence item) {
        GeofenceItems.add(item);
        GeofenceItemsMap.put(item.uuid, item);
    }

    public static class Geofence implements Serializable {
        public String uuid;
        public String customId;
        public String name;
        public int triggers;
        public double latitude;
        public double longitude;
        public int radiusMeters;
        public int enterMethod;
        public int exitMethod;
        public int currentlyEntered;

        public String getRelevantId() {
            if (customId != null && customId.length() > 0) {
               return customId;
            }
            if (name != null && name.length() > 0) {
                return name;
            }
            return uuid;
        }

        public Geofence(
                String uuid,
                String customId,
                String name,
                int triggers,
                double latitude,
                double longitude,
                int radiusMeters,
                int enterMethod,
                int exitMethod,
                int currentlyEntered
        ) {
            this.uuid = (uuid == null) ? UUID.randomUUID().toString() : uuid;
            this.customId = customId;
            this.name = name;
            this.triggers = triggers;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radiusMeters = radiusMeters;
            this.enterMethod = enterMethod;
            this.exitMethod = exitMethod;
            this.currentlyEntered = currentlyEntered;
        }

        @Override
        public String toString() {
            return getRelevantId();
        }

        public com.google.android.gms.location.Geofence toGeofence() {

            int transition = 0;
            boolean bothThere = (
                    ((triggers & GeofenceProvider.TRIGGER_ON_ENTER) == GeofenceProvider.TRIGGER_ON_ENTER) &&
                            ((triggers & GeofenceProvider.TRIGGER_ON_EXIT) == GeofenceProvider.TRIGGER_ON_EXIT)
            );
            if (bothThere) {
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL;
            } else if (((triggers & GeofenceProvider.TRIGGER_ON_ENTER) == GeofenceProvider.TRIGGER_ON_ENTER)) {
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL;
            } else if (((triggers & GeofenceProvider.TRIGGER_ON_EXIT) == GeofenceProvider.TRIGGER_ON_EXIT)) {
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL;
            }

            if (transition == 0)
                return null;

            if (radiusMeters == 0.0)
                return null;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getAppContext());

            return new com.google.android.gms.location.Geofence.Builder()
                    .setRequestId(this.customId)
                    .setTransitionTypes(transition)
                    .setCircularRegion(
                            this.latitude,
                            this.longitude,
                            this.radiusMeters)
                    .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(
                    preferences.getBoolean(Preferences.TRIGGER_THRESHOLD_ENABLED, false) ?
                            preferences.getInt(Preferences.TRIGGER_THRESHOLD_VALUE, Preferences.TRIGGER_THRESHOLD_VALUE_DEFAULT) :
                            0
                    ).build();
        }
    }
}
