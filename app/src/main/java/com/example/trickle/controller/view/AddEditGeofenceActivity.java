package com.example.trickle.controller.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.example.trickle.controller.TrickleBatteryManagerApplication;
import com.example.trickle.controller.R;
import com.example.trickle.controller.geo.LocativeGeocoder;
import com.example.trickle.controller.geo.LocativeLocationManager;
import com.example.trickle.controller.map.WorkaroundMapFragment;
import com.example.trickle.controller.persistent.GeofenceProvider;
import com.example.trickle.controller.utils.Constants;
import com.example.trickle.controller.utils.GeocodeHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import mapareas.MapAreaManager;
import mapareas.MapAreaMeasure;
import mapareas.MapAreaWrapper;

public class AddEditGeofenceActivity extends BaseActivity implements OnMapReadyCallback {

    public static final String TYPE = "type";

    public static final int DEFAULT_RADIUS_METERS = 50;
    public static final int MAX_RADIUS_METERS = 500;

    @BindView(R.id.address_button)
    Button mLocButt;
/*-------------------------------------*/
    @BindView(R.id.customLocationId)
    EditText mCID;
/*-------------------------------------*/

    @BindView(R.id.trigger_enter)
    Switch mEnter;
    @BindView(R.id.trigger_exit)
    Switch mExit;
/*-------------------------------------*/

    @BindView(R.id.radius_slider)
    SeekBar mRadMinMax;
    @BindView(R.id.radius_label)
    TextView mRadLabel;
/*-------------------------------------*/

    @BindView(R.id.scrollView)
    NestedScrollView mScrollView;
/*-------------------------------------*/

    public String mEditGeofenceId;
    private boolean mIsEditingGeofence = false;

    private LocativeLocationManager mLocativeManager = null;
    private MapAreaManager mCManager = null;
    private MapAreaWrapper mCircle = null;
    public ProgressDialog mProgressDialog = null;

    private GeocodeHandler mGeocoderHandler = null;
    private boolean mAddressNeedsFormatting = true;
    private boolean mGeocodeAndSave = false;
    private boolean mSaved = false;
    private Constants.HttpMethod mEnterMethod = Constants.HttpMethod.POST;
    private Constants.HttpMethod mExitMethod = Constants.HttpMethod.POST;
    private GoogleMap theGoogleMap = null;

