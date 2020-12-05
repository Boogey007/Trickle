package com.example.trickle.controller.model;

public class BatteryInfo {

    private int batteryLevel;
    private int batteryStatus;

    public BatteryInfo() { }

    public int getBattLevel() { return batteryLevel; }
    public void setBattLevel(int batteryLevel) {  this.batteryLevel = batteryLevel; }

    public int getBattStatus() { return batteryStatus; }
    public void setBattStatus(int batteryStatus) { this.batteryStatus = batteryStatus; }

    @Override
    public String toString() {
        return "BatteryInfo{" +
                "battLevel=" + batteryLevel +
                ", battStatus=" + batteryStatus +
                '}';
    }
}
