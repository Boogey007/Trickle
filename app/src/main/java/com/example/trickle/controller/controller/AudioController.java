package com.example.trickle.controller.controller;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import com.example.trickle.controller.utils.Constants;
import static com.example.trickle.controller.utils.Constants.NORMAL_MODE;
import static com.example.trickle.controller.utils.Constants.SILENT_MODE;
import static com.example.trickle.controller.utils.Constants.VIBRATE_MODE;

public class AudioController {

    private Context mContext;
    private AudioManager aM;

    private static AudioController mInstance;

    protected AudioController(Context context) {
        this.mContext = context;
        aM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public static AudioController getInstance(Context context) {
        if (mInstance == null)
            mInstance = new AudioController(context);

        return mInstance;
    }

    public void muteThePhone(int modeSetting) {
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            mContext.startActivity(intent);
        } else {
            if (modeSetting == NORMAL_MODE) {
                setPhoneRingerToNormal();
            } else if (modeSetting == VIBRATE_MODE) {
                setPhoneRingerToVibrate();
            } else if (modeSetting == SILENT_MODE) {
                setPhoneRingerToSilent();
            }
        }
    }

    public void setPhoneRingerToNormal() {
        aM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void setPhoneRingerToVibrate() {
        aM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    public void setPhoneRingerToSilent() {
        aM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public int getRingerMode() {
        int ringerMode = aM.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            return NORMAL_MODE;
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            return VIBRATE_MODE;
        } else if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            return SILENT_MODE;
        }
        return 0;
    }
}