package com.example.trickle.controller.model;

public class ContextInfo {

    private TimeInfo time;
    private LocationInfo location;

    public ContextInfo() { }

    public LocationInfo getLocation() { return location; }

    public void setLocation(LocationInfo location) { this.location = location; }

    @Override
    public String toString() {
        return "ContextInfo{" +
                "time=" + time +
                ", location=" + location +
                '}';
    }
}
