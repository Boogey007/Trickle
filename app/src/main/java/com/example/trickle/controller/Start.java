package com.example.trickle.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class to set all necessary timers / start the background service
 */
abstract class Start {

    /**
     * Creates the ON_AT and OFF_AT timers
     *
     * @param c the contextl
     */
    static void createTimers(final Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (prefs.getBoolean("off_screen_off", true) || prefs.getBoolean("on_unlock", true)) {
            c.startService(new Intent(c, ScreenChangeReceiver.class));
        } else {
            c.stopService(new Intent(c, ScreenChangeReceiver.class));
        }

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);

        if (prefs.getBoolean("on_at", false)) {
            String[] time = prefs.getString("on_at_time", Receiver.ON_AT_TIME).split(":");

            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
            cal.set(Calendar.MINUTE, Integer.valueOf(time[1]));

            if (cal.getTimeInMillis() <= System.currentTimeMillis())
                cal.add(Calendar.DAY_OF_MONTH, 1);

            PendingIntent pi = PendingIntent.getBroadcast(c, Receiver.TIMER_ON_AT,
                    new Intent(c, Receiver.class).putExtra("changeWiFi", true).setAction("ON_AT"),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 19) {
                APILevel19Wrapper
                        .setExactTimer(c, AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }

        } else {
            // stop timer
            am.cancel(PendingIntent.getBroadcast(c, Receiver.TIMER_ON_AT,
                    new Intent(c, Receiver.class).putExtra("changeWiFi", true).setAction("ON_AT"),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        }
        if (prefs.getBoolean("off_at", false)) {
            String[] time = prefs.getString("off_at_time", Receiver.OFF_AT_TIME).split(":");

            cal = Calendar.getInstance();

            cal.set(Calendar.SECOND, 1);
            cal.set(Calendar.MILLISECOND, 0);

            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
            cal.set(Calendar.MINUTE, Integer.valueOf(time[1]));

            if (cal.getTimeInMillis() <= System.currentTimeMillis())
                cal.add(Calendar.DAY_OF_MONTH, 1);

            PendingIntent pi = PendingIntent.getBroadcast(c, Receiver.TIMER_OFF_AT,
                    new Intent(c, Receiver.class).putExtra("changeWiFi", false).setAction("OFF_AT"),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 19) {
                APILevel19Wrapper
                        .setExactTimer(c, AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }

        } else {
            // stop timer
            am.cancel(PendingIntent.getBroadcast(c, Receiver.TIMER_OFF_AT,
                    new Intent(c, Receiver.class).putExtra("changeWiFi", false).setAction("OFF_AT"),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        }

    }

    @SuppressWarnings("deprecation")
    static void start(final Context c) {
        createTimers(c);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        c.getPackageManager().setComponentEnabledSetting(new ComponentName(c, UnlockReceiver.class),
                prefs.getBoolean("on_unlock", true) ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
