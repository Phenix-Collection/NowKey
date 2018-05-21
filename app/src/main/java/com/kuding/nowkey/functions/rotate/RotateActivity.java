package com.kuding.nowkey.functions.rotate;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.kuding.nowkey.R;

/**
 * Created by user on 17-2-13.
 */

public class RotateActivity extends Activity {
    private static final int REQUEST_CODE_WRITE_SETTINGS = 111;

    private boolean hasRun = false;

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
        if (!hasRun) {
            hasRun = true;
            checkPermission();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!hasRun) {
            hasRun = true;
            checkPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasRun = false;
    }

    private void checkPermission() {
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
        } else {
            //有了权限，具体的动作
            if (getRotationStatus(this) == 1) {
                setRotationStatus(getContentResolver(), 0);
            } else {
                setRotationStatus(getContentResolver(), 1);
            }
            finish();
        }
    }

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

    private void setRotationStatus(ContentResolver resolver, int status) {
        //得到uri
        Uri uri = android.provider.Settings.System.getUriFor("accelerometer_rotation");
        //沟通设置status的值改变屏幕旋转设置
        android.provider.Settings.System.putInt(resolver, "accelerometer_rotation", status);
        //通知改变
        resolver.notifyChange(uri, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (!Settings.System.canWrite(this)) {
                finish();
            } else {
                //有了权限，具体的动作
                if (getRotationStatus(this) == 1) {
                    setRotationStatus(getContentResolver(), 0);
                } else {
                    setRotationStatus(getContentResolver(), 1);
                }
                finish();
            }
        }
    }
}
