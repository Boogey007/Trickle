package com.example.trickle.controller.controller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class GPSController implements LocationListener {
    Context mContext;
    private LocationManager locationManager;

    private static GPSController mInstance;

    protected GPSController(Context context) {
        this.mContext = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static GPSController getInstance(Context context) {
        if (mInstance == null)
            mInstance = new GPSController(context);

        return mInstance;
    }

    public boolean activeGPS() {
        boolean status = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return status;
    }

    public void switchGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mContext.startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) { }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override
    public void onProviderEnabled(String s) {}
    @Override
    public void onProviderDisabled(String s) { }
}
