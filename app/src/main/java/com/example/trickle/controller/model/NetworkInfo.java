package com.example.trickle.controller.model;

public class NetworkInfo {

    private int wifi;
    private int mobileData;
    private int gps;

    public NetworkInfo() { }

    public int getWifiState() { return wifi; }
    public void setWifiState(int wifi) { this.wifi = wifi; }

    public int getMobileDataState() { return mobileData; }
    public void setMobileDataState(int mobileData) { this.mobileData = mobileData; }

    public int getGpsState() { return gps; }
    public void setGpsState(int gps) { this.gps = gps; }

    @Override
    public String toString() {
        return "NetworkInfo{" +
                "wifiState=" + wifi +
                ", mobileDataState=" + mobileData +
                ", gpsState=" + gps +
                '}';
    }
}
