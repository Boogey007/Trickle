package com.example.trickle.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UnlockReceiver extends BroadcastReceiver {

    public final static String USER_PRESENT_ACTION = "USER_PRESENT";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_USER_PRESENT.equals(action)) {
            context.sendBroadcast(
                    new Intent(context, Receiver.class).setAction(USER_PRESENT_ACTION));
        }
    }
}
