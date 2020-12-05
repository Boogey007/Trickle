package com.example.trickle.controller;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.example.trickle.controller.persistent.Storage;
import com.example.trickle.controller.view.GeofenceFragment;

import java.util.List;

import javax.inject.Inject;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNav;
    private ViewPager mViewpager;
    private FrameLayout mMainFrame;

    @Inject
    Storage mStorage;

    private BatteryFragment battFrag;
    private ControllerFragment cFrag;
    private GeofencingFragment geoFrag;

    private String fragTag = GeofenceFragment.TAG;
    private static final String FT = "current.fragment";

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(fragTag))
                fragTag = savedInstanceState.getString(fragTag);
        }

        super.onCreate(savedInstanceState);
        ((TrickleBatteryManagerApplication) getApplication()).getComponent().inject(this);
        setContentView(R.layout.activity_home);

        mMainFrame = (FrameLayout) findViewById(R.id.main_container);
        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);

        mContext = getApplicationContext();
        battFrag = new BatteryFragment();
        cFrag = new ControllerFragment();
        geoFrag = new GeofencingFragment();

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2541b2")));
        }

        setFragment(battFrag, "Trickle+");
        updateToolbarText("Trickle+");

        mBottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_battery:
                                setFragment(battFrag, "Trickle+");
                                updateToolbarText("Trickle+");
                                refresh();
                                return true;
                            case R.id.menu_controller:
                                setFragment(cFrag, "Controller");
                                updateToolbarText("Controller");
                                refresh();
                                return true;
                            case R.id.menu_geofencing:
                                setFragment(geoFrag, "Geofencing");
                                updateToolbarText("Geofencing");
                                refresh();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
    }

    private void setFragment(Fragment fragment, String name) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.main_container, fragment, name);
        fragmentTransaction.commit();
    }

    private void refresh() {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrolly);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, (findViewById(R.id.activity_main)).getTop());
            }
        });
    }

    public void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> frags = fragmentManager.getFragments();
        if (frags != null) {
            for (Fragment fragment : frags) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (getVisibleFragment() != null) {
            String nameFragment = getVisibleFragment().toString();
            if (nameFragment.contains("Trickle+")) {
                this.finishAffinity();
            } else {
                setFragment(battFrag, "Trickle+");
                mBottomNav.setSelectedItemId(R.id.menu_battery);
            }
        } else {
            setFragment(battFrag, "Trickle+");
            mBottomNav.setSelectedItemId(R.id.menu_battery);
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FT, FT);
    }

    @Override
    protected void onStart() { super.onStart(); }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { super.onRestoreInstanceState(savedInstanceState); }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) { super.onPostCreate(savedInstanceState); }

}