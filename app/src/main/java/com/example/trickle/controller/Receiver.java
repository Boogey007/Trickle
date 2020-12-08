package com.example.trickle.controller;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import java.lang.reflect.Method;

public class Receiver extends BroadcastReceiver {

    private static NetworkInfo.State previousState = null;

    private static final int TIMER_SCREEN_OFF = 1;
    private static final int TIMER_NO_NETWORK = 2;
    static final int TIMER_ON_AT = 3;
    static final int TIMER_OFF_AT = 4;
    static final int TIMER_ON_EVERY = 5;

    static final int TIMEOUT_NO_NETWORK = 5;
    static final int TIMEOUT_SCREEN_OFF = 10;
    static final int ON_EVERY_TIME_MIN = 120;
    static final String ON_AT_TIME = "8:00";
    static final String OFF_AT_TIME = "22:00";
    boolean success;
    private void startTimer(final Context context, int id, int time) {
        String action = (id == TIMER_SCREEN_OFF) ? "SCREEN_OFF_TIMER" : "NO_NETWORK_TIMER";
        Intent timerIntent =
                new Intent(context, Receiver.class).putExtra("timer", id).setAction(action);
        if (PendingIntent.getBroadcast(context, id, timerIntent, PendingIntent.FLAG_NO_CREATE) ==
                null) {
            if (Build.VERSION.SDK_INT >= 19) {
                APILevel19Wrapper.setExactTimer(context, AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 60000 * time, PendingIntent
                                .getBroadcast(context, id, timerIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                        .set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000 * time,
                                PendingIntent.getBroadcast(context, id, timerIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
    }

    private boolean stopTimer(final Context context, int id) {
        Intent timerIntent = new Intent(context, Receiver.class).putExtra("timer", id)
                .setAction(id == TIMER_SCREEN_OFF ? "SCREEN_OFF_TIMER" : "NO_NETWORK_TIMER");
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, id, timerIntent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
            pendingIntent.cancel();
        }
        return pendingIntent != null;
    }

    @SuppressLint("InlinedApi")
    private static SharedPreferences getSharedPreferences(final Context context) {
        String prefFileName = context.getPackageName() + "_preferences";
        return context.getSharedPreferences(prefFileName, Context.MODE_MULTI_PROCESS);
    }

    @SuppressWarnings("deprecation")
    private static void changeWiFi(final Context context, boolean on) {
        SharedPreferences prefs = getSharedPreferences(context);
        if (on && prefs.getBoolean("airplane", true)) {
            try {
                if (android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                        APILevel17Wrapper.isAirplaneModeOn(context) : Settings.System
                        .getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON) ==
                        1) {
                    return;
                }
            } catch (final Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (on) {
            WifiManager wifi = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            try {
                Method m = wifi.getClass().getDeclaredMethod("isWifiApEnabled");
                if ((boolean) m.invoke(wifi)) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            WifiManager wm = ((WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE));
            if (wm.isWifiEnabled() != on) {
                boolean success = wm.setWifiEnabled(on);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Can not change WiFi state: " + e.getClass().getName(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        SharedPreferences prefs = getSharedPreferences(context);
        if (intent.hasExtra("timer")) {
            changeWiFi(context, false);
            stopTimer(context, intent.getIntExtra("timer", 0));
        } else if (intent.hasExtra("changeWiFi")) {
            changeWiFi(context, intent.getBooleanExtra("changeWiFi", false));
            Start.createTimers(context);
        } else {
            if (ScreenChangeReceiver.SCREEN_OFF_ACTION.equals(action)) {
                if (prefs.getBoolean("off_screen_off", true)) {
                    if (!prefs.getBoolean("ignore_screen_off", false)) {
                        startTimer(context, TIMER_SCREEN_OFF,
                                prefs.getInt("screen_off_timeout", TIMEOUT_SCREEN_OFF));
                    }
                }
            } else if (UnlockReceiver.USER_PRESENT_ACTION.equals(action) || ScreenChangeReceiver.SCREEN_ON_ACTION.equals(action)) {
                stopTimer(context, TIMER_SCREEN_OFF);
                if (prefs.getBoolean("on_unlock", true)) {
                    boolean noNetTimer = stopTimer(context, TIMER_NO_NETWORK);
                    if (((WifiManager) context.getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()) {
                        if (noNetTimer && prefs.getBoolean("off_no_network", true)) {
                            startTimer(context, TIMER_NO_NETWORK,
                                    prefs.getInt("no_network_timeout", TIMEOUT_NO_NETWORK));
                        }
                    } else {
                        changeWiFi(context, true);
                    }
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                final NetworkInfo nwi =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (nwi == null) return;
                if (nwi.isConnected()) {
                    if (!nwi.getState().equals(previousState)) {
                    }
                    stopTimer(context, TIMER_NO_NETWORK);
                } else if (nwi.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if (prefs.getBoolean("off_no_network", true)) {
                        startTimer(context, TIMER_NO_NETWORK,
                                prefs.getInt("no_network_timeout", TIMEOUT_NO_NETWORK));
                    }
                }
                previousState = nwi.getState();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED) {
                    if (prefs.getBoolean("off_no_network", true)) {
                        startTimer(context, TIMER_NO_NETWORK,
                                prefs.getInt("no_network_timeout", TIMEOUT_NO_NETWORK));
                    }
                    if (prefs.getBoolean("off_screen_off", true) &&
                            ((Build.VERSION.SDK_INT < 20 &&
                                    !((PowerManager) context.getApplicationContext()
                                            .getSystemService(Context.POWER_SERVICE))
                                            .isScreenOn()) || (Build.VERSION.SDK_INT >= 20 &&
                                    !APILevel20Wrapper.isScreenOn(context)))) {
                        startTimer(context, TIMER_SCREEN_OFF,
                                prefs.getInt("screen_off_timeout", TIMEOUT_SCREEN_OFF));
                    }
                } else if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_DISABLED) {
                    stopTimer(context, TIMER_SCREEN_OFF);
                    stopTimer(context, TIMER_NO_NETWORK);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo nwi2 = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pInfo winfo =
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 14)
                    if (nwi2.isConnected() ||
                            (Build.VERSION.SDK_INT >= 14 && APILevel14Wrapper.groupFormed(winfo))) {
                        if (!nwi2.getState().equals(previousState)) {
                        }
                        stopTimer(context, TIMER_NO_NETWORK);
                    } else if (nwi2.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        if (prefs.getBoolean("off_no_network", true)) {
                            startTimer(context, TIMER_NO_NETWORK,
                                    prefs.getInt("no_network_timeout", TIMEOUT_NO_NETWORK));
                        }
                    }
                previousState = nwi2.getState();
            } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                if (prefs.getBoolean("power_connected", false)) {
                    changeWiFi(context, true);
                    if (prefs.getBoolean("off_screen_off",
                            true)) {
                        stopTimer(context, TIMER_SCREEN_OFF);
                        prefs.edit().putBoolean("ignore_screen_off", true).apply();
                    }
                }
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                if (prefs.getBoolean("power_connected", false)) {

                    if (prefs.getBoolean("off_screen_off", true)) {
                        prefs.edit().putBoolean("ignore_screen_off", false).apply();
                        if ((Build.VERSION.SDK_INT < 20 &&
                                !((PowerManager) context.getApplicationContext()
                                        .getSystemService(Context.POWER_SERVICE))
                                        .isScreenOn()) || (Build.VERSION.SDK_INT >= 20 &&
                                !APILevel20Wrapper.isScreenOn(context))) {
                            startTimer(context, TIMER_SCREEN_OFF,
                                    prefs.getInt("screen_off_timeout", TIMEOUT_SCREEN_OFF));
                        }
                    }
                }
            }
        }
    }
}
