package com.example.trickle.controller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BatteryService extends Service {

    private int plugged;
    private int status;
    private int health;
    private int level;
    private int scale;
    private boolean present;
    private String technology;
    private int temperature;
    private int voltage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String tag = "Debug Info";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null)
                return;
            if (intent == null)
                return;

            Intent broadcast = new Intent();

            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
                plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                status = intent.getExtras().getInt(BatteryManager.EXTRA_STATUS, 0);
                health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

                broadcast.putExtra("present", present);
                broadcast.putExtra("plugged", plugged);
                broadcast.putExtra("status", status);
                broadcast.putExtra("health", health);
                broadcast.putExtra("technology", technology);
                broadcast.putExtra("scale", scale);
                broadcast.putExtra("level", level);
                broadcast.putExtra("temperature", temperature);
                broadcast.putExtra("voltage", voltage);
                broadcast.putExtra("plugged", plugged);
            }

            broadcast.setAction("com.example.trickle.controller.batteryservice");
            sendBroadcast(broadcast);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return super.onStartCommand(intent, flags, startId); }
}
