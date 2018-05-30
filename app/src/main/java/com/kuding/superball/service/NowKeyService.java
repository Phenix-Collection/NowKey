package com.kuding.superball.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.FunctionUtils;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.Utils.Utils;
import com.kuding.superball.floatview.FloatingBallController;
import com.kuding.superball.functions.sound.FunctionSoundMuteActivity;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.info.FunctionItemInfo;
import com.kuding.superball.interfaces.OnFloatIconClickListener;
import com.kuding.superball.interfaces.OnFloatIconUpdateListener;
import com.kuding.superball.interfaces.OnMenuItemClickListener;
import com.kuding.superball.floatview.MiniBallController;

public class NowKeyService extends Service implements OnFloatIconClickListener, OnMenuItemClickListener, OnFloatIconUpdateListener {

    private static final String TAG = "NowKeyService";

    // 服务的操作
    public static final String STOP_NOW_KEY = "action.stop.now.key";                                      // 停止服务
    public static final String START_NOW_KEY = "start_now_key";                                           // 启动Nowkey服务
    public static final String SHOW_NOWKEY_PANEL = "show_nowkey_panel";                                   // 点击悬浮球
    public static final String HIDE_NOWEY_FLOATBALL = "hide_nowey_floatball";                             // 隐藏悬浮球（用于外部程序截屏之类的操作）
    public static final String SHOW_NOWEY_FLOATBALL = "show_nowey_floatball";                             // 恢复悬浮球(和HIDE_NOWEY_FLOATBALL 对应使用

    static final String ACTION_FULL_SCREEN = "android.intent.action.ACTION_FULL_SCREEN_TRIGGER";          // framwork层 发送的全屏广播
    static final String ACTION_NOT_FULL_SCREEN = "android.intent.action.ACTION_NOT_FULL_SCREEN_TRIGGER";  // framwork层 发送的非全屏广播
    public static final String ACTION_CHANGE_MODE = "com.tct.onetouchbooster.CHANGE_POWER_SAVER_MODE";

    private static final int HIDE_PANEL = 100;
    private static final int HIDE_PANEL_DELAY = 3000;

    // Normal 模式 变量
    private boolean mShowingNormalBall;
    private boolean mShowingNormalPanel;

    // Mini 模式 变量
    private boolean mShowingMiniBall;
    private boolean mShowingMiniPanel;

    private boolean mIsMiniMode = true;
    private boolean mIsShowNowKey = false;

    private NowKeyReceiver mReceiver;
    private PackageReceiver mPkgReceiver;
    private int mOrientation;

    private boolean mNowKeyEnable;//add by yangzhong.gong for task-4584915
    private boolean mIsFullScreen = false;  // 用于标记是否捕获了 全屏的广播
    private boolean mIsSuperMode = false;

