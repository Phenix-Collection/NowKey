package com.kuding.superball.functions.rotate;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import com.kuding.superball.ActionActivity;
import com.kuding.superball.info.FunctionItemInfo;
import com.kuding.superball.R;

/**
 * Created by user on 17-2-5.
 */

public class FunctionRotation extends FunctionItemInfo {
    private Context mContext;
    private RotationObserver mRotationObserver;

    public FunctionRotation(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        mRotationObserver = new RotationObserver(new Handler());
        mRotationObserver.startObserver();
        refreshButton();
    }

    @Override
    public void onDelete(Context context) {
        try {
            mRotationObserver.stopObserver();
            mRotationObserver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                startActivity(mContext, ActionActivity.ACTION_ROTATION);
            } else {
                //有了权限，具体的动作
                if (getRotationStatus(mContext) == 1) {
                    setRotationStatus(context.getContentResolver(), 0);
                } else {
                    setRotationStatus(context.getContentResolver(), 1);
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private void setRotationStatus(ContentResolver resolver, int status) {
        //得到uri
        Uri uri = android.provider.Settings.System.getUriFor("accelerometer_rotation");
        //沟通设置status的值改变屏幕旋转设置
        android.provider.Settings.System.putInt(resolver, "accelerometer_rotation", status);
        //通知改变
        resolver.notifyChange(uri, null);
    }

//    private boolean rotate(Context context) {
//        try {
//            Class<?> clazz = Class.forName("com.android.internal.view.RotationPolicy");
//            Method isRotationLocked = clazz.getMethod("isRotationLocked", Context.class);
//            isRotationLocked.setAccessible(true);
//            Object isLocked = isRotationLocked.invoke(null, context);
//            Method setRotationLock = clazz.getMethod("setRotationLock", Context.class, boolean.class);
//            setRotationLock.setAccessible(true);
//            if ((boolean) isLocked) {
//                setRotationLock.invoke(null, context, false);
//            } else {
//                setRotationLock.invoke(null, context, true);
//            }
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    //更新按钮状态
    private void refreshButton() {
        if (getRotationStatus(mContext) == 1) {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext
                        .getDrawable(R.drawable.ic_portrait_from_auto_rotate);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        } else {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext
                        .getDrawable(R.drawable.ic_portrait_to_auto_rotate);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        }
    }

    //得到屏幕旋转的状态
    private int getRotationStatus(Context context) {
        int status = 0;
        try {
            status = android.provider.Settings.System.getInt(context.getContentResolver(),
                    android.provider.Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return status;
    }

    private class RotationObserver extends ContentObserver {
        ContentResolver mResolver;

        public RotationObserver(Handler handler) {
            super(handler);
            mResolver = mContext.getContentResolver();
        }

        //屏幕旋转设置改变时调用
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshButton();
        }

        public void startObserver() {
            mResolver.registerContentObserver(Settings.System
                            .getUriFor(Settings.System.ACCELEROMETER_ROTATION), false,
                    this);
        }

        public void stopObserver() {
            mResolver.unregisterContentObserver(this);
        }
    }
}
