package com.example.trickle.controller;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.trickle.controller.utils.Preferences;
import com.example.trickle.controller.view.GeofencesActivity;
import com.example.trickle.controller.view.SettingsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.trickle.controller.TrickleBatteryManagerApplication.getAppContext;

public class GeofencingFragment extends Fragment {

    private Context mContext;
    private Unbinder unbinder;

    @BindView(R.id.geofencingText)
    TextView geofencingText;
    @BindView(R.id.geofencingSettingsText)
    TextView geofencingSettings;

    @BindView(R.id.wifiHomeToggleSwitch)
    SwitchCompat wifiHomeToggleSwitch;

    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_geofencingsection, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        wifiHomeToggleSwitch.setChecked(prefs.getBoolean(Preferences.WIFI_TOGGLE_GEOFENCE, false));
    }

    @OnClick({R.id.geofencingText})
    public void geofencingLaunch() {
        Intent i = new Intent(mContext, GeofencesActivity.class);
        startActivity(i);
    }

    @OnClick({R.id.geofencingSettingsText})
    public void geofencingSettingLaunch() {
        Intent i = new Intent(mContext, SettingsActivity.class);
        startActivity(i);
    }

    @OnCheckedChanged({R.id.wifiHomeToggleSwitch})
    public void wifiToggleGeofenceTrigger(boolean checked) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Preferences.WIFI_TOGGLE_GEOFENCE, checked);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle("Geofencing");
        actionBar.setBackgroundDrawable( new ColorDrawable(Color.parseColor("#2541b2")));
    }
}
