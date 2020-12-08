package com.example.trickle.controller.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.SwitchCompat;
import android.widget.Toast;
import com.example.trickle.controller.R;
import com.gc.materialdesign.views.Slider;

public class DisplayController {

    private Context context;
    private Activity activity;
    public static final String ACCESSIBILITY_DISPLAY_DALTONIZER = "accessibility_display_daltonizer";
    public static final String ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled";
    public static final int MAX_BRIGHTNESS_VALUE = 255;
    public static final int MIN_BRIGHTNESS_VALUE = 0;

    private BrightnessObserver brightnessObserver = null;
    private AutoBrightnessObserver autobrightnessObserver = null;

    private SwitchCompat brightnessAuto;
    private Slider diplayBrightSlider;

    private static DisplayController mInstance;

    protected DisplayController(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public static DisplayController getInstance(Context context, Activity activity) {
        if (mInstance == null)
            mInstance = new DisplayController(context, activity);

        return mInstance;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void init() {
        diplayBrightSlider = (Slider) activity.findViewById(R.id.brightnessSlider);
        brightnessAuto = (SwitchCompat) activity.findViewById(R.id.autoBrightnessSwitch);
        initSlider(diplayBrightSlider);

        final Uri BRIGHTNESS_URL = Settings.System.getUriFor(android.provider.Settings.System.SCREEN_BRIGHTNESS);
        brightnessObserver = new BrightnessObserver(new Handler());
        context.getContentResolver()
                .registerContentObserver(BRIGHTNESS_URL, true, brightnessObserver);

        final Uri AUTOBRIGHTNESS_URL = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
        autobrightnessObserver = new AutoBrightnessObserver(new Handler());
        context.getContentResolver()
                .registerContentObserver(AUTOBRIGHTNESS_URL, true, autobrightnessObserver);

        int value = Settings.Secure.getInt(context.getContentResolver(), ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0);

        if (checkIfAutoBrightness()) {
            brightnessAuto.setChecked(true);
            diplayBrightSlider.setValue(0);
        } else {
            brightnessAuto.setChecked(false);
        }
    }

    public boolean checkIfAutoBrightness() {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initSlider(Slider slider) {
        slider.setValue(0);
        slider.setMin(MIN_BRIGHTNESS_VALUE);
        slider.setMax(MAX_BRIGHTNESS_VALUE);
        slider.setShowNumberIndicator(false);

        boolean canWriteSettings = Settings.System.canWrite(context);

        if (!canWriteSettings) {
            diplayBrightSlider.setEnabled(false);
            brightnessAuto.setEnabled(false);
            Toast.makeText(context, "Please Enable Write Permissions", Toast.LENGTH_SHORT).show();
            askWritePermissions();
        }
        int screenBrightness = getCurrentBrightness();
        slider.setValue(screenBrightness);
        slider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int progress) {
                setBrightnessToManual();
                setCurrentBrightness(progress);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void askWritePermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        context.startActivity(intent);
    }

    public int getCurrentBrightness() {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
    }

    public void setCurrentBrightness(int screenBrightnessValue) {

        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                screenBrightnessValue);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setBrightnessToAuto() {
        boolean canWriteSettings = Settings.System.canWrite(context);

        if (!canWriteSettings) {

        } else {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            diplayBrightSlider.setValue(0);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setBrightnessToManual() {
        boolean canWriteSettings = Settings.System.canWrite(context);

        if (!canWriteSettings) {
            // then nah
        } else {
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            diplayBrightSlider.setValue(getCurrentBrightness());
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void autoBrightnessToggle() {
        boolean canWriteSettings = Settings.System.canWrite(context);

        if (!canWriteSettings) {

        } else {
            if (checkIfAutoBrightness()) {
                brightnessAuto.setChecked(true);
                diplayBrightSlider.setValue(0);
            } else {
                brightnessAuto.setChecked(false);
                diplayBrightSlider.setValue(getCurrentBrightness());
            }
        }
    }

    public void enableBrightnessSettings() {
        diplayBrightSlider.setEnabled(true);
        brightnessAuto.setEnabled(true);
    }

    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean canWriteSettings = Settings.System.canWrite(context);

            if (!canWriteSettings) diplayBrightSlider.setEnabled(false);
            else {
                diplayBrightSlider.setEnabled(true);
                autoBrightnessToggle();
                diplayBrightSlider.setValue(getCurrentBrightness());
            }
        }
    }

    private class AutoBrightnessObserver extends ContentObserver {
        AutoBrightnessObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            boolean canWriteSettings = Settings.System.canWrite(context);

            if (!canWriteSettings) diplayBrightSlider.setEnabled(false);
            else {
                diplayBrightSlider.setEnabled(true);
                autoBrightnessToggle();
            }
        }
    }
}
