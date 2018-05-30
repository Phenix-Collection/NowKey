package com.kuding.superball.floatview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuding.superball.Utils.BaseModelComparator;
import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.GeometryUtil;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.Utils.Utils;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.interfaces.OnFloatIconClickListener;
import com.kuding.superball.interfaces.OnMenuItemClickListener;
import com.kuding.superball.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/**
 * NowKey Mini 模式 的 控制器
 */
public class MiniBallController implements
        View.OnTouchListener, NowKeyPanelModel.NowKeyModelCallback, MiniBallPanel.OnPanelAnimationListener {
    private static final String TAG = "MiniBallController";
    private static MiniBallController sInstance;

    private static final long CLICK_SPACING_TIME = 200;     // 点击悬浮求，触发点击事件的延迟时间
    private static final long LONG_PRESS_TIME = 800;        // 长按悬浮求，触发长按事件的时间
    private static final long SHORT_DRAG_TIME = 300;        // 短拉触发的限定时间
    private static final long SHOW_INTRODUCE_TIME = 5000;   // 显示短拉介绍的时间

    private static int ORIENTATION_PORTRAIT = 1;
    private static int ORIENTATION_LANDSCAPE = 2;

    private static int mHistoryX = 0;
    private static int mHistoryY = 0;

    private boolean mHidingPanel = false;
    private boolean mBorderFloatingBall = true;            // 悬浮球是否自动靠边,迷你模式默认是靠边的
    private boolean mPanelShowed = false;

    private Context mContext;
    private Application mApp;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;             // 悬浮球的布局
    private WindowManager.LayoutParams mPanelParams;        // 转盘的布局
    private WindowManager.LayoutParams mCircleParams;       // 悬浮球短拉范围的圆环布局
    private WindowManager.LayoutParams mIntroduceParams;    // 短拉介绍的布局

    private View mFloatBall;                                // 悬浮求
    private View mCircleView;                               // 圆环背景
    private View mIntroduceView;                            // 短拉介绍的view
    private MiniBallPanel mPanel;
    private FloatingPanelFrameLayout mPanelLayout;

    private ImageView mIconBgView;
    private ImageView mCircleBg;
    private ImageView mIconCenterView;
    private TextView mTvIntroduce;
    private VectorDrawable mIconBgImg;
    private VectorDrawable mCirque;
    private VectorDrawable mIconCenterImg;
    private VectorDrawable mIconCenterImg2;
    private VectorDrawable mIntroduceBg;

    private int mViewWidth;
    private int mViewHeight;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenRealHeight;
    private int mStatusBarHeight;
    private int mNavigationBarHeight;
    private int mPanelHeight;
    private int mFabViewMinPadding;                          // 初始小圆靠边的最小距离
    private int mOrientation = ORIENTATION_PORTRAIT;         // 当前的屏幕是竖屏还是横屏  垂直 1 水平 2

    private long mCurrentClickTime;                          // 当前点击的时间
    private int mClickCount = 0;                             // 点击的次数
    private LongPressedThread mLongPressedThread;
    private ClickPressedThread mPrevClickThread;

    private float mXInScreen;
    private float mYInScreen;
    private float mXInView;
    private float mYInView;
    private float mXDownInScreen;
    private float mYDownInScreen;

    private float mCurrentAlpha = 0f;                       // 当前悬浮球的透明度
    public float mWidthPercent = 1f;                        // 悬浮球的X位置，位于屏幕宽的百分比
    public float mHeightPercent = 0.3f;                     // 悬浮球的Y位置，位于屏幕高的百分比

    public int mShortDragMaxDistance;                       // 短拉的最大距离
    private int mCircleViewHeight;                          // 圆形背景的高度
    private boolean mCircleViewShow = false;                // 是否显示短拉的背景图
    private boolean mFinishShortDrag = false;               // 是否结束短拉的事件
    private boolean mIsNeedShortDrag = true;                // 是否开通了短拉的手势
    private boolean mIsAlignStart = true;                   // 小圆球是否靠在开始那一边（左边）

    private Runnable mHideHalfCallback;
    private Handler mBaseHandler = new Handler();

    private OnFloatIconClickListener mOnFloatIconClickListener;

    private ArrayList<BaseItemInfo> mBaseItems;

    private MiniBallController(Context context, Application app) {
        mContext = context;
        mApp = app;
        NowKeyPanelModel.getInstance().setNowkeyModelCallback(this);
    }

    public static MiniBallController getController(Application app) {
        if (sInstance == null) {
            sInstance = new MiniBallController(app.getApplicationContext(), app);
        }
        return sInstance;
    }

    /**
     * 初始化
     */
    public void init() {
        getWindowManager();
        mScreenWidth = getScreenWidth(mContext);
        mScreenHeight = getScreenHeight(mContext);
        mScreenRealHeight = getDpi(mContext);
        initData();
        initCircleView();
        initIntroduce();
        initFloatBall();
        initFloatingPanel();
    }

    /**
     * 获得窗口服务
     *
     * @return
     */
    private WindowManager getWindowManager() {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 初始化数据
     */
    private void initData() {
        float currentScale_f = PreferenceUtils.getFloatBallViewSize(50);
        int currentScale = (int) currentScale_f;
        mViewWidth = Utils.dip2px(mContext, currentScale);
        mViewHeight = mViewWidth;
        mFabViewMinPadding = Utils.dip2px(mContext, 10);

        mPanelHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_width);
        mIconCenterImg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_luncher_icon);
        mIconCenterImg2 = (VectorDrawable) mContext.getDrawable(R.drawable.ic_clear_black_24px);
        mIconBgImg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_launcher_bg);
        mCirque = (VectorDrawable) mContext.getDrawable(R.drawable.cirque);
        mIntroduceBg = (VectorDrawable) mContext.getDrawable(R.drawable.circle1_4);

        mCircleViewHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.float_circle_view_width);
        mShortDragMaxDistance = (mCircleViewHeight / 2) + mViewWidth / 2;

        mCurrentAlpha = PreferenceUtils.getFloatBallViewOpacity(50) / 100f;
    }

    /**
     * 初始化悬浮球
     */
    private void initFloatBall() {
        // 从配置文件获取当前的大小
        ViewGroup.LayoutParams lp;

        mFloatBall = View.inflate(mApp, R.layout.floating_icon_view, null);
        mIconBgView = (ImageView) mFloatBall.findViewById(R.id.floating_icon_bg);
        lp = mIconBgView.getLayoutParams();
        lp.width = mViewWidth;
        lp.height = mViewHeight;
        mIconBgView.setLayoutParams(lp);
        float cs = ((float) lp.height) / 1.35f;
        mIconCenterView = (ImageView) mFloatBall.findViewById(R.id.floating_icon_center);
        lp = mIconCenterView.getLayoutParams();
        lp.width = (int) cs;
        lp.height = (int) cs;
        mIconCenterView.setLayoutParams(lp);
        mFloatBall.setOnTouchListener(this);
        mIconCenterView.setImageDrawable(mIconCenterImg);
        mIconBgView.setImageDrawable(mIconBgImg);

        mParams = new WindowManager.LayoutParams();
        mParams.setTitle("NowKeyBall");
        //设置window type
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        mParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.width = mViewWidth;
        mParams.height = mViewHeight;

        setFloatBallAlpha(mCurrentAlpha);
        getFloatBallXY();
        if (mHistoryX == 0 && mHistoryY == 0) {
            mParams.x = mScreenWidth;
            mParams.y = mScreenHeight / 2;
        } else {
            if (mHistoryX > mScreenWidth - mViewWidth / 2 || mHistoryY > mScreenHeight - mViewHeight / 2) {
                mParams.x = 0;
                mParams.y = mScreenHeight / 3;
                mHistoryX = mParams.x;
                mHistoryY = mParams.y;
                saveFloatBallXY();
            } else {
                mParams.x = mHistoryX;
                mParams.y = mHistoryY;
            }
        }
    }

    /**
     * 保存 悬浮球的 位置
     */
    private void saveFloatBallXY() {
        PreferenceUtils.setMiniFloatBallX(mHistoryX);
        PreferenceUtils.setMiniFloatBallY(mHistoryY);
    }

    /**
     * 获取 悬浮球 的位置
     */
    private void getFloatBallXY() {
        mHistoryX = PreferenceUtils.getMiniFloatBallX(0);
        mHistoryY = PreferenceUtils.getMiniFloatBallY(0);
    }

    /**
     * 初始化 圆环
     */
    private void initCircleView() {
        mCircleView = View.inflate(mApp, R.layout.circle_view, null);
        mCircleBg = (ImageView) mCircleView.findViewById(R.id.img_circle_bg);
        mCirque.setTint(getThemeColor());
        mCircleBg.setImageDrawable(mCirque);

        mCircleParams = new WindowManager.LayoutParams();
        mCircleParams.setTitle("NowKeyCircleView");
        //设置window type
        mCircleParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        mCircleParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mCircleParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //调整悬浮窗显示的停靠位置为左侧置顶
        mCircleParams.gravity = Gravity.LEFT | Gravity.TOP;
        mCircleParams.width = mCircleViewHeight;
        mCircleParams.height = mCircleViewHeight;

        mCircleParams.x = mScreenWidth - mViewWidth - mFabViewMinPadding;
        mCircleParams.y = mScreenHeight / 3;
        //mWindowManager.addView(mCircleView, mCircleParams);
    }

    /**
     * 初始化 介绍短拉 的 视图
     */
    private void initIntroduce() {
        if (PreferenceUtils.isNeedShowShortDragIntroduce(true)) {
            mIntroduceView = View.inflate(mApp, R.layout.view_introduce_gesture, null);
            mIntroduceView.setBackground(mIntroduceBg);
            mTvIntroduce = (TextView) mIntroduceView.findViewById(R.id.tv_introduce_text);

            mIntroduceParams = new WindowManager.LayoutParams();
            mIntroduceParams.setTitle("NowKeyIntroduceView");
            //设置window type
            mIntroduceParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            //设置图片格式，效果为背景透明
            mIntroduceParams.format = PixelFormat.RGBA_8888;
            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
            mIntroduceParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            //调整悬浮窗显示的停靠位置为左侧置顶
            if (mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                mIntroduceParams.gravity = Gravity.END | Gravity.BOTTOM;
            } else {
                mIntroduceParams.gravity = Gravity.START | Gravity.BOTTOM;
            }
            mIntroduceParams.width = mScreenWidth * 4 / 5;
            mIntroduceParams.height = mScreenWidth * 4 / 5;
        }
    }

    /**
     * 初始化 转盘
     */
    private void initFloatingPanel() {
        mPanelLayout = (FloatingPanelFrameLayout) View.inflate(mApp, R.layout.mini_ball_panel_layout, null);
        /*
        mPanelLayout.setDispatchKeyEventListener(new FloatingPanelFrameLayout.DispatchKeyEventListener() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (mPanelShowed) {
                        hidePanel();
                        return true;
                    }
                }
                return false;
            }
        });*/
        mPanelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnFloatIconClickListener != null) {
                    mOnFloatIconClickListener.onFloatOutsideClick();
                }
            }
        });

        mPanel = (MiniBallPanel) mPanelLayout.findViewById(R.id.id_mini_panel);
        getPanelData();
        mPanel.setNowKeyData(mBaseItems);

        mPanelParams = new WindowManager.LayoutParams();
        mPanelParams.setTitle("NowKeyPanel");
        //设置window type
        mPanelParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        mPanelParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mPanelParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mPanelParams.gravity = Gravity.LEFT | Gravity.TOP;
        mPanelParams.width = mScreenWidth;
        mPanelParams.height = mScreenRealHeight;
        mPanelParams.y = 0;
    }

    /**
     * 销毁时 调用
     */
    public void onDestroy() {
        Log.d(TAG, "anxi onDestroy");
        NowKeyPanelModel.getInstance().removeNowKeyModelCallback(this);
        removeAllViews();
        NowKeyPanelModel.getInstance().onDestroy();
        sInstance = null;

        mWindowManager = null;
        mCircleParams = null;
        mPanelParams = null;
        mParams = null;
        mCircleParams = null;
        mIntroduceParams = null;

        mIconBgView = null;
        mIconCenterView = null;
        mIconBgImg = null;
        mCirque = null;
        mIconCenterImg = null;
        mIconCenterImg2 = null;
        mIntroduceBg = null;

        mContext = null;
        mApp = null;
        mFloatBall = null;
        mCircleView = null;
        mIntroduceView = null;
        mCircleBg = null;
        mPanelLayout = null;
        mTvIntroduce = null;

        mLongPressedThread = null;
        mPrevClickThread = null;
        mHideHalfCallback = null;
        mOnFloatIconClickListener = null;
        mBaseHandler = null;

        if (mBaseItems != null) {
            mBaseItems.clear();
            mBaseItems = null;
        }

        if (mPanel != null) {
            mPanel.onDestroy();
            mPanel = null;
        }
    }

    public ArrayList<BaseItemInfo> getItems() {
        return mBaseItems;
    }

    /**
     * 让悬浮球自动靠边
     */
    private void stepAside() {
        if (mParams == null) return;
        if (mBorderFloatingBall) {
            float viewCenterX = mParams.x + mViewWidth / 2;
            float dxLeft = viewCenterX;
            float dxRight = mScreenWidth - dxLeft;
            float result;
            if (dxLeft < dxRight) {
                result = dxLeft;
                mIsAlignStart = true;
                mPanel.mIsAlignStart = true;
            } else {
                result = dxRight;
                mIsAlignStart = false;
                mPanel.mIsAlignStart = false;
            }

            //设置靠边，有边距
            if (result == dxLeft) {
                mParams.x = mFabViewMinPadding;
            } else if (result == dxRight) {
                mParams.x = mScreenWidth - mViewWidth - mFabViewMinPadding;
            }

            //如果太靠近顶部和底部，会使展开的转盘显示不全，所以此时要调整一下y的位置。
            if (mParams.y < mPanelHeight / 2) {
                mParams.y = mPanelHeight / 2 - mViewHeight / 2;
            } else if (mParams.y > mScreenHeight - mPanelHeight / 2) {
                mParams.y = (mScreenHeight - mPanelHeight / 2 - mViewHeight);
            }

            try {
                getWindowManager().updateViewLayout(mFloatBall, mParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mParams != null) {
            mHistoryX = mParams.x;
            mHistoryY = mParams.y;
            saveFloatBallXY();
            mWidthPercent = (float) mHistoryX / (float) mScreenWidth;
            mHeightPercent = (float) mHistoryY / (float) mScreenRealHeight;
        }

        hideHalf();
    }

    /**
     * 没有对悬浮球操作之后，要便透明
     */
    private void hideHalf() {
        if (mFloatBall != null) {
            if (mHideHalfCallback == null) {
                mHideHalfCallback = new HideHalfCallback();
            }
            mFloatBall.removeCallbacks(mHideHalfCallback);
            mFloatBall.postDelayed(mHideHalfCallback, 2000);
        }
    }

    /**
     * 更新悬浮球的位置
     */
    private void updateViewPosition() {
        if (mParams == null) return;

        mParams.x = (int) (mXInScreen - mXInView);
        mParams.y = (int) (mYInScreen - mYInView);

        if (mParams.x < 0) {
            mParams.x = 0;
        }
        if (mParams.y < 0) {
            mParams.y = 0;
        }
        if ((mParams.x + mViewWidth) > mScreenWidth) {
            mParams.x = mScreenWidth - mViewWidth;
        }
        if (mParams.y + mViewHeight > mScreenHeight - getStatusBarHeight()) {
            mParams.y = mScreenHeight - getStatusBarHeight() - mViewHeight;
        }
        setFloatBallAlpha(mCurrentAlpha);
        getWindowManager().updateViewLayout(mFloatBall, mParams);
        mHistoryX = mParams.x;
        mHistoryY = mParams.y;
    }

    /**
     * 修改 悬浮球的大小
     *
     * @param option
     */
    public void updateViewSize(int option) {
        if (mParams == null) return;
        removeAllViews();
        ViewGroup.LayoutParams lp;
        int cs;
        float temp;
        switch (option) {
            case 40:
                mViewWidth = Utils.dip2px(mContext, 40);
                break;
            case 45:
                mViewWidth = Utils.dip2px(mContext, 45);
                break;
            case 50:
                mViewWidth = Utils.dip2px(mContext, 50);
                break;
            case 55:
                mViewWidth = Utils.dip2px(mContext, 55);
                break;
            default:
                mViewWidth = Utils.dip2px(mContext, 50);
                break;
        }
        mViewHeight = mViewWidth;
        mParams.height = mViewHeight;
        mParams.width = mViewWidth;

        lp = mIconBgView.getLayoutParams();
        lp.width = mParams.width;
        lp.height = mParams.height;
        mIconBgView.setLayoutParams(lp);
        temp = (float) lp.width;

        lp = mIconCenterView.getLayoutParams();
        cs = (int) (temp / 1.35f);
        lp.width = cs;
        lp.height = cs;
        mIconCenterView.setLayoutParams(lp);

        try {
            setFloatBallAlpha(mCurrentAlpha);
            getWindowManager().updateViewLayout(mFloatBall, mParams);
            showFloatBall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提供给外部 修改 悬浮球透明度的 方法
     *
     * @param alpha
     */
    public void updateFloatBallAlpha(float alpha) {
        mCurrentAlpha = alpha;
        setFloatBallAlpha(alpha);
        hideHalf();
    }

    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                mStatusBarHeight = mFloatBall.getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStatusBarHeight;
    }

    public int getBottomStatusHeight(Context context) {
        int totalHeight = getDpi(context);

        int contentHeight = getScreenHeight(context);

        mNavigationBarHeight = totalHeight - contentHeight;

        return mNavigationBarHeight;
    }

    public int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    public int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 显示 悬浮球
     */
    public void showFloatBall() {
        Log.d(TAG, "anxi showFloatBall");
        try {
            getWindowManager().addView(mFloatBall, mParams);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //add by yangzhong.gong for task-4584915 begin
        //setFloatBallAlpha(mCurrentAlpha);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFloatBall, "alpha", 0f, mCurrentAlpha);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatBall, "scaleX", 0.4f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatBall, "scaleY", 0.4f, 1f);

        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.start();
        setBorderOptions(true);
        //add by yangzhong.gong for task-4584915 end
    }

    public void setonFloatIconClickListener(OnFloatIconClickListener onFloatIconClickListener) {
        mOnFloatIconClickListener = onFloatIconClickListener;
    }

    public void setOnPanelClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        if (mPanel != null) {
            mPanel.setOnMenuItemClickListener(onMenuItemClickListener);
        }
    }

    @Override
    public void onNowKeyItemUpdatePosition(ArrayList<BaseItemInfo> items, int page) {
        if (page == 0) {
            getPanelData();
            mPanel.setNowKeyData(mBaseItems);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "anxi down mCurrentAlpha = " + mCurrentAlpha);
                setFloatBallAlpha(mCurrentAlpha);
                //modify by yangzhong.gong for defect-4641685 begin
                if (mFloatBall != null && mHideHalfCallback != null) {
                    mFloatBall.removeCallbacks(mHideHalfCallback);
                }
                //modify by yangzhong.gong for defect-4641685 end
                mXInScreen = event.getRawX();
                mYInScreen = event.getRawY() - getStatusBarHeight();
                mXDownInScreen = event.getRawX();
                mYDownInScreen = event.getRawY() - getStatusBarHeight();
                mXInView = event.getX();
                mYInView = event.getY();
                //记录当前点击的时间
                mCurrentClickTime = System.currentTimeMillis();
                mClickCount++;
                //取消上一次点击的线程
                if (mBaseHandler != null) {
                    if (mPrevClickThread != null) {
                        mBaseHandler.removeCallbacks(mPrevClickThread);
                    }
                    mLongPressedThread = new LongPressedThread();
                    mBaseHandler.postDelayed(mLongPressedThread, LONG_PRESS_TIME);
                }

                // 判断短拉的手势 是否需要
                if (Constant.NOW_KEY_ITEM_TYPE_NONE == PreferenceUtils.getMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_NONE)) {
                    mIsNeedShortDrag = false;
                } else {
                    mIsNeedShortDrag = true;
                    mFinishShortDrag = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mXInScreen = event.getRawX();
                mYInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();
                if (this.isMoved()) {
                    mClickCount = 0; // 只要移动了 就没有点击事件了
                    if (mBaseHandler != null) {
                        mBaseHandler.removeCallbacks(mLongPressedThread); //取消注册的长按事件
                    }

                    //处理短拉 事件
                    // 想触发短拉事件有3个前提， 1：拖动悬浮球不能超出短拉范围 2：从按下拖动，到松开时间要小于300 毫秒 3：需要有移动悬浮求的操作
                    if (!mCircleViewShow && !mFinishShortDrag && mIsNeedShortDrag) {
                        showIntroduceView();
                        showCircleView();
                    }
                    if (mCircleViewShow) {
                        long spendTime = System.currentTimeMillis() - mCurrentClickTime;
                        if (isBeyondShortDragMax() || spendTime > SHORT_DRAG_TIME) {
                            hideCircleView();
                            mFinishShortDrag = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!this.isMoved()) {
                    //如果按住的时间超过了长按时间，那么其实长按事件已经出发生了,这个时候把数据清零
                    if (mBaseHandler != null && (System.currentTimeMillis() - mCurrentClickTime <= LONG_PRESS_TIME)) {
                        //取消注册的长按事件
                        mBaseHandler.removeCallbacks(mLongPressedThread);
                        mPrevClickThread = new ClickPressedThread();
                        mBaseHandler.postDelayed(mPrevClickThread, CLICK_SPACING_TIME);
                    }
                }

                if (this.isMoved() && isShortDrag() && mOnFloatIconClickListener != null && !mFinishShortDrag && mIsNeedShortDrag) {
                    mOnFloatIconClickListener.onFloatIconShortDrag();
                    mXInScreen = mXDownInScreen;
                    mYInScreen = mYDownInScreen;
                    updateViewPosition();
                }
                hideCircleView();
                hideIntroduceView();
                stepAside();
                break;
        }
        return true;
    }

    /**
     * 隐藏圆形背景
     */
    private void hideCircleView() {
        try {
            if (mCircleView != null) {
                getWindowManager().removeView(mCircleView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCircleViewShow = false;
    }

    /**
     * 显示圆形背景
     */
    private void showCircleView() {
        if (mCircleParams == null) return;
        mCircleViewShow = true;

        if (!mCircleView.isAttachedToWindow()) {
            Log.d(TAG, "anxi mCircleView.isAttachedToWindow() == false");
            initCircleView();
        } else {
            Log.d(TAG, "anxi mCircleView.isAttachedToWindow() == true");
        }

        mCircleParams.x = (int) (mXInScreen - mXInView - mCircleViewHeight / 2 + mViewHeight / 2);
        mCircleParams.y = (int) (mYInScreen - mYInView - mCircleViewHeight / 2 + mViewHeight / 2);
        try {
            getWindowManager().addView(mCircleView, mCircleParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCircleView.setAlpha(0.15f);
    }

    /**
     * 显示圆形背景
     */
    private void showIntroduceView() {
        if (mIntroduceParams == null || mTvIntroduce == null) return;
        // 只显示一次 短拉 介绍
        if (PreferenceUtils.isNeedShowShortDragIntroduce(true)) {

            mTvIntroduce.setVisibility(View.GONE);
            mIntroduceBg.setTint(getThemeColor());
            mIntroduceView.setBackground(mIntroduceBg);
            mIntroduceView.setAlpha(0);

            try {
                getWindowManager().addView(mIntroduceView, mIntroduceParams);
            } catch (Exception e) {
                e.printStackTrace();
            }

            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(mIntroduceView, "alpha", 0.5f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mIntroduceView, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mIntroduceView, "scaleY", 0f, 1f);
            ObjectAnimator tranY = ObjectAnimator.ofFloat(mIntroduceView, "Y", mScreenWidth / 2f, 0f);
            ObjectAnimator tranX = ObjectAnimator.ofFloat(mIntroduceView, "X", -mScreenWidth / 2f, 0f);
            animatorSet.setDuration(500);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY, tranX, tranY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mTvIntroduce != null) {
                        mTvIntroduce.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (mTvIntroduce != null) {
                        mTvIntroduce.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.start();
            // 15秒之后执行 hideIntroduceView，
            mBaseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideIntroduceView();
                }
            }, 15000);
        }
    }

    /**
     * 隐藏 短拉手势介绍 的 视图
     * 只显示一次，显示完了之后就释放资源
     */
    private void hideIntroduceView() {
        if (mIntroduceParams == null) return;
        if (PreferenceUtils.isNeedShowShortDragIntroduce(true)) {
            try {
                if (mIntroduceView != null) {
                    getWindowManager().removeView(mIntroduceView);
                    Log.d(TAG, "anxi removeView (mIntroduceView)");
                } else {
                    Log.d(TAG, "anxi mIntroduceView equal null !!!");
                }
                PreferenceUtils.setIsNeedShortDragIntroduce(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "anxi Exception e:" + e);
            }
        }
    }

    @Override
    public void onNowKeyItemDelete(BaseItemInfo item, int page) {

    }

    @Override
    public void onNowKeyItemAdd(BaseItemInfo item, int page, boolean external) {
        if (item == null) return;
        if (page == 0) {
            resetPanel();
        }
    }

    @Override
    public void onNowKeyItemReplace(BaseItemInfo item, int page, boolean external) {
        if (item == null) return;
        if (page == 0) {
            resetPanel();
        }
    }

    @Override
    public void onNowKeyItemUpdate(BaseItemInfo item, int page) {
        if (item == null) return;
        if (item.getKey_word().equals("callacontact")) {
            if (page == 0) {
                mPanel.updateCurrentItem(item);
            }
        }
    }

    public BaseItemInfo getItemFromPackage(String pkg) {
        for (BaseItemInfo info : mBaseItems) {
            if (pkg.equals(info.getKey_word())) return info;
        }
        return null;
    }

    /**
     * 移除掉所有的 view
     */
    public void removeAllViews() {
        Log.d(TAG, "anxi removeAllViews");
        if (mPanelShowed) {
            hidePanel();
            mPanelShowed = false;
        }

        try {
            if (mFloatBall != null) {
                getWindowManager().removeView(mFloatBall);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mCircleView != null) {
                getWindowManager().removeView(mCircleView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mPanelLayout != null) {
                getWindowManager().removeView(mPanelLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mFloatBall != null && mHideHalfCallback != null) {
            mFloatBall.removeCallbacks(mHideHalfCallback);
        }
    }

    /**
     * 显示转盘
     */
    public void showPanel() {
        if (mPanel != null) {
            FrameLayout.LayoutParams panelParams = (FrameLayout.LayoutParams) mPanel.getLayoutParams();
            //add by yangzhong.gong for defect-5328308 begin
            //调整mPanel的位置
            if (mOrientation == ORIENTATION_PORTRAIT) {
                panelParams.rightMargin = (mScreenWidth / 2 - mParams.x - mViewWidth / 2);
                panelParams.bottomMargin = (mScreenRealHeight / 2 - mParams.y- mViewWidth / 2);
            } else if (mOrientation == ORIENTATION_LANDSCAPE) {
                panelParams.rightMargin = (mScreenWidth / 2 - mParams.x - mViewWidth / 2);
                panelParams.bottomMargin = (mScreenRealHeight / 2 - mParams.y - mViewWidth / 2);
            }
            //add by yangzhong.gong for defect-5328308 end
            //panelParams.bottomMargin = (mScreenHeight / 2 - mParams.y);

            try {
                getWindowManager().addView(mPanelLayout, mPanelParams);
                mPanelShowed = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, "mPanel.setShowPanelAnimation()");
            //延迟50ms播放动画，因为转盘第一次启动，需要一点点时间来执行 onMeasure 和 onLayout
            mBaseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //设置动画
                    mPanel.setShowPanelAnimation(mIsAlignStart, MiniBallController.this);
                    FloatViewToSmallAnimation();
                }
            }, 50);
        }
    }

    /**
     * 隐藏转盘
     */
    public void hidePanel() {
        Log.d(TAG, "hidePanel isPanelFling:" + isPanelFling());
        if (mHidingPanel) return;
        if (mPanel != null && !isPanelFling()) {
            Log.d(TAG, "mPanel.setHidePanelAnimation()");
            mPanel.setHidePanelAnimation(mIsAlignStart, this);
            FloatViewToBigAnimation();
        }
        mBaseHandler.postDelayed(new AutoHidePanelRunnable(), 600);
    }

    /**
     * 获取转盘的数据
     */
    public void getPanelData() {
        NowKeyPanelModel.getInstance().initExtraData();
        mBaseItems = NowKeyPanelModel.getInstance().loadMiniData();
        Collections.sort(mBaseItems, new BaseModelComparator());
        mBaseItems = Utils.convertBaseItemInfos(mContext, mBaseItems);
    }

    /**
     * 避免关闭转盘失败，这里做预防处理
     * 600毫秒后，处理关闭转盘之后 的工作
     */
    class AutoHidePanelRunnable implements Runnable {
        @Override
        public void run() {
            if (mHidingPanel) {
                Log.e(TAG, " AutoHidePanelRunnable");
                onHidePanelAnimationEnd();
                mPanel.resetPosition();
            }
        }
    }

    /**
     * 悬浮球变小动画
     */
    private void FloatViewToSmallAnimation() {
        if (mHideHalfCallback != null) {
            mFloatBall.removeCallbacks(mHideHalfCallback);
        } else {
            mHideHalfCallback = new HideHalfCallback();
            mFloatBall.removeCallbacks(mHideHalfCallback);
        }

        // floatView 动画 分成两个部分
        // 一：背景变小从1 -> 0.85
        // 二: 中间的图标发生动画(这个第二步又分为两个部分 一：图标变小从1->0  二：换成十字图标，变大0->0.4 同时 旋转90度)

        // 第一步
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatBall, "scaleX", 1f, 0.85f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatBall, "scaleY", 1f, 0.85f);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();

        // 第二步
        AnimatorSet animatorSet2 = new AnimatorSet();
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mIconCenterView, "scaleX", 1f, 0);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mIconCenterView, "scaleY", 1f, 0);

        animatorSet2.setDuration(200);
        animatorSet2.setInterpolator(new AccelerateInterpolator());
        animatorSet2.playTogether(scaleX2, scaleY2);
        animatorSet2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIconCenterView == null) return;
                mIconCenterView.setImageDrawable(mIconCenterImg2);
                AnimatorSet animatorSet3 = new AnimatorSet();
                ObjectAnimator scaleX3 = ObjectAnimator.ofFloat(mIconCenterView, "scaleX", 0, 0.4f);
                ObjectAnimator scaleY3 = ObjectAnimator.ofFloat(mIconCenterView, "scaleY", 0, 0.4f);
                ObjectAnimator rotation3 = ObjectAnimator.ofFloat(mIconCenterView, "rotation", 0, 90);
                animatorSet3.setDuration(200);
                animatorSet3.setInterpolator(new AccelerateInterpolator());
                animatorSet3.playTogether(scaleX3, scaleY3, rotation3);
                animatorSet3.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet2.start();
    }

    /**
     * 悬浮求变大动画
     */
    private void FloatViewToBigAnimation() {
        hideHalf();

        // floatView 动画 分成两个部分
        // 一 : 中间的图标发生动画(这个第二步又分为两个部分 一：十字图标变小从0.4->0 同时旋转90度 二：换成原来图标，变大0->1)
        // 二 : 背景变大从0.85 -> 1

        // 第一步
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mIconCenterView, "scaleX", 0.4f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mIconCenterView, "scaleY", 0.4f, 0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(mIconCenterView, "rotation", 90, 0);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIconCenterView == null) {
                    Log.d(TAG, " Fail !!! mIconCenterView == null when do FloatViewToBigAnimation() ");
                    return;
                }
                if (mIconCenterImg == null) {
                    Log.d(TAG, " Fail !!! mIconCenterImg == null when do FloatViewToBigAnimation() ");
                    return;
                }
                mIconCenterView.setImageDrawable(mIconCenterImg);
                AnimatorSet animatorSet2 = new AnimatorSet();
                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mIconCenterView, "scaleX", 0, 1f);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mIconCenterView, "scaleY", 0, 1f);

                animatorSet2.setDuration(200);
                animatorSet2.setInterpolator(new AccelerateInterpolator());
                animatorSet2.playTogether(scaleX2, scaleY2);
                animatorSet2.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

        // 第二步
        AnimatorSet animatorSet3 = new AnimatorSet();
        ObjectAnimator scaleX3 = ObjectAnimator.ofFloat(mFloatBall, "scaleX", 0.85f, 1f);
        ObjectAnimator scaleY3 = ObjectAnimator.ofFloat(mFloatBall, "scaleY", 0.85f, 1f);

        animatorSet3.setDuration(200);
        animatorSet3.setInterpolator(new AccelerateInterpolator());
        animatorSet3.playTogether(scaleX3, scaleY3);
        animatorSet3.start();
    }

    @Override
    public void onHidePanelAnimationStart() {
        Log.d(TAG, " onHidePanelAnimationStart");
        mPanelShowed = false;
        mHidingPanel = true;
    }

    @Override
    public void onHidePanelAnimationEnd() {
        Log.d(TAG, " onHidePanelAnimationEnd");
        try {
            if (mPanelLayout != null) {
                getWindowManager().removeView(mPanelLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "anxi Exception e:" + e);
        }
        mHidingPanel = false;
        if (mPanel != null) {
            mPanel.onFinishHide();
        }
    }

    @Override
    public void onHidePanelAnimationCancel() {
        Log.d(TAG, "onHidePanelAnimationCancel");
        try {
            if (mPanelLayout != null) {
                getWindowManager().removeView(mPanelLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "anxi Exception e:" + e);
        }
        mHidingPanel = false;
        mPanel.onFinishHide();
    }

    public void setFloatBallAlpha(float f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                mFloatBall.setAlpha(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回 Nowkey悬浮球边界
     *
     * @return
     */
    public Rect getFloatBallPosition() {
        return new Rect(mHistoryX, mHistoryY, mHistoryX + mViewWidth, mHistoryY + mViewHeight);
    }

    /**
     * 判断是否移动
     *
     * @return
     */
    private boolean isMoved() {
        //允许有20的偏差 在判断是否移动的时候
        if (Math.abs(mXDownInScreen - mXInScreen) <= 10
                && Math.abs(mYDownInScreen - mYInScreen) <= 10) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断是否短拉 触发
     *
     * @return
     */
    private boolean isShortDrag() {
        PointF start = new PointF(mXDownInScreen, mYDownInScreen);
        PointF end = new PointF(mXInScreen, mYInScreen);
        float distance = GeometryUtil.getDistanceBetween2Points(start, end);
        if (distance < mShortDragMaxDistance && distance > (mShortDragMaxDistance / 2 - mViewHeight / 2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断 超出了短拉的范围
     *
     * @return
     */
    private boolean isBeyondShortDragMax() {
        PointF start = new PointF(mXDownInScreen, mYDownInScreen);
        PointF end = new PointF(mXInScreen, mYInScreen);
        float distance = GeometryUtil.getDistanceBetween2Points(start, end);
        if (distance > mShortDragMaxDistance) {
            return true;
        } else {
            return false;
        }
    }

    public void resetPanel() {
        if (mPanel != null) {
            getPanelData();
            mPanel.setNowKeyData(mBaseItems);
        }
    }

    /**
     * 手机屏幕转屏的时候条用
     *
     * @param currentOrientation
     */
    public void onConfigChange(int currentOrientation) {
        setFloatBallAlpha(mCurrentAlpha);
        mOrientation = currentOrientation;

        mWindowManager = getWindowManager();
        mScreenWidth = getScreenWidth(mContext);
        mScreenHeight = getScreenHeight(mContext);
        mScreenRealHeight = getDpi(mContext);

        // 跟新悬浮球的属性
        mParams.width = mViewWidth;
        mParams.height = mViewHeight;
        if (mHistoryX == 0 && mHistoryY == 0) {
            mParams.x = mScreenWidth;
            mParams.y = mScreenHeight / 3;
        } else {
            mParams.x = (int) (mScreenWidth * mWidthPercent);
            mParams.y = (int) (mScreenHeight * mHeightPercent);
        }

        // 更新转盘的属性
        mPanelParams.width = mScreenWidth;
        mPanelParams.height = mScreenRealHeight;
        stepAside();
    }

    public void onThemeChange() {
        int color = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME, -1);
        if (color == -1) {
            color = Color.parseColor(Constant.DEFAULT_THEME);
        }
        PorterDuffColorFilter pf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        if (mIconBgImg != null) {
            mIconBgImg.setColorFilter(pf);
            mIconBgView.setImageDrawable(mIconBgImg);
        }
        if (mCirque != null) {
            mCirque.setTint(color);
            mCircleBg.setImageDrawable(mCirque);
        }
        if (mPanel != null) {
            mPanel.onThemeChanged(color);
        }
        if (mIntroduceBg != null) {
            mIntroduceBg.setColorFilter(pf);
        }
    }

    private int getThemeColor() {
        int color = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME, -1);
        if (color == -1) {
            color = Color.parseColor(Constant.DEFAULT_THEME);
        }
        return color;
    }

    public class LongPressedThread implements Runnable {
        @Override
        public void run() {
            //这里处理长按事件
            if (mClickCount == 1) {
                if (mOnFloatIconClickListener != null) {
                    mOnFloatIconClickListener.onFloatIconPress();
                }
            }
            mClickCount = 0;
        }
    }

    public class ClickPressedThread implements Runnable {
        @Override
        public void run() {
            //这里处理连续点击事件 mClickCount 为连续点击的次数
            if (mClickCount == 2) {
                if (mOnFloatIconClickListener != null) {
                    mOnFloatIconClickListener.onFloatIconDoubleClick();
                }
            } else if (mClickCount == 1) {
                if (mOnFloatIconClickListener != null) {
                    mOnFloatIconClickListener.onFloatIconClick();
                }
            }
            mClickCount = 0;
        }
    }

    /**
     * 转盘是否在转动
     *
     * @return
     */
    public boolean isPanelFling() {
        if (mPanel != null) {
            return mPanel.isFling();
        }
        return false;
    }

    /**
     * 拖动悬浮按钮停下来的时候，矫正位置，靠边有间距，不能靠近顶部和底部避免转盘显示不全
     *
     * @param bordered
     */
    public void setBorderOptions(boolean bordered) {
        mBorderFloatingBall = bordered;
        if (bordered) {
            stepAside();
        } else {
            mWidthPercent = (float) mHistoryX / (float) mScreenWidth;
            mHeightPercent = (float) mHistoryY / (float) mScreenRealHeight;
        }
    }

    /**
     * 悬浮球 静止时，变透明的动画
     */
    private class HideHalfCallback implements Runnable {
        public HideHalfCallback() {
        }

        @Override
        public void run() {
            if (null != mParams) {
                mCurrentAlpha = PreferenceUtils.getFloatBallViewOpacity(50f) / 100f;
                Log.d(TAG, " HideHalfCallback() mCurrentAlpha = " + mCurrentAlpha);
                if (mCurrentAlpha > 0.3f) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    ObjectAnimator alpha = ObjectAnimator.ofFloat(mFloatBall, "alpha", mCurrentAlpha, 0.3f);
                    animatorSet.setDuration(700);
                    animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                    animatorSet.playTogether(alpha);
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setFloatBallAlpha(0.3f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            setFloatBallAlpha(0.3f);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animatorSet.start();
                }

                mHistoryX = mParams.x;
                mHistoryY = mParams.y;
            }
        }
    }
}