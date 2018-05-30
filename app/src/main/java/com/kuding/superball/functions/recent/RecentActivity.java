package com.kuding.superball.functions.recent;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import com.kuding.superball.R;
import com.kuding.superball.floatview.CustomDialog;

import java.util.List;

/**
 * Created by user on 17-1-25.
 */

public class RecentActivity extends Activity {

    private boolean hasRun = false;
    private int flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.empty_layout);
        Intent intent = getIntent();
        if (intent != null) {
            flag = intent.getIntExtra("recent_extra", 1);
        }
        if (!hasRun) {
            hasRun = true;

            if (!serviceIsRunning()) {
                startAccessibilityService();
            } else {
                Intent intentRc = new Intent();
                if (flag == 1) {
                    intentRc.setAction(RecentService.RUN_RECENT);
                } else if (flag == 2) {
                    intentRc.setAction(RecentService.RUN_BACK);
                } else if (flag == 3) {
                    intentRc.setAction(RecentService.RUN_SPLIT_SCREEN);
                }
                sendBroadcast(intentRc);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            flag = intent.getIntExtra("recent_extra", 1);
        }
        if (!hasRun) {
            hasRun = true;
            if (!serviceIsRunning()) {
                startAccessibilityService();
            } else {
                Intent i = new Intent();
                if (flag == 1) {
                    i.setAction(RecentService.RUN_RECENT);
                } else if (flag == 2) {
                    i.setAction(RecentService.RUN_BACK);
                } else if (flag == 3) {
                    i.setAction(RecentService.RUN_SPLIT_SCREEN);
                }
                sendBroadcast(i);
                finish();
            }
        }
    }

    private boolean serviceIsRunning() {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals("com.kuding.superball/.functions.recent.RecentService")) {
                return true;
            }
        }
        return false;
    }

    private void startAccessibilityService() {

        // 隐式调用系统设置界面
        CustomDialog.Builder dialog = new CustomDialog.Builder(this);
        CustomDialog d =
                dialog.setTitle(R.string.accessibilityservice_dialog_title)
                        .setTitle(R.string.accessibilityservice_dialog_title)
                        .setMessage(R.string.accessibilityservice_dialog_content)
                        .setPositiveButton(R.string.accessibilityservice_dialog_button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton(R.string.accessibilityservice_dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        d.setCanceledOnTouchOutside(false);
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        d.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasRun = false;
    }
}
