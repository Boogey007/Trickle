package com.example.trickle.controller.model;

import java.io.Serializable;

public class CustomProfile implements Serializable {
    private ContextInfo contextInfo;

    public class Settings {
        public boolean wifiOn;
        public boolean mobileDataOn;
        public boolean brightnessAuto;
        public int brightnessLevel;
    }
}
