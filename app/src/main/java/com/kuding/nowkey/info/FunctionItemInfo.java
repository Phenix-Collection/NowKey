package com.kuding.nowkey.info;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.Utils.Utils;
import com.kuding.nowkey.ActionActivity;
import com.kuding.nowkey.NowKeyApplication;
import com.kuding.nowkey.floatview.FloatingBallController;
import com.kuding.nowkey.floatview.MiniBallController;
import com.kuding.nowkey.functions.recent.RecentActivity;
import com.kuding.nowkey.functions.screenshot.ScreenshotActivity;

import java.lang.reflect.Method;

/**
 * Created by user on 17-1-12.
 */

public class FunctionItemInfo extends BaseItemInfo {

    private static final String TAG = "FunctionItemInfo";

    private String text;
    private Drawable icon;
    private Intent intent;

    protected ImageView iconView;
    protected TextView titleView;

    protected int mIconImgHeight;
    protected int mIconImgWidth;
    private int flag;
    private static Context myContext;

    public FunctionItemInfo(Context context) {
        myContext = context;
        mIconImgHeight = Utils.dip2px(context, 48);
        mIconImgWidth = Utils.dip2px(context, 48);
    }

    public FunctionItemInfo(Context context, int flag) {
        mIconImgHeight = Utils.dip2px(context, 48);
        mIconImgWidth = Utils.dip2px(context, 48);
        this.flag = flag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIconView(ImageView iconView) {
        this.iconView = iconView;
    }

    public void setTitleView(TextView titleView) {
        this.titleView = titleView;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Drawable getIcon(Context context) {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean doAction(Context context) {
        return false;
    }

    public boolean doAction(Context context, int flag) {
        return false;
    }

    public boolean doAction(Context context, int index, int page) {
        return false;
    }

    public void onAdd(Context context) {

    }

    public void onDelete(Context context) {
        text = null;
        icon = null;
        intent = null;
        iconView = null;
        titleView = null;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    protected void startActivity(Context context, int extra) {
        if (extra <= 0) return;
        Intent intent = new Intent();
        intent.setClass(context, ActionActivity.class);
        intent.putExtra(ActionActivity.ACTION_EXTRA, extra);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
    }

    public static class FunctionNone extends FunctionItemInfo {

        public FunctionNone(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionHome extends FunctionItemInfo {

        public FunctionHome(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            if(Settings.Global.getInt(context.getContentResolver(), "low_storage", 0) == 0) {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setAction(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionRecent extends FunctionItemInfo {

        public FunctionRecent(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            if(Settings.Global.getInt(context.getContentResolver(), "low_storage", 0) == 0) {
//                final Instrumentation in = new Instrumentation();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            in.sendKeyDownUpSync(KeyEvent.KEYCODE_APP_SWITCH);
//                        } catch (SecurityException e) {
//                            e.printStackTrace();
//                            toggleRecents(c);
//                        }
//
//                    }
//                }).start();
                //return toggleRecents(context);
                toggleRecents(context);
                return true;
            }
            return false;
        }

        private boolean toggleRecents(Context context) {
            startActivity(context, ActionActivity.ACTION_RECENT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionBack extends FunctionItemInfo {

        public FunctionBack(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            return toggleBack(context);
        }

        private boolean toggleBack(Context context) {
            //startActivity(context, ActionActivity.ACTION_BACK);

            //doBackByAccessibility(context);

            startActivity(context, ActionActivity.ACTION_BACK);

            //doBackByReflect();
//            final Instrumentation in = new Instrumentation();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(100);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    in.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
//                }
//            }).start();

            return true;
        }

        // 通过 Accessibility 实现返回
        private void doBackByAccessibility(Context context) {
            Intent intent = new Intent();

            intent.putExtra("recent_extra", 2);
            intent.setClass(context, RecentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // 通过反射 实现返回
        private void doBackByReflect() {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
//                    KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
                    final int repeatCount = (KeyEvent.FLAG_VIRTUAL_HARD_KEY & KeyEvent.FLAG_LONG_PRESS) != 0 ? 1 : 0;
                    final KeyEvent evDown = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, repeatCount, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
                            0, KeyEvent.FLAG_VIRTUAL_HARD_KEY | KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                            InputDevice.SOURCE_KEYBOARD);

                    final KeyEvent evUp = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, repeatCount, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                            KeyEvent.FLAG_VIRTUAL_HARD_KEY | KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                            InputDevice.SOURCE_KEYBOARD);

                    Class<?> ClassInputManager;
                    try {
                        ClassInputManager = Class.forName("android.hardware.input.InputManager");
                        Method[] methods = ClassInputManager.getMethods();
                        //System.out.println("cchen " + Arrays.toString(methods));
                        Method methodInjectInputEvent = null;
                        Method methodGetInstance = null;
                        for (Method method : methods) {
                            //System.out.println("cchen " + method.getName());
                            if (method.getName().contains("getInstance")) {
                                methodGetInstance = method;
                            }
                            if (method.getName().contains("injectInputEvent")) {
                                methodInjectInputEvent = method;
                            }
                        }
                        Object instance = methodGetInstance.invoke(ClassInputManager, null);
                        //boolean bool = InputManager.class.isInstance(instance);
                        // methodInjectInputEvent =
                        // InputManager.getMethod("injectInputEvent",
                        // KeyEvent.class, Integer.class);
                        methodInjectInputEvent.invoke(instance, evDown, 0);
                        methodInjectInputEvent.invoke(instance, evUp, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    public static class FunctionSplitScreen extends FunctionItemInfo {

        public FunctionSplitScreen(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            return toggleSplitScreen(context);
        }

        private boolean toggleSplitScreen(Context context) {
            Intent intent = new Intent();

            intent.putExtra("recent_extra", 3);
            intent.setClass(context, RecentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            //startActivity(context, ActionActivity.ACTION_SPLIT_SCREEN);

            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionNetwork extends FunctionItemInfo {
        public FunctionNetwork(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_NETWORK);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    public static class FunctionGps extends FunctionItemInfo {
        public FunctionGps(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_GPS);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionSettings extends FunctionItemInfo {
        public FunctionSettings(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_SETTINGS);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

//    public static class FunctionSound extends FunctionItemInfo {
//        public FunctionSound(Context context) {
//            super(context);
//        }
//
//        @Override
//        public boolean doAction(Context context) {
//            startActivity(context, ActionActivity.ACTION_SOUND);
//            return true;
//        }
//    }

    public static class FunctionLock extends FunctionItemInfo {
        public FunctionLock(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            PowerManager mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            Class<?> ClassInputManager;
            try {
                ClassInputManager = Class.forName("android.os.PowerManager");
                Method method = ClassInputManager.getMethod("goToSleep", long.class);
                method.setAccessible(true);
                method.invoke(mPM, SystemClock.uptimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
                startActivity(context, ActionActivity.ACTION_LOCK);
            }

            //startActivity(context, ActionActivity.ACTION_LOCK);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionNotification extends FunctionItemInfo {
        public FunctionNotification(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            return expandNotification(context);
        }

        private boolean expandNotification(Context context) {
            Object service = context.getSystemService("statusbar");
            if (null == service)
                return false;
            try {
                Class<?> clazz = Class.forName("android.app.StatusBarManager");
                int sdkVersion = android.os.Build.VERSION.SDK_INT;
                Method expand = null;
                if (sdkVersion <= 16) {
                    expand = clazz.getMethod("expand");
                } else {
          /*
           * Android SDK 16之后的版本展开通知栏有两个接口可以处理
           * expandNotificationsPanel()
           * expandSettingsPanel()
           */
                    //expand =clazz.getMethod("expandNotificationsPanel");
                    expand = clazz.getMethod("expandNotificationsPanel");
                }
                expand.setAccessible(true);
                expand.invoke(service);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionDisplay extends FunctionItemInfo {
        public FunctionDisplay(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_DISPLAY);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionSync extends FunctionItemInfo {
        public FunctionSync(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_SYNC);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionScreenshot extends FunctionItemInfo {
        public FunctionScreenshot(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            if (Utils.checkPackageExist(context, "com.tcl.screenshotex")) {
                final Boolean mIsMiniMode = PreferenceUtils.isMiniMode(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Handler handler = new Handler(myContext.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(mIsMiniMode) {
                                        MiniBallController.getController(NowKeyApplication.getInstance()).removeAllViews();
                                    } else {
                                        FloatingBallController.getController(NowKeyApplication.getInstance()).removeAllViews();
                                    }
                                }
                            });
                            Thread.sleep(50);
                            Intent i = new Intent();
                            i.setAction("android.intent.action.MAIN");
                            i.setPackage("com.tcl.screenshotex");
                            NowKeyApplication.getInstance().startService(i);
                            Thread.sleep(200);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(mIsMiniMode) {
                                        MiniBallController.getController(NowKeyApplication.getInstance()).showFloatBall();
                                    } else {
                                        FloatingBallController.getController(NowKeyApplication.getInstance()).showFloatBall();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }).start();

            } else {
                Intent i = new Intent();
                i.setClass(context, ScreenshotActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
//            startActivity(context, ActionActivity.ACTION_SCREEN_SHOT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionAlarm extends FunctionItemInfo {
        public FunctionAlarm(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_ALARM);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionCamera extends FunctionItemInfo {
        public FunctionCamera(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_CAMERA);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionNSettings extends FunctionItemInfo {
        public FunctionNSettings(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_N_SETTINGS);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionCalculator extends FunctionItemInfo {
        public FunctionCalculator(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_CALCULATOR);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionCalendar extends FunctionItemInfo {
        public FunctionCalendar(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_CALENDAR);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionCall extends FunctionItemInfo {
        public FunctionCall(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_CALL);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionChat extends FunctionItemInfo {
        public FunctionChat(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_CHAT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionRecentContact extends FunctionItemInfo {
        public FunctionRecentContact(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_RECENT_CONTACT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionContact extends FunctionItemInfo {
        public FunctionContact(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_CONTACT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionSelfie extends FunctionItemInfo {
        public FunctionSelfie(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_SELFIE);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionVideo extends FunctionItemInfo {
        public FunctionVideo(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_VIDEO);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionGoogleVoice extends FunctionItemInfo {
        public FunctionGoogleVoice(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_GOOGLE_VOICE);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionEvent extends FunctionItemInfo {
        public FunctionEvent(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_EVENT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionEmail extends FunctionItemInfo {
        public FunctionEmail(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_EMAIL);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionPlayList extends FunctionItemInfo {
        public FunctionPlayList(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_PLAY_LIST);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionRecogniseSongs extends FunctionItemInfo {
        public FunctionRecogniseSongs(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_RECOGNISE_SONGS);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionNavigationHome extends FunctionItemInfo {
        public FunctionNavigationHome(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_NAVIGATION_HOME);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionTimer extends FunctionItemInfo {
        public FunctionTimer(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_TIMER);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionRecoder extends FunctionItemInfo {
        public FunctionRecoder(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_SOUND_RECORDER);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionCallAContact extends FunctionItemInfo {
        int actionType = -1;

        public FunctionCallAContact(Context context) {
            super(context);
        }

        public void setActionType(int action) {
            actionType = action;
        }

        @Override
        public boolean doAction(Context context, int index, int page) {
            Intent intent = new Intent();
            intent.setClass(context, ActionActivity.class);
            intent.putExtra(ActionActivity.ACTION_EXTRA, ActionActivity.ACTION_CALL_A_CONTACT);
            intent.putExtra(ActionActivity.ACTION_EXTRA_INDEX, index);
            intent.putExtra(ActionActivity.ACTION_EXTRA_PAGE, page);
            intent.putExtra(ActionActivity.ACTION_EXTRA_ACTION, actionType);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionGallery extends FunctionItemInfo {
        public FunctionGallery(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_GALLERY);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionAddAContact extends FunctionItemInfo {
        public FunctionAddAContact(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_ADD_A_CONTACT);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionColorCatcher extends FunctionItemInfo {
        public FunctionColorCatcher(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_COLORCATCHER);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class FunctionBooster extends FunctionItemInfo {
        public FunctionBooster(Context context) {
            super(context);
        }

        @Override
        public boolean doAction(Context context) {
            startActivity(context, ActionActivity.ACTION_BOOSTER);
            return true;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}

