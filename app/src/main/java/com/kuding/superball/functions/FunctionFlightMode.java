package com.kuding.superball.functions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.provider.Settings;

import com.kuding.superball.ActionActivity;
import com.kuding.superball.info.FunctionItemInfo;
import com.kuding.superball.R;

/**
 * Created by user on 17-2-5.
 */

public class FunctionFlightMode extends FunctionItemInfo {

    private Context mContext;

    private BroadcastReceiver airplaneModeOn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {//飞行模式状态改变
                refreshIcon();
            }
        }
    };

    public FunctionFlightMode(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.registerReceiver(airplaneModeOn, filter);
        refreshIcon();
    }

    @Override
    public void onDelete(Context context) {
        try {
            context.unregisterReceiver(airplaneModeOn);
            airplaneModeOn = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        startActivity(context, ActionActivity.ACTION_AIR_PLANE);
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private void refreshIcon() {
        if (isAirPlaneModeOn()) {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext
                        .getDrawable(R.drawable.airplanemode_1);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        } else {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext
                        .getDrawable(R.drawable.ic_signal_airplane_enable);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        }
    }

    private boolean isAirPlaneModeOn() {
        int mode = 0;
        try {
            mode = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return mode == 1;//为1的时候是飞行模式
    }
}
