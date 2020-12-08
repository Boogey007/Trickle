package com.example.trickle.controller;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.trickle.controller.controller.DisplayController;
import com.example.trickle.controller.controller.GPSController;
import com.example.trickle.controller.controller.NetworkController;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.example.trickle.controller.controller.DisplayController.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED;

@RuntimePermissions
public class ControllerFragment extends Fragment {

    @BindView(R.id.wifiSwitch)
    SwitchCompat wifiSwitch;
/*---------------------------------------------------*/
    @BindView(R.id.wifiText)
    TextView wifiText;
/*---------------------------------------------------*/

    @BindView(R.id.mobileDataSwitch)
    SwitchCompat mobileDataSwitch;
    @BindView(R.id.mobile_data_text)
    TextView mobileDataText;
/*---------------------------------------------------*/

    @BindView(R.id.flightModeSwitch)
    SwitchCompat flightModeSwitch;
    @BindView(R.id.flightModeText)
    TextView flightModeText;
/*---------------------------------------------------*/

    @BindView(R.id.hotspotSwitch)
    SwitchCompat hotspotSwitch;
    @BindView(R.id.hotspotText)
    TextView hotspotText;
/*---------------------------------------------------*/

    @BindView(R.id.gpsSwitch)
    SwitchCompat gpsSwitch;
    @BindView(R.id.gpsText)
    TextView gpsText;
/*---------------------------------------------------*/

    @BindView(R.id.brightnessText)
    TextView brightnessText;
/*---------------------------------------------------*/

    @BindView(R.id.autoBrightnessSwitch)
    SwitchCompat autoBrightnessSwitch;
/*---------------------------------------------------*/

    GPSController gpsC;
    NetworkController networkC;
    DisplayController displayC;

    private Context mContext;
    private Unbinder unbinder;

    IntentFilter intentF = null;
    BroadcastReceiver mRec = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();

        gpsC = GPSController.getInstance(mContext);
        networkC = NetworkController.getInstance(mContext);
        displayC = DisplayController.getInstance(mContext, getActivity());

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initControllerItems();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void initControllerItems() {
        initializeNetwork();
        initializeGps();
        initializeDisplay();

        Intent i = new Intent(this.getActivity(), ControllerService.class);
        getActivity().startService(i);

        intentF = new IntentFilter();
        intentF.addAction("com.example.trickle.controller.controllerservice");
        mRec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifi, data, plane, gps, hs;
                wifi = intent.getIntExtra("wifi_state", 0);
                data = intent.getIntExtra("data_state", 0);
                plane = intent.getIntExtra("plane_state", 0);
                gps = intent.getIntExtra("gps_state", 0);
                hs = intent.getIntExtra("hotspot_state", 0);
                editControllerItems eci = new editControllerItems();

                eci.execute(wifi, data, plane, hs, gps);
            }
        };
        getActivity().registerReceiver(mRec, intentF);
    }

    public void initializeNetwork() { initNetworkSwitch();}
    private void initNetworkSwitch() {
        wifiSwitch.setChecked(networkC.isWifiConnected());
        mobileDataSwitch.setChecked(networkC.isMobileNetworkConnected());
        flightModeSwitch.setChecked(networkC.isAirplaneModeOn(mContext));
    }

    @OnClick({R.id.wifiText, R.id.wifi_icon})
    public void wifiSettings(View view) { networkC.wifiSwitchSettings(); }

    @OnCheckedChanged(R.id.wifiSwitch)
    public void wifiSwitchToggle(boolean checked) { networkC.toggleWiFi(checked); }

    @OnClick({R.id.flightModeRow})
    public void flightModeSettings(View view) { networkC.flightModeTogglePermission(); }

    @OnClick({R.id.hotspotRow})
    public void hotspotSettings(View view) {networkC.hsTogglePermission(); }

    @OnClick({R.id.brightnessText})
    public void brightnessSettings(View view) { }

    @OnCheckedChanged({R.id.autoBrightnessSwitch})
    public void autoBrightnessSwitchToggle(boolean checked) {
        if (autoBrightnessSwitch.isChecked())
            displayC.setBrightnessToAuto();
         else
            displayC.setBrightnessToManual();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializeDisplay() { displayC.init(); }

    public void initializeGps() { initGpsSwitch(); }
    private void initGpsSwitch() {
        boolean gpsStatus = gpsC.activeGPS();
        gpsSwitch.setChecked(gpsStatus);
    }

    @OnClick({R.id.gpsRow})
    public void dataRoamingSettings(View view) {
        gpsC.switchGPS();
    }

    public class editControllerItems extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(final Integer... params) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < params.length; i++) {
                        if (i == 0) {
                            if (params[i] == 1) {
                                wifiSwitch.setChecked(true);
                            } else if (params[i] == -1) {
                                wifiSwitch.setChecked(false);
                            }
                        } else if (i == 1) {
                            if (params[i] == 1) {
                                mobileDataSwitch.setChecked(true);
                            } else if (params[i] == -1) {
                                mobileDataSwitch.setChecked(false);
                            }
                        } else if (i == 2) {
                        } else if (i == 3) {
                            if (params[i] == 1) {
                                flightModeSwitch.setChecked(true);
                            } else if (params[i] == -1) {
                                flightModeSwitch.setChecked(false);
                            }
                        } else if (i == 4) {
                            if (params[i] == 1) {
                                hotspotSwitch.setChecked(true);
                            } else if (params[i] == -1) {
                                hotspotSwitch.setChecked(false);
                            }
                        } else if (i == 5) {
                            if (params[i] == 1) {
                                gpsSwitch.setChecked(true);
                            } else if (params[i] == -1) {
                                gpsSwitch.setChecked(false);
                            }
                        }
                    }
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) { }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar aB = activity.getSupportActionBar();
        if (aB != null)
            aB.setTitle("Controller");

        boolean writeToSettingsAccess = Settings.System.canWrite(mContext);
        if ( writeToSettingsAccess )
            displayC.enableBrightnessSettings();
    }

    @Override
    public void onDestroy() {
        if (mRec != null) {
            try {
                getActivity().unregisterReceiver(mRec);
                mRec = null;
            } catch (IllegalArgumentException e) { }
        }
        super.onDestroy();
    }
}
