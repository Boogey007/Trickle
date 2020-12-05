package com.example.trickle.controller.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.provider.Settings;

public class NetworkController {
    private Context context;
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;

    private final int HOTSPOT_ENABLED = 13;

    private static NetworkController mInstance;

    protected NetworkController(Context context) {

        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static NetworkController getInstance(Context context) {
        if (mInstance == null)
            mInstance = new NetworkController(context);

        return mInstance;
    }

    public boolean isWifiConnected() {
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi != null)
            return wifi.isConnected();

        return false;
    }

    public void toggleWiFi(boolean value) {
        toggleWiFiTask task = new toggleWiFiTask();
        task.execute(value);
    }

    public class toggleWiFiTask extends AsyncTask<Boolean, Void, Boolean> {
        WifiManager wm;

        @Override
        protected Boolean doInBackground(Boolean... params) {
            wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return params[0];
        }

        @Override
        protected void onPostExecute(Boolean result) { wm.setWifiEnabled(result); }
    }

    public boolean isMobileNetworkConnected() {
        NetworkInfo mNI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mNI != null) {
            return mNI.isConnected();
        }
        return false;
    }

    public boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public void wifiSwitchSettings() {
        Intent wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
        context.startActivity(wifi);
    }

    public void hsTogglePermission() {
        Intent hs = new Intent();
        hs.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        context.startActivity(hs);
    }

    public void flightModeTogglePermission() {
        Intent apModeToggle = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        context.startActivity(apModeToggle);
    }

    public class togglePlaneTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean enabled = isAirplaneModeOn(context);
            return !enabled;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, result ? 1 : 0);
            Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            i.putExtra("state", result);
            context.sendBroadcast(i);
        }
    }
}
