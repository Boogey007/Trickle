package com.example.trickle.controller.modules;

import android.content.Context;

import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.notification.NotificationManager;
import com.example.trickle.controller.service.TriggerManager;
import com.example.trickle.controller.utils.ResourceUtils;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private TrickleBatteryManagerApplication mApp;

    public AppModule(TrickleBatteryManagerApplication application) { mApp = application; }

    @Provides
    Context getApplicationContext() { return mApp; }

    @Provides
    NotificationManager provideNotificationManager(Context context) { return new NotificationManager(context); }

    @Provides
    TriggerManager provideTriggerManager() { return new TriggerManager(); }

    @Provides
    @Singleton
    Bus provideBus() { return new Bus(); }

    @Provides
    ResourceUtils provideResourceUtils() { return new ResourceUtils(mApp); }
}
