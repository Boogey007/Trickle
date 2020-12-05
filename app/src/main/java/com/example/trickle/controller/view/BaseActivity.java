package com.example.trickle.controller.view;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Toast;

import com.example.trickle.controller.TrickleBatteryManagerApplication;

import javax.inject.Inject;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Inject
    protected SharedPreferences mPrefs;

    private static final int REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getLayoutResourceId() != 0) {
            setContentView(getLayoutResourceId());
        }

        ButterKnife.bind(this);

        ((TrickleBatteryManagerApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getMenuResourceId() != 0) {
            getMenuInflater().inflate(getMenuResourceId(), menu);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "ERROR: Permission Refused", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected abstract int getLayoutResourceId();

    @Nullable
    protected abstract String getToolbarTitle();

    protected abstract int getMenuResourceId();
}