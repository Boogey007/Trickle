package com.example.trickle.controller.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    private SwitchCompat autoBrightnessSwitch;
    private Slider brightnessSlider;

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
        brightnessSlider = (Slider) activity.findViewById(R.id.brightnessSlider);
        autoBrightnessSwitch = (SwitchCompat) activity.findViewById(R.id.autoBrightnessSwitch);
        initSlider(brightnessSlider);

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
            autoBrightnessSwitch.setChecked(true);
            brightnessSlider.setValue(0);
        } else {
            autoBrightnessSwitch.setChecked(false);
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
            brightnessSlider.setEnabled(false);
            autoBrightnessSwitch.setEnabled(false);
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

    /**
     * Shows the modify system settings panel to allow the user to add WRITE_SETTINGS permissions for this app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void askWritePermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * Get current system brightness value as an integer
     */
    public int getCurrentBrightness() {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
    }

    public void setCurrentBrightness(int screenBrightnessValue) {

        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                screenBrightnessValue);

    }

    /**
     * Turn automatic brightness mode on - set manual mode off
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void setBrightnessToAuto() {
        boolean canWriteSettings = Settings.System.canWrite(context);

        if (!canWriteSettings) {

        } else {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            brightnessSlider.setValue(0);
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
            brightnessSlider.setValue(getCurrentBrightness());
        }
    }

    public void showRootWorkaroundInstructions(final Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this.context);
        builder.setMessage("Since your phone is not rooted, you must manually grant the permission " +
                "'android.permission.WRITE_SECURE_SETTINGS' by going to adb and inputting the following command" +
                " adb -d shell pm grant com.example.trickle.controller android.permission.WRITE_SECURE_SETTINGS")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Copy the command", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setClipboard(context, "adb -d shell pm grant com.example.trickle.controller android.permission.WRITE_SECURE_SETTINGS");
                        Toast.makeText(context, "Command copied", Toast.LENGTH_SHORT)
                                .show();
                        dialog.cancel();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * @param context
     * @param text    - Copy the text passed in the parameters onto the clipboard
     */
    private void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void autoBrightnessToggle() {
        boolean canWriteSettings = Settings.System.canWrite(context);

        if (!canWriteSettings) {

        } else {
            if (checkIfAutoBrightness()) {
                autoBrightnessSwitch.setChecked(true);
                brightnessSlider.setValue(0);
            } else {
                autoBrightnessSwitch.setChecked(false);
                brightnessSlider.setValue(getCurrentBrightness());
            }
        }
    }

    public void enableBrightnessSettings() {
        brightnessSlider.setEnabled(true);
        autoBrightnessSwitch.setEnabled(true);
    }

    /**
     * BrightnessObserver: Handle the change in brightness in real time and change the Slider value
     */
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

            if (!canWriteSettings) {
                brightnessSlider.setEnabled(false);
            } else {
                brightnessSlider.setEnabled(true);
                autoBrightnessToggle();
                brightnessSlider.setValue(getCurrentBrightness());
            }
        }
    }


    /**
     * AutoBrightnessObserver: Handle the change in brightness in real time and change the Slider Value
     */
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

            if (!canWriteSettings) {
                brightnessSlider.setEnabled(false);
            } else {
                brightnessSlider.setEnabled(true);
                autoBrightnessToggle();
            }
        }
    }
}