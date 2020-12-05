package com.example.trickle.controller.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.R;
import com.example.trickle.controller.utils.Preferences;

import butterknife.BindView;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.notification_success_switch)
    Switch mNSuccessSwitch;
    @BindView(R.id.notification_fail_switch)
    Switch mNFailSwitch;
    @BindView(R.id.notification_only_latest_switch)
    Switch mNLatestSwitch;
    @BindView(R.id.notification_sound_switch)
    Switch mNSoundSwitch;

    @BindView(R.id.trigger_threshold_enabled_switch)
    Switch mTTEnabled;
    @BindView(R.id.trigger_threshold_seekbar)
    SeekBar mTriggerThresholdSeekBar;
    @BindView(R.id.trigger_threshold_notice)
    TextView mTriggerThresholdNotice;


    private void updateThresholdNotice() {
        if (mTTEnabled.isChecked()) {
            mTriggerThresholdNotice.setText("Trigger Threshold Updated " + mTriggerThresholdSeekBar.getProgress());
            return;
        }
        mTriggerThresholdNotice.setText("Trigger Threshold disabled");
    }

    private void setupThreshold() {
        mTTEnabled.setChecked(mPrefs.getBoolean(Preferences.TRIGGER_THRESHOLD_ENABLED, false));
        mTriggerThresholdSeekBar.setEnabled(mTTEnabled.isChecked());
        mTriggerThresholdSeekBar.setProgress(mPrefs.getInt(Preferences.TRIGGER_THRESHOLD_VALUE, Preferences.TRIGGER_THRESHOLD_VALUE_DEFAULT) / 1000);
        mTriggerThresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { updateThresholdNotice(); }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        mTTEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mTriggerThresholdSeekBar.setEnabled(b);
                updateThresholdNotice();
            }
        });
        updateThresholdNotice();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TrickleBatteryManagerApplication) getApplication()).getComponent().inject(this);

        mNSuccessSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SUCCESS, false));
        mNFailSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_FAIL, false));
        mNLatestSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, false));
        mNSoundSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SOUND, false));

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2541b2")));
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                save(true);
                return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupThreshold();
    }

    @Override
    public void onResume() { super.onResume(); }


    @Override
    protected int getLayoutResourceId() { return R.layout.activity_settings; }

    @Override
    protected String getToolbarTitle() { return "Settings"; }

    @Override
    protected int getMenuResourceId() { return R.menu.settings; }

    private void save(boolean finish) {
        SharedPreferences.Editor preferenceEditor = mPrefs.edit();
        preferenceEditor.putBoolean(Preferences.NOTIFICATION_SUCCESS, mNSuccessSwitch.isChecked());
        preferenceEditor.putBoolean(Preferences.NOTIFICATION_FAIL, mNFailSwitch.isChecked());
        preferenceEditor.putBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, mNLatestSwitch.isChecked());
        preferenceEditor.putBoolean(Preferences.NOTIFICATION_SOUND, mNSoundSwitch.isChecked());
        preferenceEditor.putBoolean(Preferences.TRIGGER_THRESHOLD_ENABLED, mTTEnabled.isChecked());
        preferenceEditor.putInt(Preferences.TRIGGER_THRESHOLD_VALUE, mTriggerThresholdSeekBar.getProgress() * 1000);
        preferenceEditor.apply();

        if (finish)
            finish();
    }
}
