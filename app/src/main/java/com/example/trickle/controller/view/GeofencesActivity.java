package com.example.trickle.controller.view;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.widget.FrameLayout;

import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.R;
import com.example.trickle.controller.geo.LocativeGeocoder;
import com.example.trickle.controller.model.Geofences;
import com.example.trickle.controller.persistent.GeofenceProvider;
import com.example.trickle.controller.persistent.Storage;
import com.example.trickle.controller.service.LocativeService;
import com.example.trickle.controller.utils.Dialog;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class GeofencesActivity extends BaseActivity implements GeofenceFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTIFICATION_CLICK = "notification_click";

    @BindView(R.id.container)
    FrameLayout mContentFrame;

    @BindView(R.id.add_geofence)
    FloatingActionButton mFabButton;

    @Inject
    Storage mStorage;

    private GeofenceFragment mGeofenceFragment = null;

    private String fragTag = GeofenceFragment.TAG;
    private static final String fragsTag = "current.fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(fragTag))
                fragTag = savedInstanceState.getString(fragTag);

        }
        super.onCreate(savedInstanceState);
        ((TrickleBatteryManagerApplication) getApplication()).getComponent().inject(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(fragTag, fragTag);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2541b2")));
        }
        if (Geofences.GeofenceItems.size() == 0) {
            load();
        }
        android.support.v4.app.FragmentManager fragman = getSupportFragmentManager();
        switch (fragTag) {
            case GeofenceFragment.TAG: {
                android.support.v4.app.Fragment f = fragman.getFragment(new Bundle(), GeofenceFragment.TAG);
                if (mGeofenceFragment == null)
                    mGeofenceFragment = f != null ? (GeofenceFragment) f : GeofenceFragment.newInstance("str1", "str2");
                fragman.beginTransaction().replace(R.id.container, mGeofenceFragment, GeofenceFragment.TAG).commit();
                if (Geofences.GeofenceItems.size() == 0) {
                    load();
                }
                break;
            }
        }
    }

    public void load() {
        if (getLoaderManager().getLoader(0) == null)
            getLoaderManager().initLoader(0, null, this);
        getLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    public void onFragmentInteraction(String id) { }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { super.onRestoreInstanceState(savedInstanceState); }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    protected int getLayoutResourceId() { return R.layout.activity_geofences; }

    @Override
    protected String getToolbarTitle() { return null; }

    @Override
    protected int getMenuResourceId() { return 0; }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) { super.onPostCreate(savedInstanceState); }

    @SuppressWarnings("unused")
    @OnClick(R.id.add_geofence)
    public void addGeofenceClick() {
        createGeofence();
    }

    private void createGeofence() {
        Intent addEditGeofencesIntent = new Intent(GeofencesActivity.this, AddEditGeofenceActivity.class);
        GeofencesActivity.this.startActivity(addEditGeofencesIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://" + "com.example.trickle.controller" + "/geofences");
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGeofenceFragment.geofences.clear();
        ArrayList<Geofences.Geofence> items = new ArrayList<>();
        while (data.moveToNext()) {
            Geofences.Geofence item = GeofenceProvider.fromCursor(data);
            mGeofenceFragment.geofences.addItem(item);
            items.add(item);
        }
        mGeofenceFragment.refresh();

        updateGeofencingService(items);
    }

    private void updateGeofencingService(ArrayList<Geofences.Geofence> items) {
        Intent geoService = new Intent(this, LocativeService.class);
        geoService.putExtra(LocativeService.EXTRA_ACTION, LocativeService.Action.ADD);
        geoService.putExtra(LocativeService.EXTRA_GEOFENCE, items);
        this.startService(geoService);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    public SharedPreferences getPrefs() { return this.getPreferences(MODE_PRIVATE); }

    public void onGeofenceImportSelection(final Geofences.Geofence fence) {
        if (!mStorage.fenceExistsWithCustomId(fence)) {
            insertGeofence(fence);
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage("Duplicate Geofence. Would you like to overwrite?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        insertGeofence(fence);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void insertGeofence(final Geofences.Geofence fence) {
        final Activity self = this;
        final ProgressDialog dialog = Dialog.getIndeterminateProgressDialog(this, "Importing Geofence…");
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Address address = new LocativeGeocoder().getFromLatLong(fence.latitude, fence.longitude, self);
                if (address != null)
                    fence.name = address.getAddressLine(0);

                mStorage.insertOrUpdateFence(fence);
                final android.support.v4.app.FragmentManager fragManager = getSupportFragmentManager();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        android.support.v4.app.FragmentTransaction transaction = fragManager.beginTransaction();
                        Geofences.GeofenceItems.add(fence);
                        transaction.replace(R.id.container, mGeofenceFragment, GeofenceFragment.TAG).commit();
                        mFabButton.show();

                    }
                });
            }
        }).run();

    }

}
