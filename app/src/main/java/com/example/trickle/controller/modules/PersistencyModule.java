package com.example.trickle.controller.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.persistent.Storage;

import dagger.Module;
import dagger.Provides;

@Module
public class PersistencyModule {

    private TrickleBatteryManagerApplication mApp;

    public PersistencyModule(TrickleBatteryManagerApplication app) {
        mApp = app;
    }

    @SuppressWarnings("unused")
    @Provides
    Storage provideStorage(Context context) {
        return new Storage(context);
    }

    @SuppressWarnings("unused")
    @Provides
    SharedPreferences providePreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
