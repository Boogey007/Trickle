package com.example.trickle.controller.geo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class LocativeLocationManager {

    public static final int MY_PERMISSIONS_REQUEST = 1;

    private static final String TAG = "location";

    private Timer timer1;
    private LocationManager lm;
    private LocationResult locationResult;
    private boolean gps_on = false;
    private boolean networok_on = false;

    public boolean getLocation(Activity context, LocationResult result) {
        locationResult = result;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_on = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            networok_on = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_on && !networok_on)
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                context.requestPermissions(permissions, MY_PERMISSIONS_REQUEST);

                return false;
            } else {
                requestUpdates();
                return true;
            }
        } else {
            requestUpdates();
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestUpdates();
            }
        }
    }

    private void requestUpdates() {
        try {
            if (gps_on)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            if (networok_on)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            timer1 = new Timer();
            timer1.schedule(new GetLastLocation(), 25000);
        } catch (SecurityException e) { }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
            try {
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerNetwork);
            } catch (SecurityException e) { }
        }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Canceling location updater");
            timer1.cancel();
            locationResult.gotLocation(location);
            try {
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerGps);
            } catch (SecurityException e) {
                Log.e("location", e.getMessage(), e);
            }
        }

        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    private class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            try {
                lm.removeUpdates(locationListenerGps);
                lm.removeUpdates(locationListenerNetwork);

                Location net_loc = null, gps_loc = null;
                if (gps_on)
                    gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (networok_on)
                    net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gps_loc != null && net_loc != null) {
                    if (gps_loc.getTime() > net_loc.getTime())
                        locationResult.gotLocation(gps_loc);
                    else
                        locationResult.gotLocation(net_loc);
                    return;
                }

                if (gps_loc != null) {
                    locationResult.gotLocation(gps_loc);
                    return;
                }
                if (net_loc != null) {
                    locationResult.gotLocation(net_loc);
                    return;
                }
                locationResult.gotLocation(null);
            } catch (SecurityException e) {
                Log.e("location", e.getMessage(), e);
            }
        }
    }

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }
}
