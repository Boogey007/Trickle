package com.example.trickle.controller;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

@TargetApi(19)
abstract class APILevel19Wrapper {
    static void setExactTimer(final Context context, int type, long time, PendingIntent pi) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setExact(type, time, pi);
    }
}
