package com.example.trickle.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

@TargetApi(17)
abstract class APILevel17Wrapper {

	static boolean sleepPolicySetToNever(final Context c) throws SettingNotFoundException {
		return Settings.Global.WIFI_SLEEP_POLICY_NEVER == Settings.Global.getInt(c.getContentResolver(),
				Settings.Global.WIFI_SLEEP_POLICY);
	}

	static boolean isAirplaneModeOn(final Context c) throws SettingNotFoundException {
		return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON) == 1;
	}

}