    private Vibrator mVibrator = null;   // 振动
    public static boolean onDestroying = true;//add by yangzhong.gong for defect-5055624

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_PANEL:
                    if (mShowingNormalPanel) {
                        mShowingNormalPanel = false;
                        mShowingNormalBall = true;
                        FloatingBallController.getController(getApplication()).hidePanel();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FloatingBallController.getController(getApplication()).hideHalf();
                            }
                        }, 2000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mReceiver = new NowKeyReceiver();
        mOrientation = getResources().getConfiguration().orientation;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_THEME_CHANGE);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(ACTION_FULL_SCREEN);
        filter.addAction(ACTION_NOT_FULL_SCREEN);
        filter.addAction(ACTION_CHANGE_MODE);
        registerReceiver(mReceiver, filter);

        mPkgReceiver = new PackageReceiver();
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pkgFilter.addDataScheme("package");
        registerReceiver(mPkgReceiver, pkgFilter);
        //add by yangzhong.gong for task-4584915 begin
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor("low_storage"),
                false, mNowKeyBallObserver);
        //add by yangzhong.gong for task-4584915 end

        mVibrator = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case START_NOW_KEY:
                    startNowKey();
                    break;
                case STOP_NOW_KEY:
                    stopSelf();
                    break;
                case SHOW_NOWKEY_PANEL:
                    onFloatIconClick();
                    break;
                case HIDE_NOWEY_FLOATBALL:
                    hideFloatBall();
                    break;
                case SHOW_NOWEY_FLOATBALL:
                    showFloatBall();
                    break;
            }
        } else if (intent == null) {
            // 服务异常被杀死的情况,intent 为 null
            mIsShowNowKey = PreferenceUtils.isShowNowKey(true);
            if (mIsShowNowKey) {
                startNowKey();
            }
        }
        return START_STICKY;
    }

    /**
     * 重新启动悬浮求
     */
    private synchronized void startNowKey() {
        onDestroying = false;//add by yangzhong.gong for defect-5055624
        mIsMiniMode = PreferenceUtils.isMiniMode(false);
        if (mIsMiniMode) {
            mShowingMiniBall = false;
            mShowingMiniPanel = false;
        } else {
            mShowingNormalBall = false;
            mShowingNormalPanel = false;
        }
        MiniBallController.getController(getApplication()).onDestroy();
        FloatingBallController.getController(getApplication()).onDestroy();
        if (mIsMiniMode) {
            createMiniBall();
        } else {
            createNormalBall();
        }
    }

    /**
     * 创建一个轻量级的悬浮球
     */
    private void createMiniBall() {
        Log.d(TAG, "createMiniBall");
        mIsMiniMode = true;
        mIsShowNowKey = PreferenceUtils.isShowNowKey(true);
        if (mIsShowNowKey && !mShowingMiniBall && !mShowingMiniPanel) {
            MiniBallController.getController(getApplication()).init();
            MiniBallController.getController(getApplication()).onThemeChange();
            MiniBallController.getController(getApplication()).setonFloatIconClickListener(this);
            MiniBallController.getController(getApplication()).setOnPanelClickListener(this);

            // 调用 showFloatBall() 显示悬浮球之前 先判断是否处于低内存的情况
            // 如果是低内存  ：只初始化，不显示悬浮球。这样只要等低内存的状态消失，就可以在 mNowKeyBallObserver 中监听到，并且显示悬浮球
            // 如果不是低内存 ：初始化之后，直接显示悬浮球。
            boolean mIsLowStorage = Settings.Global.getInt(getContentResolver(), "low_storage", 0) == 1;
            if (!mIsLowStorage) {
                MiniBallController.getController(getApplication()).showFloatBall();
                mShowingMiniBall = true;
                mShowingMiniPanel = false;
            } else {
                mShowingMiniBall = false;
                mShowingMiniPanel = false;
            }
        }
    }

    /**
     * 创建一个 普通模式 的悬浮球
     */
    private void createNormalBall() {
        Log.d(TAG, "anxi createNormalBall");
        mIsShowNowKey = PreferenceUtils.isShowNowKey(true);
        if (mIsShowNowKey && !mShowingNormalBall && !mShowingNormalPanel) {
            mIsMiniMode = false;
            mShowingNormalPanel = false;
            FloatingBallController.getController(getApplication()).init();
            FloatingBallController.getController(getApplication()).onThemeChange();
            FloatingBallController.getController(getApplication()).setonFloatIconClickListener(this);
            FloatingBallController.getController(getApplication()).setOnPanelClickListener(this);
            FloatingBallController.getController(getApplication()).setOnFloatIconUpdateListener(this);

            // 调用 showFloatBall() 显示悬浮球之前 先判断是否处于低内存的情况
            // 如果是低内存  ：只初始化，不显示悬浮球。这样只要等低内存的状态消失，就可以在 mNowKeyBallObserver 中监听到，并且显示悬浮球
            // 如果不是低内存 ：初始化之后，直接显示悬浮球。
            boolean mIsLowStorage = Settings.Global.getInt(getContentResolver(), "low_storage", 0) == 1;
            if (!mIsLowStorage) {
                FloatingBallController.getController(getApplication()).showFloatBall();
                mShowingNormalBall = true;
                mShowingNormalPanel = false;
            } else {
                mShowingNormalBall = false;
                mShowingNormalPanel = false;
            }
        }
    }

    /**
     * 低内存状态的监听
     */
    private ContentObserver mNowKeyBallObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean mIsLowStorage = Settings.Global.getInt(getContentResolver(), "low_storage", 0) == 1;
            if (mIsLowStorage) {
                if (mIsMiniMode) {
                    if (mShowingMiniBall || mShowingMiniPanel) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mShowingMiniBall = false;
                                mShowingMiniPanel = false;
                                MiniBallController.getController(getApplication()).removeAllViews();
                            }
                        });
                    }
                } else {
                    if (mShowingNormalBall || mShowingNormalPanel) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mShowingNormalBall = false;
                                mShowingNormalPanel = false;
                                FloatingBallController.getController(getApplication()).removeAllViews();
                            }
                        });
                    }
                }

            } else {
                if (mIsMiniMode) {
                    boolean isShow = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
                    if (isShow && !mShowingMiniPanel && !mShowingMiniBall) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mShowingMiniBall = true;
                                MiniBallController.getController(getApplication()).showFloatBall();
                            }
                        });
                    }
                } else {
                    boolean showFloatingBall = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
                    if (showFloatingBall && !mShowingNormalBall && !mShowingNormalPanel) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mShowingNormalBall = true;
                                FloatingBallController.getController(getApplication()).showFloatBall();
                            }
                        });
                    }
                }
            }
        }
    };

    /**
     * 隐藏悬浮球 当外部应用截屏之类操作的时候，暂时隐藏悬浮球
     */
    private synchronized void hideFloatBall() {
        Log.d(TAG, "anxi hideFloatBall()");
        if (mIsMiniMode) {
            if (mShowingMiniBall || mShowingMiniPanel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mShowingMiniBall = false;
                        mShowingMiniPanel = false;
                        MiniBallController.getController(getApplication()).removeAllViews();
                    }
                });
            }
        } else {
            if (mShowingNormalBall || mShowingNormalPanel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mShowingNormalBall = false;
                        mShowingNormalPanel = false;
                        FloatingBallController.getController(getApplication()).removeAllViews();
                    }
                });
            }
        }
    }

    /**
     * 恢复悬浮球 与 hideFloatBall 对应使用
     */
    private synchronized void showFloatBall() {
        Log.d(TAG, "anxi showFloatBall()");
        if (mIsMiniMode) {
            boolean isShow = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
            if (isShow && !mShowingMiniPanel && !mShowingMiniBall) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mShowingMiniBall = true;
                        MiniBallController.getController(getApplication()).showFloatBall();
                    }
                });
            }
        } else {
            boolean showFloatingBall = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
            if (showFloatingBall && !mShowingNormalBall && !mShowingNormalPanel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mShowingNormalBall = true;
                        FloatingBallController.getController(getApplication()).showFloatBall();
                    }
                });
            }
        }
    }

    public void onDestroy() {
        Log.d(TAG, "anxi onDestroy");
        super.onDestroy();
        onDestroying = true;//add by yangzhong.gong for defect-5055624
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(mPkgReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //add by yangzhong.gong for task-4584915 begin
        try {
            getContentResolver().unregisterContentObserver(mNowKeyBallObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //add by yangzhong.gong for task-4584915 end

        mShowingNormalBall = false;
        mShowingNormalPanel = false;

        mShowingMiniBall = false;
        mShowingMiniPanel = false;
        mIsFullScreen = false;
        mIsSuperMode = false;

        mVibrator.cancel();
        mVibrator = null;

        if (mIsMiniMode) {
            MiniBallController.getController(getApplication()).onDestroy();
        } else {
            FloatingBallController.getController(getApplication()).onDestroy();
        }
    }

    @Override
    public void onFloatIconClick() {
        Log.d(TAG, "anxi onFloatIconClick");
        if (mIsMiniMode) {
            if (mShowingMiniPanel && !MiniBallController.getController(getApplication()).isPanelFling()) {
                mShowingMiniPanel = false;
                mShowingMiniBall = true;
                MiniBallController.getController(getApplication()).hidePanel();
            } else if (!mShowingMiniPanel) {
                mShowingMiniPanel = true;
                mShowingMiniBall = true;
                MiniBallController.getController(getApplication()).showPanel();
            }
        } else {
            mShowingNormalPanel = true;
            mShowingNormalBall = false;
            FloatingBallController.getController(getApplication()).showPanel();
            if (mHandler != null) {
                if (mHandler.hasMessages(HIDE_PANEL)) {
                    mHandler.removeMessages(HIDE_PANEL);
                }
                mHandler.sendEmptyMessageDelayed(HIDE_PANEL, HIDE_PANEL_DELAY);
            }
        }
    }

    @Override
    public void onFloatIconPress() {
        Log.d(TAG, "anxi onFloatIconPress");
        int type = Constant.NOW_KEY_ITEM_TYPE_NONE;
        String action = "";
        if (mIsMiniMode) {
            // 从配置文件读取 迷你模式 长按的动作
            type = PreferenceUtils.getMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_NONE);
            action = PreferenceUtils.getMiniGestureLongPressAction("");
        } else {
            // 从配置文件读取 普通模式 长按的动作
            type = PreferenceUtils.getNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_NONE);
            action = PreferenceUtils.getNormalGestureLongPressAction("");
        }

        if (type == Constant.NOW_KEY_ITEM_TYPE_NONE) {
            return;
        }

        // 振动一下
        vibrate();

        if (type == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
            if ("sound".equals(action)) {
                doSoundAction();
            } else if ("callacontact".equals(action)) {
                FunctionItemInfo info = FunctionUtils.getCallAContactFromGesture(this, 1);
                if (info != null) {
                    info.doAction(this, -1, -1);
                }
            } else {
                FunctionItemInfo info = FunctionUtils.functionFilter(this, action);
                if (info != null) {
                    info.doAction(this);
                }
            }
        } else {
            if (!"camera".equals(action)) {
                final PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intent = Utils.getApplicationIntent(action, packageManager);
                //add by yangzhong.gong ofr defect-4455564 begin
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                //add by yangzhong.gong ofr defect-4455564 end
            }
        }
    }

    @Override
    public void onFloatIconShortDrag() {
        int type = Constant.NOW_KEY_ITEM_TYPE_NONE;
        String action = "";
        if (mIsMiniMode) {
            // 从配置文件读取 迷你模式 双击的动作
            type = PreferenceUtils.getMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_NONE);
            action = PreferenceUtils.getMiniGestureShortDragAction("");
            Log.d(TAG, " type = " + type);
            Log.d(TAG, " action = " + action);
        } else {
            // 从配置文件读取 普通模式 双击的动作
            type = PreferenceUtils.getNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_NONE);
            action = PreferenceUtils.getNormalGestureShortDragAction("");
            Log.d(TAG, " type = " + type);
            Log.d(TAG, " action = " + action);
        }

        if (type == Constant.NOW_KEY_ITEM_TYPE_NONE) {
            return;
        }

        // 振动一下
        vibrate();

        if (type == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
            if ("sound".equals(action)) {
                doSoundAction();
            } else if ("callacontact".equals(action)) {
                FunctionItemInfo info = FunctionUtils.getCallAContactFromGesture(this, 2);
                if (info != null) {
                    info.doAction(this, -1, -1);
                }
            } else {
                FunctionItemInfo info = FunctionUtils.functionFilter(this, action);
                if (info != null) {
                    info.doAction(this);
                }
            }
        } else {
            if (!"recent".equals(action)) {
                final PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intent = Utils.getApplicationIntent(action, packageManager);
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 振动
     */
    private void vibrate() {
        if (mVibrator != null) {
            mVibrator.vibrate(100);
        }
    }

    @Override
    public void onFloatIconDoubleClick() {
        Log.d(TAG, "anxi onFloatIconDoubleClick");
        int type = Constant.NOW_KEY_ITEM_TYPE_NONE;
        String action = "";
        if (mIsMiniMode) {
            // 从配置文件读取 迷你模式 双击的动作
            if (!mShowingMiniPanel) {
                type = PreferenceUtils.getMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                action = PreferenceUtils.getMiniGestureDoubleClickAction("");
                Log.d(TAG, " type = " + type);
                Log.d(TAG, " action = " + action);
            }
        } else {
            // 从配置文件读取 普通模式 双击的动作
            type = PreferenceUtils.getNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_NONE);
            action = PreferenceUtils.getNormalGestureDoubleClickAction("");
            Log.d(TAG, " type = " + type);
            Log.d(TAG, " action = " + action);
        }

        if (type == Constant.NOW_KEY_ITEM_TYPE_NONE) {
            return;
        }

        // 振动一下
        vibrate();

        if (type == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
            if ("sound".equals(action)) {
                doSoundAction();
            } else if ("callacontact".equals(action)) {
                FunctionItemInfo info = FunctionUtils.getCallAContactFromGesture(this, 0);
                if (info != null) {
                    info.doAction(this, -1, -1);
                }
            } else {
                FunctionItemInfo info = FunctionUtils.functionFilter(this, action);
                if (info != null) {
                    info.doAction(this);
                }
            }
        } else {
            if (!"recent".equals(action)) {
                final PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intent = Utils.getApplicationIntent(action, packageManager);
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }

    private void doSoundAction() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringMode = audioManager.getRingerMode();
        switch (ringMode) {
            case AudioManager.RINGER_MODE_SILENT:
                NotificationManager nof = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nof.isNotificationPolicyAccessGranted()) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this, FunctionSoundMuteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (notificationManager.isNotificationPolicyAccessGranted()) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(this, FunctionSoundMuteActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFloatOutsideClick() {
        if (mIsMiniMode) {
            if (mShowingMiniPanel && !MiniBallController.getController(getApplication()).isPanelFling()) {
                mShowingMiniPanel = false;
                mShowingMiniBall = true;
                MiniBallController.getController(getApplication()).hidePanel();
            }
        } else {
            mShowingNormalBall = true;
            mShowingNormalPanel = false;
            FloatingBallController.getController(getApplication()).hidePanel();
        }
    }

    @Override
    public void onConfirmDialogClick() {

    }

    @Override
    public void itemClick() {
        if (mIsMiniMode) {
            mShowingMiniPanel = false;
            mShowingMiniBall = true;
            MiniBallController.getController(getApplication()).hidePanel();
        } else {
            mShowingNormalPanel = false;
            mShowingNormalBall = true;
            FloatingBallController.getController(getApplication()).hidePanel();
        }
    }

    @Override
    public void onItemLongClick() {
        FloatingBallController.getController(getApplication()).showPanelEdit();
    }

    @Override
    public boolean showWarning() {
        return false;
    }

    @Override
    public void panelClick() {
        if (mIsMiniMode) {
            mShowingMiniPanel = false;
            mShowingMiniBall = true;
            MiniBallController.getController(getApplication()).hidePanel();
        } else {
            mShowingNormalPanel = false;
            mShowingNormalBall = true;
            boolean editing = FloatingBallController.getController(getApplication()).isPanelEdit();
            if (editing) {
                FloatingBallController.getController(getApplication()).exitPanelEdit();
            } else {
                FloatingBallController.getController(getApplication()).hidePanel();
            }
        }
    }

    @Override
    public void addItemClick() {
        Log.d(TAG, "addItemClick");
        mShowingNormalPanel = false;
        mShowingNormalBall = false;
        FloatingBallController.getController(getApplication()).removeAllViews();
    }

    @Override
    public void startTouching() {
        if (mHandler != null) {
            if (mHandler.hasMessages(HIDE_PANEL)) {
                mHandler.removeMessages(HIDE_PANEL);
            }
        }
    }

    @Override
    public void finishTouching() {
        if (mHandler != null) {
            if (mHandler.hasMessages(HIDE_PANEL)) {
                mHandler.removeMessages(HIDE_PANEL);
            }
            mHandler.sendEmptyMessageDelayed(HIDE_PANEL, HIDE_PANEL_DELAY);
        }
    }

    @Override
    public void onFloatFinishUpdate(boolean external) {
        if (!external) {
            mShowingNormalPanel = true;
            mShowingNormalBall = false;
            FloatingBallController.getController(getApplication()).showPanel();
            if (mHandler != null) {
                if (mHandler.hasMessages(HIDE_PANEL)) {
                    mHandler.removeMessages(HIDE_PANEL);
                }
                mHandler.sendEmptyMessageDelayed(HIDE_PANEL, HIDE_PANEL_DELAY);
            }
        }
    }

    @Override
    public void onFloatFinishDeleteAll() {
        mShowingNormalPanel = false;
        mShowingNormalBall = false;
        FloatingBallController.getController(getApplication()).removeAllViews();
        PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
    }

    class NowKeyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "anxi action = " + action);
            if (Constant.BROADCAST_THEME_CHANGE.equals(action)) {
                if (mIsMiniMode) {
                    MiniBallController.getController(getApplication()).onThemeChange();
                } else {
                    FloatingBallController.getController(getApplication()).onThemeChange();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if (mIsMiniMode) {
                    if (mShowingMiniPanel) {
                        MiniBallController.getController(getApplication()).hidePanel();
                    }
                    mShowingMiniBall = true;
                    mShowingMiniPanel = false;
                } else {
                    if (mShowingNormalPanel) {
                        FloatingBallController.getController(getApplication()).hidePanel();
                    }
                    mShowingNormalPanel = false;
                    mShowingNormalBall = true;
                }
            } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                if (mIsMiniMode) {
                    return;
                } else {
                    FloatingBallController.getController(getApplication()).onLocaleChanged();
                }
            } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                // 屏幕旋转触发
                if (mIsMiniMode) {
                    if (mOrientation != getResources().getConfiguration().orientation) {
                        mOrientation = getResources().getConfiguration().orientation;
                        if(!mIsFullScreen) {//add by yangzhong.gong for defect-5586528
                            mShowingMiniBall = true;
                        }
                        if (mShowingMiniPanel) {
                            mShowingMiniPanel = false;
                            MiniBallController.getController(getApplication()).hidePanel();
                        }
                        MiniBallController.getController(getApplication()).onConfigChange(mOrientation);
                    }
                    return;
                } else {
                    if (mOrientation != getResources().getConfiguration().orientation) {
                        mOrientation = getResources().getConfiguration().orientation;
                        if(!mIsFullScreen) {//add by yangzhong.gong for defect-5586528
                            mShowingNormalBall = true;
                        }
                        if (mShowingNormalPanel) {
                            mShowingNormalPanel = false;
                            FloatingBallController.getController(getApplication()).hidePanel();
                        }
                        FloatingBallController.getController(getApplication()).onConfigChange();
                    }
                }
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                // 当用户点击 Home键 或者 Recent 键的时候会触发，触发的时候会 隐藏转盘，如果转盘存在的话。
                if (mIsMiniMode) {
                    if (mShowingMiniPanel) {
                        mShowingMiniPanel = false;
                        mShowingMiniBall = true;
                        MiniBallController.getController(getApplication()).hidePanel();
                    }
                    return;
                } else {
                    if (mShowingNormalPanel) {
                        if (mShowingNormalPanel) {
                            mShowingNormalPanel = false;
                            mShowingNormalBall = true;
                            FloatingBallController.getController(getApplication()).hidePanel();
                        }
                    }
                }
            } else if (ACTION_FULL_SCREEN.equals(action)) {
                // 接受到了全屏的广播 隐藏悬浮球
                mIsFullScreen = true;
                if (mIsMiniMode && (mShowingMiniBall || mShowingMiniPanel)) {
                    mShowingMiniBall = false;
                    mShowingMiniPanel = false;
                    MiniBallController.getController(getApplication()).removeAllViews();
                } else if (!mIsMiniMode && (mShowingNormalBall || mShowingNormalPanel)) {
                    mShowingNormalBall = false;
                    mShowingNormalPanel = false;
                    FloatingBallController.getController(getApplication()).removeAllViews();
                }
            } else if (ACTION_NOT_FULL_SCREEN.equals(action)) {
                // 接受到了非全屏的广播 显示悬浮球
                if (mIsFullScreen) {
                    mIsFullScreen = false;
                    mIsShowNowKey = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
                    if (mIsMiniMode) {
                        if (mIsShowNowKey && !mShowingMiniPanel && !mShowingMiniBall) {
                            mShowingMiniBall = true;
                            mShowingMiniPanel = false;
                            MiniBallController.getController(getApplication()).showFloatBall();
                        }
                    } else {
                        if (mIsShowNowKey && !mShowingNormalPanel && !mShowingNormalBall) {
                            mShowingNormalBall = true;
                            mShowingNormalPanel = false;
                            FloatingBallController.getController(getApplication()).showFloatBall();
                        }
                    }
                }
            } else if (ACTION_CHANGE_MODE.equals(action)) {
                String mode = intent.getStringExtra("newMode");
                if (mode.equals("super_mode")) {
                    mIsSuperMode = true;
                    if (mIsMiniMode && (mShowingMiniBall || mShowingMiniPanel)) {
                        mShowingMiniBall = false;
                        mShowingMiniPanel = false;
                        MiniBallController.getController(getApplication()).removeAllViews();
                    } else if (!mIsMiniMode && (mShowingNormalBall || mShowingNormalPanel)) {
                        mShowingNormalBall = false;
                        mShowingNormalPanel = false;
                        FloatingBallController.getController(getApplication()).removeAllViews();
                    }
                } else {
                    if (mIsSuperMode) {
                        mIsSuperMode = false;
                        mIsShowNowKey = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
                        if (mIsMiniMode) {
                            if (mIsShowNowKey && !mShowingMiniPanel && !mShowingMiniBall) {
                                mShowingMiniBall = true;
                                mShowingMiniPanel = false;
                                MiniBallController.getController(getApplication()).showFloatBall();
                            }
                        } else {
                            if (mIsShowNowKey && !mShowingNormalPanel && !mShowingNormalBall) {
                                mShowingNormalBall = true;
                                mShowingNormalPanel = false;
                                FloatingBallController.getController(getApplication()).showFloatBall();
                            }
                        }
                    }
                }
            }
        }
    }

    class PackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mIsMiniMode) {
                return;
            }
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                String packageName = intent.getDataString();
                packageName = packageName.substring(8, packageName.length());
                if (packageName == null) return;
                BaseItemInfo info = FloatingBallController.getController(getApplication()).
                        getItemFromPackage(packageName);
                if (info == null) return;
                FloatingBallController.getController(getApplication()).deleteItemExternal(info);
            }
        }
    }
}