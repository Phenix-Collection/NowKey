package com.kuding.nowkey.functions.flashlight;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;

import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.R;

/**
 * Created by user on 17-2-5.
 */

public class FunctionLight extends FunctionItemInfo implements
        FlashlightController.FlashlightListener {

    private Context mContext;

    public FunctionLight(Context context) {
        super(context);
        mContext = context;

    }

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
        FlashlightController.getInstance(context).addListener(this);
        boolean isEnable = FlashlightController.getInstance(context).isEnabled();
        if (isEnable) {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext.getDrawable(R.drawable.torch);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        } else {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext.
                        getDrawable(R.drawable.ic_signal_flashlight_disable);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        }
    }

    @Override
    public void onDelete(Context context) {
        try {
            FlashlightController.getInstance(context).removeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        boolean isEnable = FlashlightController.getInstance(context).isEnabled();
        if (isEnable) {
            FlashlightController.getInstance(context).setFlashlight(false);
        } else {
            FlashlightController.getInstance(context).setFlashlight(true);
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void onFlashlightChanged(boolean enabled) {
        if (enabled) {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext.getDrawable(R.drawable.torch);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        } else {
            if (iconView != null) {
                VectorDrawable vd = (VectorDrawable) mContext.
                        getDrawable(R.drawable.ic_signal_flashlight_disable);
                vd.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                vd.setTint(Color.WHITE);
                iconView.setImageDrawable(vd);
            }
        }
    }

    @Override
    public void onFlashlightError() {

    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {

    }
}
