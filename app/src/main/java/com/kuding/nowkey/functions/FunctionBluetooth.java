package com.kuding.nowkey.functions;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;

import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.R;

/**
 * Created by user on 17-2-5.
 */

public class FunctionBluetooth extends FunctionItemInfo {

    public FunctionBluetooth(Context context) {
        super(context);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
//                            LogUtil.e("onReceive---------STATE_TURNING_ON");
                            break;
                        case BluetoothAdapter.STATE_ON:
//                            LogUtil.e("onReceive---------STATE_ON");
                            if (iconView != null) {
                                VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.bluetooth);
                                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                                vd.setTint(Color.WHITE);
                                iconView.setImageDrawable(vd);
                            }
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
//                            LogUtil.e("onReceive---------STATE_TURNING_OFF");
                            if (iconView != null) {
                                VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.ic_qs_bluetooth_off);
                                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                                vd.setTint(Color.WHITE);
                                iconView.setImageDrawable(vd);
                            }
                            break;
                        case BluetoothAdapter.STATE_OFF:
//                            LogUtil.e("onReceive---------STATE_OFF");
                            break;
                    }
                    break;
            }
        }
    };

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.ic_qs_bluetooth_off);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        } else {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.bluetooth);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        }
    }

    @Override
    public void onDelete(Context context) {
        try {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
        } else {
            bluetoothAdapter.disable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
