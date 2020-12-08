package com.example.trickle.controller.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.trickle.controller.model.Geofences;
import com.example.trickle.controller.persistent.GeofenceProvider;
import com.example.trickle.controller.service.LocativeService;

import java.util.ArrayList;

public class StartupBroadCastReceiver extends BroadcastReceiver {

    public static final String TAG = "Geofencing";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Uri uri = Uri.parse("content://" + "com.example.trickle.controller" + "/geofences");
        Loader<Cursor> loader = new CursorLoader(context, uri, null, null, null, null);

        loader.registerListener(0, new Loader.OnLoadCompleteListener<Cursor>() {
            @Override
            public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
                ArrayList<Geofences.Geofence> items = new ArrayList<>();
                if (data != null) {
                    while (data.moveToNext()) {
                        Geofences.Geofence item = GeofenceProvider.fromCursor(data);
                        items.add(item);
                    }
                }
                Intent startIntent = new Intent(context, LocativeService.class);
                startIntent.putExtra(LocativeService.EXTRA_ACTION, LocativeService.Action.ADD);
                startIntent.putExtra(LocativeService.EXTRA_GEOFENCE, items);
                context.startService(startIntent);
            }
        });

        loader.startLoading();
    }
}