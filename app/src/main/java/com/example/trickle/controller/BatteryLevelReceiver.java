package com.example.trickle.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.trickle.controller.controller.AudioController;
import com.example.trickle.controller.controller.NetworkController;

import static com.example.trickle.controller.TrickleBatteryManagerApplication.getAppContext;
import static com.example.trickle.controller.TrickleBatteryManagerApplication.getApplication;
import static com.example.trickle.controller.geo.StartupBroadCastReceiver.TAG;


public class BatteryLevelReceiver extends BroadcastReceiver {

    boolean wifiT;
    boolean ringerT;

    NetworkController networkController = NetworkController.getInstance(getAppContext());
    AudioController audioController = AudioController.getInstance(getAppContext());

    SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(getAppContext());

    public BatteryLevelReceiver() {
        ((TrickleBatteryManagerApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        checkWiFi();
        checkRinger();
    }

    private void checkWiFi() {
        wifiT = preferences.getBoolean("wifiLowBatTriggerEnabled", false);

        if (wifiT)
            networkController.toggleWiFi(false);
    }

    private void checkRinger() {
        ringerT = preferences.getBoolean("silentLowBatTriggerEnabled", false);

        if (ringerT) {
            audioController.setPhoneRingerToSilent();
        }
    }
}