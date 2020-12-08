package com.example.trickle.controller.model;

public enum EventType {

    ENTER("enter"),
    EXIT("exit");

    private final String mEvent;

    EventType(String apiName) { mEvent = apiName; }

}
