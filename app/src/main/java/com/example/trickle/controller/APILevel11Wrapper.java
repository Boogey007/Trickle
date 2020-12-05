package com.example.trickle.controller;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.widget.NumberPicker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
abstract class APILevel11Wrapper {

    static void showNumberPicker(final Context c, final SharedPreferences prefs, final Preference p, final int summary, final int min, final int max, final String title, final String setting, final int def, final boolean changeTitle) {
        if (c == null) return;
        final NumberPicker np = new NumberPicker(c);
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(prefs.getInt(setting, def));
        new AlertDialog.Builder(c).setTitle(title).setView(np)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        np.clearFocus();
                        prefs.edit().putInt(setting, np.getValue()).apply();
                        if (changeTitle) p.setTitle(c.getString(summary, np.getValue()));
                        else p.setSummary(c.getString(summary, np.getValue()));
                    }
                }).create().show();
    }

}
