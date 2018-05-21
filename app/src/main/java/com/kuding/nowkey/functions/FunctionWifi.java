package com.kuding.nowkey.functions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.R;

/**
 * Created by user on 17-2-5.
 */

public class FunctionWifi extends FunctionItemInfo {

    public FunctionWifi(Context context) {
        super(context);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    //System.out.println("系统关闭wifi");
                    if (iconView != null) {
                        VectorDrawable vd =
                                (VectorDrawable) context.getDrawable(R.drawable.ic_qs_wifi_disabled);
                        vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                        vd.setTint(Color.WHITE);
                        iconView.setImageDrawable(vd);
                    }
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    //System.out.println("系统开启wifi");
                    if (iconView != null) {
                        VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.wifi);
                        vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                        vd.setTint(Color.WHITE);
                        iconView.setImageDrawable(vd);
                    }
                }
            }
        }
    };

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(broadcastReceiver, filter);
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled()) {
            if (iconView != null) {
                VectorDrawable vd =
                        (VectorDrawable) context.getResources().getDrawable(R.drawable.ic_qs_wifi_disabled);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        } else {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.wifi);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        }
    }

    @Override
    public void onDelete(Context context) {
        try {
            context.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        } else {
            wm.setWifiEnabled(false);
        }

        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
