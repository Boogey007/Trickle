package com.example.trickle.controller.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

public class WorkaroundMapFragment extends SupportMapFragment {
    private OnTouchListener touchLister;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstance) {
        View layout = super.onCreateView(layoutInflater, viewGroup, savedInstance);

        TouchableWrapper layoutOfFrame = new TouchableWrapper(getActivity());

        layoutOfFrame.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));

        ((ViewGroup) layout).addView(layoutOfFrame,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return layout;
    }

    public void setListener(OnTouchListener listener) {
        touchLister = listener;
    }

    public interface OnTouchListener {
        public abstract void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                touchLister.onTouch();
            } else if (action == MotionEvent.ACTION_UP) {
                touchLister.onTouch();
            }
            return super.dispatchTouchEvent(event);
        }
    }
}
