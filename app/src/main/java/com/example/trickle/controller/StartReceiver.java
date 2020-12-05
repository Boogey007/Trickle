package com.example.trickle.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*if (BuildConfig.DEBUG) Logger.log("received: " + intent.getAction());*/
        if (!Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction()) ||
                intent.getDataString().contains(context.getPackageName())) {
            Start.start(context);
        /*    context.startService(new Intent(context, LogDeleteService.class));*/
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // AC already connected on boot?
            Intent batteryIntent =
                    context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            if (plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                    plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                context.sendBroadcast(new Intent(context, Receiver.class)
                        .setAction(Intent.ACTION_POWER_CONNECTED));
            }
        }
    }
}
