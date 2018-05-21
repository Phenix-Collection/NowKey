package com.kuding.nowkey;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.info.BaseItemInfo;
import com.kuding.nowkey.R;
import com.kuding.nowkey.floatview.GestureController;

/**
 * Created by user on 17-1-24.
 */

public class NowKeyApplication extends Application {
    static final String TAG = "NowKeyApplication";

    private Bitmap mScreenCaptureBitmap;
    private BaseItemInfo mCurrentAction;
    private static final Object mSyncObject = new Object();

    private static NowKeyApplication instance;

    public NowKeyApplication() {
        super();
        instance = this;
    }

    /**
     * Singleton
     *
     * @return singleton Application instance
     */
    public static NowKeyApplication getInstance() {
        synchronized (mSyncObject) {
            if (null == instance) {
                instance = new NowKeyApplication();
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setFirstTimeValues();
    }

    public Bitmap getmScreenCaptureBitmap() {
        return mScreenCaptureBitmap;
    }

    public void setmScreenCaptureBitmap(Bitmap mScreenCaptureBitmap) {
        this.mScreenCaptureBitmap = mScreenCaptureBitmap;
    }

    public void setCurrentBaseItem(BaseItemInfo info) {
        mCurrentAction = info;
    }

    public BaseItemInfo getCurrentActionItem() {
        return mCurrentAction;
    }


    /**
     * 设置首次启动需要修改的值
     */
    private void setFirstTimeValues() {
        boolean isFirstTime = PreferenceUtils.isFirstTimeUseNowKey(this);
        if (isFirstTime) {
            // 设置默认手势
            if (GestureController.getInstance(this).resetGesture()) {
                Log.d(TAG, "initialize Gesture success!!!");
            } else {
                Log.e(TAG, "initialize Gesture fail!!!");
            }

            // 设置默认的nowkey 开关
            boolean isNowKeyEnable = this.getResources().getBoolean(R.bool.def_NowKey_enable_now_key);
            PreferenceUtils.isShowNowKey(isNowKeyEnable);

            // 设置默认的Nowkey 模式
            boolean isMiniMode = this.getResources().getBoolean(R.bool.def_NowKey_is_mini_mode);
            PreferenceUtils.setIsMiniMode(isMiniMode);

            PreferenceUtils.setIsFirstTimeUseNowKey(false);
        }
    }
}
