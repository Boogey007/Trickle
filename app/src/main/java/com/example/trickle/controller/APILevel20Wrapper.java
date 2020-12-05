package com.example.trickle.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

@TargetApi(20)
abstract class APILevel20Wrapper {
    static boolean isScreenOn(final Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }
}
