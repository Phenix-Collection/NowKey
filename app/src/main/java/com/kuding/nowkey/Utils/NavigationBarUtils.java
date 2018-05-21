package com.kuding.nowkey.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by user on 5/3/17.
 */
public class NavigationBarUtils {

    static final String TAG = "NavigationBarUtils";

    // add by junye.li for no drawer begin
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class<?> systemPropertiesClass = Class
                    .forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass,
                    "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }

        } catch (Exception e) {
            Log.e(TAG, "checkDeviceHasNavigationBar ", e);
        }

        Object service = context.getSystemService("statusbar");
        if (null == service)
            return hasNavigationBar;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            Method expand = clazz.getMethod("getNavigationBarState");
            expand.setAccessible(true);
            hasNavigationBar = (boolean) expand.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasNavigationBar;
    }


    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    public static int getNavigationBarHeightDirectly(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    public static int getStatusBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }
    // add by junye.li for no drawer end

}
