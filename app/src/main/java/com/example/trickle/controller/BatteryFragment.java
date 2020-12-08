package com.example.trickle.controller;


import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.trickle.controller.controller.AudioController;
import com.example.trickle.controller.utils.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.trickle.controller.TrickleBatteryManagerApplication.getAppContext;
import static com.example.trickle.controller.utils.Constants.SILENT_MODE;

public class BatteryFragment extends Fragment {
    @BindView(R.id.tempOfBattery_txt2)
    TextView textViewbatteryTemp;

    @BindView(R.id.healthOfBattery_txt2)
    TextView healthOfBattery;

    @BindView(R.id.techOfBattery_txt2)
    TextView batteryTech;

    @BindView(R.id.voltageOfBattery_txt2)
    TextView batteryVoltage;

    @BindView(R.id.pluggedStatusOfBattery_txt2)
    TextView batteryPlugged;

    @BindView(R.id.progressBarOfBattery)
    ProgressBar progressBarOfBattery;

    @BindView(R.id.currentBatteryValue)
    TextView currentBatteryValue;

    @BindView(R.id.batteryMuteOnLowText)
    TextView lowBatMute;

    @BindView(R.id.batteryMuteOnLowSwitch)
    SwitchCompat batteryMuteOnLowSwitch;

    @BindView(R.id.lowWifiTrigSwitch)
    SwitchCompat wifiSwitchOnLowBattery;

    @BindView(R.id.nightModeText)
    TextView nightModeText;


    private Context mContext;
    private Unbinder unbinder;
    private AudioController audioController = null;

