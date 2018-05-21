package com.kuding.nowkey.functions.recent;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by user on 17-1-25.
 */

public class RecentService extends AccessibilityService {

    public static final String RUN_RECENT = "run_recent";
    public static final String RUN_BACK = "run_back";
    public static final String RUN_SPLIT_SCREEN = "run_split_screen";


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RUN_RECENT.equals(action)) {
                performGlobalAction(GLOBAL_ACTION_RECENTS);
            } else if (RUN_BACK.equals(action)) {
                performGlobalAction(GLOBAL_ACTION_BACK);
            } else if (RUN_SPLIT_SCREEN.equals(action)) {
                performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        //指定包名
        accessibilityServiceInfo.packageNames = new String[]{getPackageName()};
        //指定事件类型
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED
                | AccessibilityEvent.TYPE_VIEW_FOCUSED;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        accessibilityServiceInfo.notificationTimeout = 100;
        setServiceInfo(accessibilityServiceInfo);

        IntentFilter filter = new IntentFilter();
        filter.addAction(RUN_RECENT);
        filter.addAction(RUN_BACK);
        filter.addAction(RUN_SPLIT_SCREEN);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
