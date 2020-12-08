package com.example.trickle.controller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

public class ControllerService extends Service {

    final String GPS_CHANGE_ACTION = "android.location.PROVIDERS_CHANGED";
    final String HOT_CHANGE_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    final int HOTSPOT_ENABLED = 13;
    final int HOTSPOT_DISABLED = 11;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String tag = "Debug Info";

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent broadcast_intent = new Intent();
            int wifiState;

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    broadcast_intent.putExtra("wifi_state", -1);
                } else if (wifiState == WifiManager.WIFI_STATE_DISABLING) {
                    broadcast_intent.putExtra("wifi_state", 0);
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    broadcast_intent.putExtra("wifi_state", 1);
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    broadcast_intent.putExtra("wifi_state", 0);
                } else {
                    broadcast_intent.putExtra("wifi_state", 0);
                }
            }
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (NetworkInfo.State.CONNECTED == mMobile.getState()) {
                    broadcast_intent.putExtra("data_state", 1);
                } else {
                    broadcast_intent.putExtra("data_state", -1);
                }
            }
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                int modeIdx = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                boolean isEnabled = (modeIdx == 1);
                if (isEnabled) {
                    broadcast_intent.putExtra("plane_state", 1);
                } else {
                    broadcast_intent.putExtra("plane_state", -1);
                }
            }

            if (intent.getAction().matches(GPS_CHANGE_ACTION)) {
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (statusOfGPS) {
                    broadcast_intent.putExtra("gps_state", 1);
                } else {
                    broadcast_intent.putExtra("gps_state", -1);
                }
            }

            if (intent.getAction().matches(HOT_CHANGE_ACTION)) {
                int state = intent.getIntExtra("wifi_state", 0);
                if (state == HOTSPOT_DISABLED) {
                    broadcast_intent.putExtra("hotspot_state", -1);
                } else if (state == HOTSPOT_ENABLED) {
                    broadcast_intent.putExtra("hotspot_state", 1);
                } else {
                    broadcast_intent.putExtra("hotspot_state", 0);
                }
            }
            broadcast_intent.setAction("com.example.trickle.controller.controllerservice");
            sendBroadcast(broadcast_intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mFilter.addAction(GPS_CHANGE_ACTION);
        mFilter.addAction(HOT_CHANGE_ACTION);
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
