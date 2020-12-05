package com.example.trickle.controller.model;

public class TimeInfo {

    private int day;
    private int minutes;

    public TimeInfo() { }

    public TimeInfo(int day, int minutes) {
        this.day = day;
        this.minutes = minutes;
    }

    public int getDayOfWeek() { return day; }
    public void setDayOfWeek(int day) { this.day = day; }

    public int getTimeInMins() { return minutes; }
    public void setTimeInMins(int minutes) { this.minutes = minutes; }

    @Override
    public String toString() {
        return "TimeInfo{" +
                "dayOfWeek=" + day +
                ", timeInMins=" + minutes +
                '}';
    }
}