    LocativeLocationManager.LocationResult locationResult = new LocativeLocationManager.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (theGoogleMap != null) {
                zoomToLocation(location);
            }
            setCircleToLocation(location);
            reverseGeocoding(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TrickleBatteryManagerApplication) getApplication()).getComponent().inject(this);

        mEditGeofenceId = getIntent().getStringExtra("geofenceId");
        if (mEditGeofenceId != null)
            mIsEditingGeofence = true;

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2541b2")));

        mRadMinMax.setMax(MAX_RADIUS_METERS);
        mRadMinMax.setProgress(DEFAULT_RADIUS_METERS);
        mRadMinMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int radiusMeters, boolean fromUser) {
                if (mCircle != null)
                    updateRadius();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        WorkaroundMapFragment WRMapFrags = (WorkaroundMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        WRMapFrags.getMapAsync(this);
        WRMapFrags.setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        mGeocoderHandler = new GeocodeHandler(this);
        updateRadius();
    }

    @SuppressWarnings("unsed")
    @OnClick(R.id.address_button)
    public void onButtonClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.address_button:
                final EditText addressTextField = new EditText(view.getContext());
                new AlertDialog.Builder(view.getContext())
                        .setMessage("Enter Address manually:")
                        .setView(addressTextField)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                geocodingAndPositionCircle(addressTextField.getText().toString());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        theGoogleMap = googleMap;
        theGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        theGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        theGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        theGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                theGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            theGoogleMap.setMyLocationEnabled(true);
        }
        theGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        Cursor cursor = null;
        if (mIsEditingGeofence) {
            ContentResolver resolver = this.getContentResolver();
            cursor = resolver.query(Uri.parse("content://" + "com.example.trickle.controller" + "/geofences"), null, "custom_id = ?", new String[]{String.valueOf(mEditGeofenceId)}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mLocButt.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_NAME)));
                mRadMinMax.setProgress(cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_RADIUS)));
                mCID.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_CUSTOMID)));
                int triggers = cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_TRIGGER));
                mEnter.setChecked(((triggers & GeofenceProvider.TRIGGER_ON_ENTER) == GeofenceProvider.TRIGGER_ON_ENTER));
                mExit.setChecked(((triggers & GeofenceProvider.TRIGGER_ON_EXIT) == GeofenceProvider.TRIGGER_ON_EXIT));
            }
        }


        mLocativeManager = new LocativeLocationManager();
        if (!mIsEditingGeofence) {
            mLocativeManager.getLocation(this, locationResult);
        }
        Location loc;
        if (theGoogleMap.isMyLocationEnabled() && theGoogleMap.getMyLocation() != null && !mIsEditingGeofence) {
            loc = theGoogleMap.getMyLocation();
            theGoogleMap.getMyLocation();
        } else if (cursor != null) {
            loc = new Location("location");
            loc.setLatitude(cursor.getDouble(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_LATITUDE)));
            loc.setLongitude(cursor.getDouble(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_LONGITUDE)));
        } else {
            loc = new Location("custom");
            loc.setLatitude(-31.215165);
            loc.setLongitude(127.123654);
        }

        theGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 16));

        setupCircleManager();
        if (mIsEditingGeofence) {
            setCircleToLocation(loc);
            if (cursor != null) {
                int radiusMeters = cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_RADIUS));
                mRadMinMax.setProgress(radiusMeters);
            } else {
                mRadMinMax.setProgress(50);
            }
        }
        updateRadius();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LocativeLocationManager.MY_PERMISSIONS_REQUEST:
                mLocativeManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_add_edit_geofence;
    }

    @Override
    protected String getToolbarTitle() {
        return "Add Geofence";
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.add_edit_geofence;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_save) {

            if (!mAddressNeedsFormatting) {
                this.save(true);
                return true;
            }

            if (mCircle == null) {
                return false;
            }

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Loading");
            mProgressDialog.setMessage("Determining Location and saving...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();

            mGeocodeAndSave = true;
            reverseGeoCircleLocation();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void save(boolean finish) {

        Log.i(Constants.LOG, "Saved #1: " + mSaved);

        if (mSaved) {
            return;
        }

        mSaved = true;
        ContentResolver resolver = this.getContentResolver();
        ContentValues newContentValues = new ContentValues();

        String custom_id = mCID.getText().toString();
        if (mIsEditingGeofence) {
            Cursor existingCursor = resolver.query(Uri.parse("content://" + "com.example.trickle.controller" + "/geofences"), null, "custom_id = ?", new String[]{String.valueOf(mEditGeofenceId)}, null);
            if (existingCursor != null && existingCursor.getCount() > 0) {
                existingCursor.moveToFirst();
                if (custom_id.length() == 0) {
                    custom_id = existingCursor.getString(existingCursor.getColumnIndex(GeofenceProvider.Geofence.KEY_CUSTOMID));
                }
            }
        }

        if (custom_id.length() == 0 && !mIsEditingGeofence) {
            custom_id = new UUID(new Random().nextLong(), new Random().nextLong()).toString();
        }

        int triggers = 0;
        if (mEnter.isChecked()) {
            triggers |= GeofenceProvider.TRIGGER_ON_ENTER;
        }
        if (mExit.isChecked()) {
            triggers |= GeofenceProvider.TRIGGER_ON_EXIT;
        }

        newContentValues.put(GeofenceProvider.Geofence.KEY_NAME, mLocButt.getText().toString());
        newContentValues.put(GeofenceProvider.Geofence.KEY_RADIUS, mCircle.getRadius()); // in meters
        newContentValues.put(GeofenceProvider.Geofence.KEY_CUSTOMID, custom_id);
        newContentValues.put(GeofenceProvider.Geofence.KEY_ENTER_METHOD, this.methodForTriggerType(Constants.TriggerType.ARRIVAL).ordinal());
        newContentValues.put(GeofenceProvider.Geofence.KEY_TRIGGER, triggers);
        newContentValues.put(GeofenceProvider.Geofence.KEY_EXIT_METHOD, this.methodForTriggerType(Constants.TriggerType.DEPARTURE).ordinal());
        newContentValues.put(GeofenceProvider.Geofence.KEY_LATITUDE, mCircle.getCenter().latitude);
        newContentValues.put(GeofenceProvider.Geofence.KEY_LONGITUDE, mCircle.getCenter().longitude);

        if (mIsEditingGeofence) {
            resolver.update(Uri.parse("content://" + "com.example.trickle.controller" + "/geofences"), newContentValues, "custom_id = ?", new String[]{String.valueOf(mEditGeofenceId)});
        } else {
            resolver.insert(Uri.parse("content://" + "com.example.trickle.controller" + "/geofences"), newContentValues);
        }

        if (finish) {
            this.finish();
            Log.i(Constants.LOG, "Finished!");
        }

        mSaved = false;

    }

    private void zoomToLocation(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        theGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 16));
    }

    private void setCircleToLocation(Location location) {
        if (theGoogleMap != null && mCManager != null && mCircle == null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            mCircle = new MapAreaWrapper(theGoogleMap, position, 100, 5.0f, 0xffff0000, 0x33ff0000, 1, 1000);
            mCManager.add(mCircle);
            mRadMinMax.setProgress(DEFAULT_RADIUS_METERS);
        }
    }

    private void setupCircleManager() {
        mCManager = new MapAreaManager(theGoogleMap,

                4, Color.RED, Color.HSVToColor(70, new float[]{1, 1, 200}), //styling

                -1,

                0.5f, 0.5f, //sets anchor point of move / resize drawable in the middle

                new MapAreaMeasure(100, MapAreaMeasure.Unit.pixels), //circles will start with 100 pixels (independent of zoom level)

                new MapAreaManager.CircleManagerListener() { //listener for all circle events

                    @Override
                    public void onCreateCircle(MapAreaWrapper draggableCircle) {

                    }

                    @Override
                    public void onMoveCircleEnd(MapAreaWrapper draggableCircle) {
                        reverseGeoCircleLocation();
                    }

                    @Override
                    public void onMoveCircleStart(MapAreaWrapper draggableCircle) {
                        mAddressNeedsFormatting = true;
                    }

                });
    }

    private void reverseGeoCircleLocation() {
        if (mCircle != null) {
            Location location = new Location("Geofence");
            location.setLongitude(mCircle.getCenter().longitude);
            location.setLatitude(mCircle.getCenter().latitude);
            reverseGeocoding(location);
        }
    }

    private void geocodingAndPositionCircle(String addr) {
        if (mCircle != null) {
            Address address = new LocativeGeocoder().getLatLongFromAddress(addr, this);
            if (address != null) {
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                updateAddressField(address);

                mCircle.setCenter(latLng);
                theGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("No location found. Please refine your query.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }
    }

    private void updateAddressField(Address address) {
        String addressText = String.format("%s, %s, %s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getLocality(),
                address.getCountryName());
        Message.obtain(mGeocoderHandler, GeocodeHandler.UPDATE_ADDRESS, addressText).sendToTarget();
    }

    private void reverseGeocoding(Location location) {
        (new ReverseGeocodingTask(this)).execute(location);
    }

    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            Location loc = params[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0)
                updateAddressField(addresses.get(0));

            mAddressNeedsFormatting = false;

            if (mGeocodeAndSave) {
                mGeocodeAndSave = false;
                Message.obtain(mGeocoderHandler, GeocodeHandler.SAVE_AND_FINISH, null).sendToTarget();
            }

            return null;
        }
    }


    private Constants.HttpMethod methodForTriggerType(Constants.TriggerType t) {
        if (t == Constants.TriggerType.ARRIVAL)
            return mEnterMethod;

        return mExitMethod;
    }


    private void updateRadius() {
        int radiusMeters = mRadMinMax.getProgress();
        mRadLabel.setText(String.format(getResources().getConfiguration().locale, "%d m", radiusMeters));
        if (mCircle != null) {
            mCircle.setRadius(radiusMeters);
        }
    }

}
