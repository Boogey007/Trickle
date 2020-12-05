package com.example.trickle.controller;

import android.annotation.TargetApi;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class APILevel14Wrapper {
    public static boolean groupFormed(final WifiP2pInfo info) {
        return info.groupFormed;
    }
}
