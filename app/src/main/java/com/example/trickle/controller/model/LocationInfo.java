package com.example.trickle.controller.model;

public class LocationInfo {

    private int typeOfPlace;
    private double lon;
    private double lat;
    private double acc;

    public LocationInfo() { }

    public int getPlaceType() { return typeOfPlace; }
    public void setPlaceType(int typeOfPlace) { this.typeOfPlace = typeOfPlace; }

    public double getLongitude() { return lon; }
    public void setLongitude(double lon) { this.lon = lon; }

    public double getLatitude() { return lat; }
    public void setLatitude(double lat) { this.lat = lat; }

    public double getAccuracy() { return acc; }
    public void setAccuracy(double acc) { this.acc = acc; }

    @Override
    public String toString() {
        return "LocationInfo{" +
                "placeType=" + typeOfPlace +
                ", longitude=" + lon +
                ", latitude=" + lat +
                ", accuracy=" + acc +
                '}';
    }
}
