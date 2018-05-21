package com.kuding.nowkey;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.R;
import com.kuding.nowkey.service.NowKeyService;

import java.util.List;

/**
 * Created by user on 17-2-7.
 */

public class BootReceiver extends BroadcastReceiver {
    static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //add by yangzhong.gong for task-4596342 begin
        String action = intent.getAction();//add by yangzhong.gong for task-4584915
        Log.d(TAG, "anxi onReceive:action = " + action);
        if ("com.tct.setupwizard.action.SETUP_WIZARD_FINISHED".equals(action)) {
            boolean mNowKeyEnable = context.getResources().getBoolean(R.bool.def_NowKey_enable_now_key);
            PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, mNowKeyEnable);
        }

        //add by yangzhong.gong for task-4596342 end
        boolean show = PreferenceUtils.isShowNowKey(false);
        if (show && !isServiceWork(context, "NowKeyService")) {
            Intent service = new Intent(context, NowKeyService.class);
            service.setAction(NowKeyService.START_NOW_KEY);
            context.startService(service);
        }

        if ("android.intent.action.NOWKEY_NAV_BROADCAST".equals(action)) {//add by yangzhong.gong for task-4584915
            if (!isServiceWork(context, "NowKeyService")) {
                PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, true);
                Intent service = new Intent(context, NowKeyService.class);
                service.setAction(NowKeyService.START_NOW_KEY);
                context.startService(service);
            } else {
                PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
            }
        }
    }

    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
