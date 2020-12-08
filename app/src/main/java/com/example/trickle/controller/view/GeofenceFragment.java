package com.example.trickle.controller.view;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.trickle.controller.R;
import com.example.trickle.controller.model.Geofences;
import com.example.trickle.controller.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeofenceFragment extends android.support.v4.app.ListFragment {

    public Geofences geofences = new Geofences();
    public static final String TAG = "fragment.geofences";
    private static final String ARG_P1 = "param1";
    private static final String ARG_P2 = "param2";
    private String mP1;
    private String mP2;

    private OnFragmentInteractionListener mListener;

    public static GeofenceFragment newInstance(String param1, String param2) {
        GeofenceFragment fragment = new GeofenceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_P1, param1);
        args.putString(ARG_P2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GeofenceFragment() { }

    public void refresh() {

        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();

        int i = 0;
        while (i < Geofences.GeofenceItems.size()) {
            HashMap<String, String> hm = new HashMap<String, String>();
            Geofences.Geofence geofence = Geofences.GeofenceItems.get(i);
            hm.put("image", "0");
            hm.put("title", geofence.name);
            hm.put("subtitle", "ID: " + geofence.getRelevantId());
            aList.add(hm);
            i++;
        }

        String[] from = {
                "image",
                "title",
                "subtitle"
        };

        int[] to = {
                R.id.image,
                R.id.title,
                R.id.subtitle
        };

        if (getActivity() != null) {
            GeofencesAdapter adapter = new GeofencesAdapter(getActivity().getBaseContext(), aList, R.layout.geofence_row, from, to);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mP1 = getArguments().getString(ARG_P1);
            mP2 = getArguments().getString(ARG_P2);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName() + " must implement OnGeofenceSelection");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener)
            mListener.onFragmentInteraction(Geofences.GeofenceItems.get(position).uuid);
    }

    @Override
    public void onResume() {
        super.onResume();
        setListShown(true);
        refresh();
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        setListShown(true);
        refresh();

        setEmptyText("You don't have any Geofences. Click \"+\" to add");

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
                final int pos = position;
                final View aView = v;
                new AlertDialog.Builder(v.getContext())
                        .setTitle(" Delete")
                        .setMessage("Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues values = new ContentValues();
                                Geofences.Geofence item = Geofences.GeofenceItems.get(pos);

                                ContentResolver resolver = aView.getContext().getContentResolver();

                                resolver.delete(Uri.parse("content://" + "com.example.trickle.controller" + "/geofences"), "custom_id = ?", new String[]{item.customId});
                                Geofences.GeofenceItems.remove(pos);
                                refresh();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(R.drawable.warning_black)
                        .show();
                return true;
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Geofences.Geofence item = Geofences.GeofenceItems.get(position);
                Intent addEditGeofencesIntent = new Intent(getActivity(), AddEditGeofenceActivity.class);
                addEditGeofencesIntent.putExtra("geofenceId", item.customId);
                getActivity().startActivity(addEditGeofencesIntent);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }
}