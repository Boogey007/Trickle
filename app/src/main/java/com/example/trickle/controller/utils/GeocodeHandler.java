package com.example.trickle.controller.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;

import java.lang.ref.WeakReference;

import com.example.trickle.controller.R;
import com.example.trickle.controller.view.AddEditGeofenceActivity;

public class GeocodeHandler extends Handler {
    public static final int UPDATE_ADDRESS = 1;
    public static final int SAVE_AND_FINISH = 2;

    private final WeakReference<AddEditGeofenceActivity> mTarget;

    public GeocodeHandler(AddEditGeofenceActivity target) {
        mTarget = new WeakReference<>(target);
    }

    @Override
    public void handleMessage(Message msg) {
        AddEditGeofenceActivity target = mTarget.get();
        if (msg.what == UPDATE_ADDRESS) {
            Button button = (Button) target.findViewById(R.id.address_button);
            button.setText((String) msg.obj);
        } else if (msg.what == SAVE_AND_FINISH) {
            if (target.mProgressDialog.isShowing()) {
                target.mProgressDialog.hide();
            }
            target.save(true);
        }

    }
}
