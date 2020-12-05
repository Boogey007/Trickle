package com.example.trickle.controller.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.R;
import com.example.trickle.controller.utils.Preferences;
import com.example.trickle.controller.utils.ResourceUtils;
import com.example.trickle.controller.view.GeofencesActivity;
import com.google.android.gms.location.Geofence;

import javax.inject.Inject;

import static com.example.trickle.controller.TrickleBatteryManagerApplication.getApplication;


public class NotificationManager {

    @Inject
    SharedPreferences mPrefs;

    @Inject
    ResourceUtils mResourceUtils;

    private Context mContext;

    public NotificationManager(Context context) {
        ((TrickleBatteryManagerApplication) getApplication()).inject(this);
        mContext = context;
    }

}