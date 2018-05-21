package com.kuding.nowkey.functions;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import com.kuding.nowkey.functions.recent.RecentActivity;
import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.R;

import java.lang.reflect.Method;

/**
 * Created by user on 17-2-5.
 */

public class FunctionSpilitScreen extends FunctionItemInfo {
    private Context mContext;
    private SpilitScreenObserver mRotationObserver;
    VectorDrawable mVdStartSplit;
    VectorDrawable mVdStopSplit;

    public FunctionSpilitScreen(Context context) {
        super(context);
        mContext = context;
        mVdStartSplit = (VectorDrawable) mContext.getDrawable(R.drawable.split_screen_gray);
        mVdStopSplit = (VectorDrawable) mContext.getDrawable(R.drawable.split_screen_gray_close);
    }

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        mRotationObserver = new SpilitScreenObserver(new Handler());
        mRotationObserver.startObserver();
        refreshButton();
    }

    @Override
    public void onDelete(Context context) {
        try {
            mRotationObserver.stopObserver();
            mRotationObserver = null;
            if (iconView != null) {
                iconView.setBackground(null);
            }
            mVdStartSplit = null;
            mVdStopSplit = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {

        try {
            int appSwitchLongClick = Settings.Global.getInt(context.getContentResolver(), "now_key_patch_done", 0);
            if (appSwitchLongClick == 1) {
                Class serviceManagerClass = Class.forName("android.os.ServiceManager");
                Method getService = serviceManagerClass.getMethod("getService",
                        String.class);
                IBinder retbinder = (IBinder) getService.invoke(
                        serviceManagerClass, "statusbar");
                Class statusBarClass = Class.forName(retbinder
                        .getInterfaceDescriptor());
                Object statusBarObject = statusBarClass.getClasses()[0].getMethod(
                        "asInterface", IBinder.class).invoke(null,
                        new Object[]{retbinder});
                Method clearAll = statusBarClass.getMethod("toggleSplitScreen");
                clearAll.setAccessible(true);
                clearAll.invoke(statusBarObject);
            } else {
                Intent intent = new Intent();
                intent.putExtra("recent_extra", 3);
                intent.setClass(context, RecentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.putExtra("recent_extra", 3);
            intent.setClass(context, RecentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    //更新按钮状态
    private void refreshButton() {

        if (getSpilitScreenStatus(mContext) == 0) {
            if (iconView != null) {
                if (mVdStartSplit == null) {
                    mVdStartSplit = (VectorDrawable) mContext.getDrawable(R.drawable.split_screen_gray);
                }
                mVdStartSplit.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                mVdStartSplit.setTint(Color.WHITE);
                iconView.setImageDrawable(mVdStartSplit);
            }
        } else {
            if (iconView != null) {
                if (mVdStopSplit == null) {
                    mVdStopSplit = (VectorDrawable) mContext.getDrawable(R.drawable.split_screen_gray_close);
                }
                mVdStopSplit.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                mVdStopSplit.setTint(Color.WHITE);
                iconView.setImageDrawable(mVdStopSplit);
            }
        }
    }


    //得到屏幕的状态
    private int getSpilitScreenStatus(Context context) {
        int status = 0;
        try {
            status = Settings.Global.getInt(context.getContentResolver(),
                    "sysbar_docked_nowkey", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    private class SpilitScreenObserver extends ContentObserver {
        ContentResolver mResolver;

        public SpilitScreenObserver(Handler handler) {
            super(handler);
            mResolver = mContext.getContentResolver();
        }

        //屏幕设置改变时调用
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshButton();
        }

        public void startObserver() {
            mResolver.registerContentObserver(Settings.Global
                    .getUriFor("sysbar_docked_nowkey"), false, this);
        }

        public void stopObserver() {
            mResolver.unregisterContentObserver(this);
        }
    }

}
