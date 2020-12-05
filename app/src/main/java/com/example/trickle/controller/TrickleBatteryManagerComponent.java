package com.example.trickle.controller;

import com.example.trickle.controller.modules.AppModule;
import com.example.trickle.controller.modules.PersistencyModule;
import com.example.trickle.controller.notification.NotificationManager;
import com.example.trickle.controller.service.ReceiveTransitionsIntentService;
import com.example.trickle.controller.service.TransitionService;
import com.example.trickle.controller.service.TriggerManager;
import com.example.trickle.controller.view.AddEditGeofenceActivity;
import com.example.trickle.controller.view.BaseActivity;
import com.example.trickle.controller.view.GeofencesActivity;
import com.example.trickle.controller.view.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, PersistencyModule.class})
public interface TrickleBatteryManagerComponent {
    void inject(ReceiveTransitionsIntentService object);

    void inject(GeofencesActivity object);

    void inject(SettingsActivity object);

    void inject(BaseActivity object);

    void inject(AddEditGeofenceActivity object);

    void inject(TriggerManager object);

    void inject(TransitionService object);

    void inject(NotificationManager object);

    void inject(HomeActivity object);

    void inject(BatteryLevelReceiver object);
}
