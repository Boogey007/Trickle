package com.example.trickle.controller.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

public class AddGeofenceDialogFragment extends DialogFragment implements AddGeofenceDialog {
    private AddGeofenceResultListener mLocalListen;
    private AddGeofenceResultListener mImporterListen;
    public static final String DIALOG_TAG = "AddGeofenceDialogFragment";

    @Override
    public void show(FragmentManager fragmentManager) { show(fragmentManager, DIALOG_TAG); }

    public static AddGeofenceDialog createInstance() { return new AddGeofenceDialogFragment(); }

    @Override
    public void setLocallyListener(AddGeofenceResultListener resultListener) {
        mLocalListen = resultListener;
    }

    @Override
    public void setImportListener(AddGeofenceResultListener resultListener) { mImporterListen = resultListener; }

    public interface AddGeofenceResultListener { public void onResult(); }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder newAlertbuilder = new AlertDialog.Builder(getActivity());

        newAlertbuilder.setTitle("Add a Geofence")
                .setMessage("Add a new Geofence ...")
                .setPositiveButton("Local", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trigger(mLocalListen);
                    }
                })
                .setNegativeButton("Import", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trigger(mImporterListen);
                    }
                });

        return newAlertbuilder.create();
    }

    private void trigger(AddGeofenceDialogFragment.AddGeofenceResultListener listener) {
        if (listener != null)
            listener.onResult();
    }
}