    IntentFilter intentFilter = null;
    BroadcastReceiver mReceiver;
    BatteryService batteryService;
    Bundle extras = null;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        View view = inflater.inflate(R.layout.fragment_battery, container, false);
        unbinder = ButterKnife.bind(this, view);
        batteryService = new BatteryService();
        audioController = AudioController.getInstance(mContext);
        preferences = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        wifiSwitchOnLowBattery.setChecked(preferences.getBoolean(Preferences.WIFI_LOW_BAT_TRIGGER, false));
        batteryMuteOnLowSwitch.setChecked(preferences.getBoolean(Preferences.SILENT_LOW_BAT_TRIGGER, false));
    }
    // --- All Battery Stats --- //
    // ALL BATTERY CONSTANTS
    // BATTERY_HEALTH_COLD
    // BATTERY_HEALTH_DEAD
    // BATTERY_HEALTH_GOOD
    // BATTERY_HEALTH_OVERHEAT
    // BATTERY_HEALTH_OVER_VOLTAGE
    // BATTERY_HEALTH_UNKNOWN
    // BATTERY_HEALTH_UNSPECIFIED_FAILURE
    // BATTERY_PLUGGED_AC
    // BATTERY_PLUGGED_USB
    // BATTERY_PLUGGED_WIRELESS
    // BATTERY_PROPERTY_CAPACITY
    // BATTERY_PROPERTY_CHARGE_COUNTER
    // BATTERY_PROPERTY_CURRENT_AVERAGE
    // BATTERY_PROPERTY_CURRENT_NOW
    // BATTERY_PROPERTY_ENERGY_COUNTER
    // BATTERY_PROPERTY_STATUS
    // BATTERY_PROPERTY_STATUS
    // BATTERY_STATUS_CHARGING
    // BATTERY_STATUS_DISCHARGING
    // BATTERY_STATUS_FULL
    // BATTERY_STATUS_NOT_CHARGING
    // BATTERY_STATUS_UNKNOWN
    // EXTRA_BATTERY_LOW

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initBatteryItems();
        if (extras != null) {
            String health = getHealth(extras.getInt("health"));
            int level = extras.getInt("level");
            String technology = extras.getString("technology");
            int temperature = extras.getInt("temperature");
            int voltage = extras.getInt("voltage");
            float batteryPercentage = extras.getFloat("batteryPercentage");
            int plugged = extras.getInt("plugged");

            batteryTech.setText(technology);

            progressBarOfBattery.setProgress((int) (batteryPercentage * 100));
            currentBatteryValue.setText("" + (int) (batteryPercentage * 100));

            healthOfBattery.setTextColor(health.equals("Good") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
            healthOfBattery.setText(health);

            Float voltageOfBattery = (float) (voltage) / 1000;
            batteryVoltage.setText("" + voltageOfBattery + " V");

            Float tempOfBattery = (float) (temperature) / 10;
            if (tempOfBattery > 45) {
                textViewbatteryTemp.setTextColor(getResources().getColor(R.color.red));
            } else if (tempOfBattery <= 45 && tempOfBattery > 35) {
                textViewbatteryTemp.setTextColor(getResources().getColor(R.color.yellow));
            } else {
                textViewbatteryTemp.setTextColor(getResources().getColor(R.color.green));
            }
            textViewbatteryTemp.setText("" + tempOfBattery + " °C");

            if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
                batteryPlugged.setText("AC");
                batteryPlugged.setTextColor(getResources().getColor(R.color.green));
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                batteryPlugged.setText("USB");
                batteryPlugged.setTextColor(getResources().getColor(R.color.green));
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                batteryPlugged.setText("Wireless");
                batteryPlugged.setTextColor(getResources().getColor(R.color.green));
            } else {
                batteryPlugged.setText("Not Plugged");
                batteryPlugged.setTextColor(Color.LTGRAY);
            }
        }
    }

    public String getHealth(int health) {
        String batStat = "";

        if( health == BatteryManager.BATTERY_HEALTH_COLD )
            batStat = "Cold";

        else if( health == BatteryManager.BATTERY_HEALTH_DEAD )
            batStat = "Dead";

        else if( health == BatteryManager.BATTERY_HEALTH_GOOD )
            batStat = "Good";

        else if( health == BatteryManager.BATTERY_HEALTH_OVERHEAT )
            batStat = "Overheat";

        else if( health == BatteryManager.BATTERY_HEALTH_UNKNOWN )
            batStat = "Unknown";

        else if( health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE )
            batStat = "Over Voltage";

        else if( health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE )
            batStat = "Unspecified Failure";
        else
            batStat = "Error";

        return batStat;
    }

    public void initBatteryItems() {
        progressBarOfBattery.setMax(100);
        Intent i = new Intent(this.getActivity(), batteryService.getClass());
        getActivity().startService(i);

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.trickle.controller.batteryservice");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                extras = intent.getExtras();

                if (extras != null) {
                    int pl_r, st_r, he_r, lvl_r, sc_r, temp_r, volt_r;
                    boolean p_r;
                    String tech_r;

                    p_r = extras.getBoolean("present");
                    pl_r = extras.getInt("plugged");
                    st_r = extras.getInt("status");
                    pl_r = extras.getInt("plugged");
                    he_r = extras.getInt("health");
                    sc_r = extras.getInt("scale");
                    lvl_r = extras.getInt("level");
                    tech_r = extras.getString("technology");
                    temp_r = extras.getInt("temperature");
                    volt_r = extras.getInt("voltage");

                    extras.putFloat("batteryPercentage", lvl_r / (float) sc_r);
                    editBatteryItems edit = new editBatteryItems();
                    editBatteryStringItems editTechItem = new editBatteryStringItems();

                    edit.execute(pl_r, he_r, sc_r, lvl_r, temp_r, volt_r, pl_r);
                    editTechItem.execute(tech_r);
                }
            }
        };
        getAppContext().registerReceiver(mReceiver, intentFilter);
    }

    public double getBatteryCapacity() {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            return batteryCapacity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            try {
                getActivity().unregisterReceiver(mReceiver);
                mReceiver = null;
            } catch (IllegalArgumentException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        super.onDestroy();
    }

    public class editBatteryStringItems extends AsyncTask<String, String, Boolean> {
        String technology;

        @Override
        protected Boolean doInBackground(final String... params) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        technology = params[0];
                    }
                });
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            batteryTech.setText(technology);
        }

    }

    @OnCheckedChanged(R.id.lowWifiTrigSwitch)
    public void wifiLowBatTrigger(boolean checked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Preferences.WIFI_LOW_BAT_TRIGGER, checked);
        editor.apply();
    }

    @OnCheckedChanged(R.id.batteryMuteOnLowSwitch)
    public void muteLowBatTrigger(boolean checked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Preferences.SILENT_LOW_BAT_TRIGGER, checked);
        editor.apply();
    }

    @OnClick({R.id.nightModeText})
    public void nightMode() {
        Intent i = new Intent(mContext, com.example.trickle.controller.Preferences.class);
        startActivity(i);
    }

    @OnClick({R.id.batteryMuteOnLowText})
    public void setPhoneToSilent(View view) {
        audioController.muteThePhone(SILENT_MODE);
    }

    public int getCharge() {
        BatteryManager batteryManager = (BatteryManager) getActivity().getSystemService(Context.BATTERY_SERVICE);
        int energy = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            energy = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        }
        return energy;
    }

    private void showPowerUsageSummary() {
        try {
            Intent powerUsageSummary = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            Context context = getContext();
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(powerUsageSummary, 0);
            if (resolveInfo != null) {
                startActivity(powerUsageSummary);
            }

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle("Trickle+");
        actionBar.setBackgroundDrawable( new ColorDrawable(Color.parseColor("#2541b2")));
    }

    public class editBatteryItems extends AsyncTask<Integer, String, Boolean> {
        int level = 0;
        String health = "";
        int voltage = 0;
        int temperature = 0;
        int plugged = 0;
        int scale = 0;
        float batteryPercentage = 0;

        @Override
        protected Boolean doInBackground(final Integer... params) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        plugged = params[0];
                        scale = params[2];
                        level = params[3];

                        batteryPercentage = level / (float) scale;
                        health = getHealth(params[1]);
                        temperature = params[4];
                        voltage = params[5];
                        plugged = params[6];
                    }

                });
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressBarOfBattery.setProgress((int) (batteryPercentage * 100));
            currentBatteryValue.setText("" + (int) (batteryPercentage * 100));

            healthOfBattery.setTextColor(health.equals("Good") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
            healthOfBattery.setText(health);

            Float voltageOfBattery = (float) (voltage) / 1000;
            batteryVoltage.setText("" + voltageOfBattery + " V");

            Float tempOfBattery = (float) (temperature) / 10;
            if (tempOfBattery > 45) {
                textViewbatteryTemp.setTextColor(getResources().getColor(R.color.red));
            } else if (tempOfBattery <= 45 && tempOfBattery > 35) {
                textViewbatteryTemp.setTextColor(getResources().getColor(R.color.yellow));
            } else {
                textViewbatteryTemp.setTextColor(getResources().getColor(R.color.green));
            }
            textViewbatteryTemp.setText("" + tempOfBattery + " °C");

            if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
                batteryPlugged.setText("AC");
                batteryPlugged.setTextColor(getResources().getColor(R.color.green));
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                batteryPlugged.setText("USB");
                batteryPlugged.setTextColor(getResources().getColor(R.color.green));
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                batteryPlugged.setText("Wireless");
                batteryPlugged.setTextColor(getResources().getColor(R.color.green));
            } else {
                batteryPlugged.setText("Not Plugged");
                batteryPlugged.setTextColor(Color.LTGRAY);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
}
