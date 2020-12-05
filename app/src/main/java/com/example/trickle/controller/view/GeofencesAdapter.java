package com.example.trickle.controller.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.example.trickle.controller.R;

import java.util.List;
import java.util.Map;

public class GeofencesAdapter extends SimpleAdapter {
    public GeofencesAdapter(Context context, List<? extends Map<String, String>> data,
                            int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setImageResource(R.drawable.location_black);

        return view;
    }

}
