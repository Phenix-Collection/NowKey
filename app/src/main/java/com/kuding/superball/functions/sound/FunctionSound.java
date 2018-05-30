package com.kuding.superball.functions.sound;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.media.AudioManager;
import android.os.Build;

import com.kuding.superball.info.FunctionItemInfo;
import com.kuding.superball.R;

/**
 * Created by user on 17-2-5.
 */

public class FunctionSound extends FunctionItemInfo {
    private AudioManager mAudioManager;
    private Context mContext;

    private int mRingMode;

    private BroadcastReceiver mRingModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                mRingMode = mAudioManager.getRingerMode();
                refreshState();
            }
        }
    };

    public FunctionSound(Context context) {
        super(context);
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        context.registerReceiver(mRingModeReceiver, filter);
        mRingMode = mAudioManager.getRingerMode();
        refreshState();
    }

    @Override
    public void onDelete(Context context) {
        try {
            context.unregisterReceiver(mRingModeReceiver);
            mRingModeReceiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        setRingMode();
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private void setRingMode() {
        switch (mRingMode) {
            case AudioManager.RINGER_MODE_SILENT:
                NotificationManager nof = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nof.isNotificationPolicyAccessGranted()) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(mContext, FunctionSoundMuteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                NotificationManager notificationManager = (NotificationManager) mContext.
                        getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (notificationManager.isNotificationPolicyAccessGranted()) {
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(mContext, FunctionSoundMuteActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
            default:
                break;
        }
    }

    private void refreshState() {
        VectorDrawable vd = null;
        String title = null;
        switch (mRingMode) {
            case AudioManager.RINGER_MODE_SILENT:
                vd = (VectorDrawable) mContext.getDrawable(R.drawable.ic_volume_ringer_mute);
                title = mContext.getString(R.string.now_key_function_function_sound_mute);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                vd = (VectorDrawable) mContext.getDrawable(R.drawable.ic_volume_ringer_vibrate);
                title = mContext.getString(R.string.now_key_function_function_sound_vibrate);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                vd = (VectorDrawable) mContext.getDrawable(R.drawable.ic_volume_ringer);
                title = mContext.getString(R.string.now_key_function_function_sound_ring);
                break;
            default:
                break;
        }
        if (vd == null) return;
        vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
        vd.setTint(Color.WHITE);
        if (iconView != null) {
            iconView.setImageDrawable(vd);
        }
        if (titleView != null && title != null) {
            titleView.setText(title);
        }
    }
}
