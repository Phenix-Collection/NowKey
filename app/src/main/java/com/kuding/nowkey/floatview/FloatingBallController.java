package com.kuding.nowkey.floatview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuding.nowkey.R;
import com.kuding.nowkey.Utils.BaseModelComparator;
import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.GeometryUtil;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.Utils.Utils;
import com.kuding.nowkey.info.BaseItemInfo;
import com.kuding.nowkey.interfaces.OnFloatIconClickListener;
import com.kuding.nowkey.interfaces.OnFloatIconUpdateListener;
import com.kuding.nowkey.interfaces.OnMenuItemClickListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/**
 * NowKey 普通模式 的 控制器
 */
public class FloatingBallController implements
        View.OnTouchListener, NowKeyPanelModel.NowKeyModelCallback {
    private static final String TAG = "FloatingBallController";
    private static FloatingBallController sInstance;

    //private static final long CLICK_SPACING_TIME = 300;
    private static final long SHOW_INTRODUCE_TIME = 5000;   // 显示短拉介绍的时间
    private static final long CLICK_SPACING_TIME = 200;
    private static final long LONG_PRESS_TIME = 800;
    private static final long SHORT_DRAG_TIME = 300;        // 短拉触发的限定时间

    private static int mHistoryX = 0;
    private static int mHistoryY = 0;

    private boolean mHidingPanel = false;
    private boolean mPanelShowed = false;

    private Context mContext;
    private Application mApp;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private WindowManager.LayoutParams mPanelParams;
    private WindowManager.LayoutParams mCircleParams;
    private WindowManager.LayoutParams mIntroduceParams;

    private View mFloatView;
    private View mIntroduceView;
    private FloatingPanelFrameLayout mPanelLayout;
    private FrameLayout mWarningDialog;
    private Button mDialogPositive;
    private Button mDialogNegative;
    private FloatingBallPanel mFirstFloatPanel;
    private FloatingBallPanel mSecondFloatPanel;
    private View mFirstCenterView;
    private View mSecondCenterView;
    private View mCircleView;
    private ImageView mCircleBg;

    private ImageView mPanelBg;

    private VectorDrawable mPanelBgImg;

    private ImageView mIconBgView;
    private ImageView mIconCenterView;
    private TextView mTvIntroduce;

    private VectorDrawable mIconBgImg;
    private VectorDrawable mIconCenterImg;
    private VectorDrawable mCirque;
    private VectorDrawable mIntroduceBg;

    private int mViewWidth;
    private int mViewHeight;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenRealHeight;
    private int mStatusBarHeight;
    private int mNavigationBarHeight;
    private int mClickCount = 0;
    private int mPanelWidth;
    private int mPanelHeight;
    private int mCurrentPage = 1;

    private float mXInScreen;
    private float mYInScreen;
    private float mXInView;
    private float mYInView;
    private float mXDownInScreen;
    private float mYDownInScreen;
    public float mWidthPercent = 1f;        // 悬浮球的X位置，位于屏幕宽的百分比
    public float mHeightPercent = 0.3f;     // 悬浮球的Y位置，位于屏幕高的百分比
    public float mCurrentAlpha = 0f;        // 当前悬浮球的透明度
    private int mFabViewMinPadding;         // 初始小圆靠边的最小距离

    public int mShortDragMaxDistance;       // 短拉的最大距离
    private int mCircleViewHeight;          // 圆形背景的高度
    boolean mCircleViewShow = false;        // 是否显示短拉的背景图
    boolean mFinishShortDrag = false;       // 是否结束短拉的事件,比如用户把悬浮求拖出了最大距离之后，就算结束了，如果用又拖回来，就不显示圆圈了
    boolean mIsNeedShortDrag = true;        // 是否开通了短拉的手势

    private long mCurrentClickTime;

    private Runnable mHideHalfCallback;
    private Handler mBaseHandler = new Handler();

    private OnFloatIconClickListener mOnFloatIconClickListener;
    private LongPressedThread mLongPressedThread;
    private ClickPressedThread mPrevClickThread;

    private ArrayList<BaseItemInfo> mPage1Items;
    private ArrayList<BaseItemInfo> mPage2Items;

    private OnFloatIconUpdateListener mOnFloatIconUpdateListener;

    private AsyncTask<Void, Drawable, Drawable> mScreenShotTask;

    private FloatingBallController(Context context, Application app) {
        mContext = context;
        mApp = app;
        NowKeyPanelModel.getInstance().setNowkeyModelCallback(this);
    }

    public static FloatingBallController getController(Application app) {
        if (sInstance == null) {
            sInstance = new FloatingBallController(app.getApplicationContext(), app);
        }
        return sInstance;
    }

    public void init() {
        mWindowManager = getWindowManager();
        mScreenWidth = getScreenWidth(mContext);
        mScreenHeight = getScreenHeight(mContext);
        mScreenRealHeight = getDpi(mContext);

        initData();
        initIntroduce();
        initCircleBg();
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

    private void initData() {
        float currentScale_f = PreferenceUtils.getFloatBallViewSize(50f);
        int currentScale = (int) currentScale_f;
        mViewWidth = Utils.dip2px(mContext, currentScale);
        mViewHeight = mViewWidth;
        mFabViewMinPadding = Utils.dip2px(mContext, 10);

        mPanelHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.now_key_float_panel_width);
        mPanelWidth = mContext.getResources().getDimensionPixelSize(R.dimen.now_key_float_panel_width);
        mIconCenterImg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_luncher_icon);
        mIconBgImg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_launcher_bg);
        mCirque = (VectorDrawable) mContext.getDrawable(R.drawable.cirque);
        mIntroduceBg = (VectorDrawable) mContext.getDrawable(R.drawable.circle1_4);

        mCircleViewHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.float_circle_view_width);
        mShortDragMaxDistance = (mCircleViewHeight / 2) + mViewWidth / 2;

        mCurrentAlpha = PreferenceUtils.getFloatBallViewOpacity(50f) / 100f;
    }

    private void initCircleBg() {
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
    }

    private void initFloatBall() {
        ViewGroup.LayoutParams lp;
        mFloatView = View.inflate(mApp, R.layout.floating_icon_view, null);
        mIconBgView = (ImageView) mFloatView.findViewById(R.id.floating_icon_bg);
        lp = mIconBgView.getLayoutParams();
        lp.width = mViewWidth;
        lp.height = mViewHeight;
        float cs = ((float) lp.height) / 1.35f;
        mIconBgView.setLayoutParams(lp);
        mIconCenterView = (ImageView) mFloatView.findViewById(R.id.floating_icon_center);
        lp = mIconCenterView.getLayoutParams();
        lp.width = (int) cs;
        lp.height = (int) cs;
        mIconCenterView.setLayoutParams(lp);
        mFloatView.setOnTouchListener(this);
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

            //mCircleParams.width = mPanelHeight - mViewWidth;
            mIntroduceParams.width = mScreenWidth * 4 / 5;
            mIntroduceParams.height = mScreenWidth * 4 / 5;
        }
    }

    /**
     * 初始化 转盘
     */
    private void initFloatingPanel() {
        mPanelBgImg = (VectorDrawable) mContext.getResources().getDrawable(R.drawable.launch_normal_bg);
        mPanelLayout = (FloatingPanelFrameLayout) View.inflate(mApp, R.layout.floating_ball_panel_layout, null);
        /*mPanelLayout.setDispatchKeyEventListener(new FloatingPanelFrameLayout.DispatchKeyEventListener() {
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
                try {
                    if (mFirstFloatPanel.isEditing() || mSecondFloatPanel.isEditing()) {
                        if (mFirstFloatPanel.isEditing() && !mFirstFloatPanel.isEmpty()) {
                            mFirstFloatPanel.hideEdit();
                        }
                        if (mSecondFloatPanel.isEditing() && !mSecondFloatPanel.isEmpty()) {
                            mSecondFloatPanel.hideEdit();
                        }
                        return;
                    }
                    if (mOnFloatIconClickListener != null) {
                        mOnFloatIconClickListener.onFloatOutsideClick();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mPanelBg = (ImageView) mPanelLayout.findViewById(R.id.panel_bg);
        mPanelBg.setImageDrawable(null);

        initWarningDialog();

        NowKeyPanelModel.getInstance().initExtraData();

        mFirstFloatPanel = (FloatingBallPanel) mPanelLayout.findViewById(R.id.id_circle_panel_first);
        mFirstFloatPanel.setPage(1);
        mPage1Items = NowKeyPanelModel.getInstance().loadData(1);

        mFirstFloatPanel.setNowKeyData(mPage1Items);
        mFirstFloatPanel.setBackground(mPanelBgImg);
        mFirstCenterView = mPanelLayout.findViewById(R.id.id_circle_menu_item_center_first);
        mFirstCenterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPage = 2;
                mFirstFloatPanel.setVisibility(View.GONE);
                mSecondFloatPanel.setVisibility(View.VISIBLE);
            }
        });

        mSecondFloatPanel = (FloatingBallPanel) mPanelLayout.findViewById(R.id.id_circle_panel_second);
        mSecondFloatPanel.setPage(2);
        mPage2Items = NowKeyPanelModel.getInstance().loadData(2);

        mSecondFloatPanel.setNowKeyData(mPage2Items);
        mSecondFloatPanel.setBackground(mPanelBgImg);
        mSecondCenterView = mPanelLayout.findViewById(R.id.id_circle_menu_item_center_second);
        mSecondCenterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPage = 1;
                mSecondFloatPanel.setVisibility(View.GONE);
                mFirstFloatPanel.setVisibility(View.VISIBLE);
            }
        });

        mPanelParams = new WindowManager.LayoutParams();
        mPanelParams.setTitle("NowKeyPanel");
        //设置window type
        mPanelParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        mPanelParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mPanelParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mPanelParams.gravity = Gravity.CENTER;
        mPanelParams.width = mScreenWidth;
        mPanelParams.height = mScreenRealHeight;
        mPanelParams.y = 0;
    }

    private void initWarningDialog() {
        ////warning dialog begin
        mWarningDialog = (FrameLayout) mPanelLayout.findViewById(R.id.panel_warning_dialog);
        TextView content = (TextView) mWarningDialog.findViewById(R.id.message);
        content.setText(R.string.now_key_item_delete_warning);
        mDialogPositive = (Button) mWarningDialog.findViewById(R.id.positiveButton);
        mDialogPositive.setText(R.string.now_key_item_delete_warning_yes);
        mDialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideWaring(true);
                if (mCurrentPage == 1 && mFirstFloatPanel != null) {
                    mFirstFloatPanel.deleteAllItem();
                } else if (mCurrentPage == 2 && mSecondFloatPanel != null) {
                    mSecondFloatPanel.deleteAllItem();
                }
                if (mOnFloatIconClickListener != null) {
                    mOnFloatIconClickListener.onConfirmDialogClick();
                }
            }
        });
        mDialogNegative = (Button) mWarningDialog.findViewById(R.id.negativeButton);
        mDialogNegative.setText(R.string.now_key_item_delete_warning_no);
        mDialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideWaring(true);
                if (mOnFloatIconClickListener != null) {
                    mOnFloatIconClickListener.onConfirmDialogClick();
                }
            }
        });
        ////warning dialog end
    }

    public void onDestroy() {
        Log.d(TAG, "anxi onDestroy");
        NowKeyPanelModel.getInstance().removeNowKeyModelCallback(this);
        removeAllViews();
        NowKeyPanelModel.getInstance().onDestroy();
        sInstance = null;
        mHideHalfCallback = null;
        mOnFloatIconClickListener = null;

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
        mPanelBgImg = null;
        mIntroduceBg = null;

        mContext = null;
        mApp = null;
        mFloatView = null;
        mIntroduceView = null;
        mPanelLayout = null;
        mWarningDialog = null;
        mDialogPositive = null;
        mDialogNegative = null;
        mCircleView = null;
        mCircleBg = null;
        mTvIntroduce = null;
        mPanelBg = null;
        mBaseHandler = null;

        mLongPressedThread = null;
        mPrevClickThread = null;
        mOnFloatIconUpdateListener = null;
        mScreenShotTask = null;


        if (mPage1Items != null) {
            mPage1Items.clear();
            mPage1Items = null;
        }
        if (mPage2Items != null) {
            mPage2Items.clear();
            mPage2Items = null;
        }
        if (mFirstCenterView != null) {
            mFirstCenterView.setOnClickListener(null);
            mFirstCenterView = null;
        }
        if (mSecondCenterView != null) {
            mSecondCenterView.setOnClickListener(null);
            mSecondCenterView = null;
        }
        if (mFirstFloatPanel != null) {
            mFirstFloatPanel.setBackground(null);
            mFirstFloatPanel.onDestroy();
            mFirstFloatPanel = null;
        }
        if (mSecondFloatPanel != null) {
            mSecondFloatPanel.setBackground(null);
            mSecondFloatPanel.onDestroy();
            mSecondFloatPanel = null;
        }
    }

    /**
     * 保存 悬浮球的 位置
     */
    private void saveFloatBallXY() {
        PreferenceUtils.setNormalFloatBallX(mHistoryX);
        PreferenceUtils.setNormalFloatBallY(mHistoryY);
    }

    /**
     * 获取 悬浮球 的位置
     */
    private void getFloatBallXY() {
        mHistoryX = PreferenceUtils.getNormalFloatBallX(0);
        mHistoryY = PreferenceUtils.getNormalFloatBallY(0);
    }

    public ArrayList<BaseItemInfo> getPage2Items() {
        return mPage2Items;
    }

    public ArrayList<BaseItemInfo> getPage1Items() {
        return mPage1Items;
    }

    public ArrayList<BaseItemInfo> getPage1DragItem() {
        ArrayList<BaseItemInfo> dragItems = new ArrayList<>();
        //add by yanghzong.gong for defect-4433958 begin
        if (mPage1Items != null) {
            for (BaseItemInfo info : mPage1Items) {
                dragItems.add(info);
            }
        }
        //add by yanghzong.gong for defect-4433958 end
        fillEmpty(dragItems);
        return dragItems;
    }

    public ArrayList<BaseItemInfo> getPage2DragItem() {
        ArrayList<BaseItemInfo> dragItems = new ArrayList<>();
        if (mPage2Items != null) {
            for (BaseItemInfo info : mPage2Items) {
                dragItems.add(info);
            }
        }
        fillEmpty(dragItems);
        return dragItems;
    }

    private void stepAside() {
        if (mParams == null) return;
        if (PreferenceUtils.isNormalBallBorder(false)) {
            float viewCenterX = mParams.x + mViewWidth / 2;
            float viewCenterY = mParams.y + mViewHeight / 2;
            float dxLeft = viewCenterX;
            float dyUp = viewCenterY;
            float dxRight = mScreenWidth - dxLeft;
            float dyDown = mScreenHeight - dyUp;
            float result = getMin(dxLeft, dyUp, dxRight, dyDown);

            //设置靠边，有边距
            if (result == dxLeft) {
                mParams.x = mFabViewMinPadding;
            } else if (result == dxRight) {
                mParams.x = mScreenWidth - mViewWidth - mFabViewMinPadding;
            } else if (result == dyUp) {
                mParams.y = 0;
            } else {
                mParams.y = mScreenHeight - mViewHeight - getStatusBarHeight();
            }
            //add by yangzhong.gong for defect-4433958 begin
            try {
                getWindowManager().updateViewLayout(mFloatView, mParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //add by yangzhong.gong for defect-4433958 end
        }

        // modify by junye.li for defect 4230320 begin
        if (mParams != null) {
            mHistoryX = mParams.x;
            mHistoryY = mParams.y;
            saveFloatBallXY();
            mWidthPercent = (float) mHistoryX / (float) mScreenWidth;
            mHeightPercent = (float) mHistoryY / (float) mScreenRealHeight;
        }
        // modify by junye.li for defect 4230320 end
        hideHalf();
    }


    private void fillEmpty(ArrayList<BaseItemInfo> infos) {
        if (infos.size() >= 8) return;
        boolean hasAdded;
        ArrayList<Integer> emptyIndexes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            hasAdded = false;
            for (BaseItemInfo info : infos) {
                if (info.getIndex() == i) {
                    hasAdded = true;
                    break;
                }
            }
            if (!hasAdded) {
                emptyIndexes.add(i);
            }
        }

        for (int index : emptyIndexes) {
            BaseItemInfo itemInfo = new BaseItemInfo();
            itemInfo.setType(0);
            itemInfo.setKey_word("");
            itemInfo.setIndex(index);
            infos.add(itemInfo);
        }

        Collections.sort(infos, new BaseModelComparator());
    }

    /**
     * 没有对悬浮球操作之后，要便透明
     */
    public void hideHalf() {
        if (mFloatView != null) {
            if (mHideHalfCallback == null) {
                mHideHalfCallback = new HideHalfCallback();
            }
            mFloatView.removeCallbacks(mHideHalfCallback);
            mFloatView.postDelayed(mHideHalfCallback, 2000);
        }
    }

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
        getWindowManager().updateViewLayout(mFloatView, mParams);
        mHistoryX = mParams.x;
        mHistoryY = mParams.y;
        mWidthPercent = (float) mHistoryX / (float) mScreenWidth;
        mHeightPercent = (float) mHistoryY / (float) mScreenRealHeight;
    }

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
            getWindowManager().updateViewLayout(mFloatView, mParams);
            showFloatBall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                mStatusBarHeight = mFloatView.getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStatusBarHeight;
    }

    private float getMin(float dxLeft, float dyUp, float dxRight, float dyDown) {
        float a = Math.min(dxLeft, dyUp);
        float b = Math.min(dxRight, dyDown);
        float c = Math.min(a, b);
        return c;
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
     * 显示悬浮球
     */
    public void showFloatBall() {
        Log.d(TAG, "anxi showFloatBall");
        try {
            getWindowManager().addView(mFloatView, mParams);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //add by yangzhong.gong for task-4584915 begin
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFloatView, "alpha", 0f, mCurrentAlpha);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatView, "scaleX", 0.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatView, "scaleY", 0.2f, 1f);

        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.start();
        stepAside();
        //add by yangzhong.gong for task-4584915 end
    }

    public void setonFloatIconClickListener(OnFloatIconClickListener onFloatIconClickListener) {
        mOnFloatIconClickListener = onFloatIconClickListener;
    }

    public void setOnFloatIconUpdateListener(OnFloatIconUpdateListener onFloatIconUpdateListener) {
        mOnFloatIconUpdateListener = onFloatIconUpdateListener;
    }

    public void setOnPanelClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        if (mFirstFloatPanel != null) {
            mFirstFloatPanel.setOnMenuItemClickListener(onMenuItemClickListener);
        }
        if (mSecondFloatPanel != null) {
            mSecondFloatPanel.setOnMenuItemClickListener(onMenuItemClickListener);
        }
    }

    public void updatePage1Position(ArrayList<BaseItemInfo> updates) {
        NowKeyPanelModel.getInstance().updateItemPosition(updates, 1);
    }

    public void updatePage2Position(ArrayList<BaseItemInfo> updates) {
        NowKeyPanelModel.getInstance().updateItemPosition(updates, 2);
    }

    @Override
    public void onNowKeyItemUpdatePosition(ArrayList<BaseItemInfo> items, int page) {
        if (page == 1) {
            mPage1Items = items;
            mFirstFloatPanel.resetNowKeyData(mPage1Items);
        } else if (page == 2) {
            mPage2Items = items;
            mSecondFloatPanel.resetNowKeyData(mPage2Items);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setFloatBallAlpha(mCurrentAlpha);
                //modify by yangzhong.gong for defect-4641685 begin
                if (mFloatView != null && mHideHalfCallback != null) {
                    mFloatView.removeCallbacks(mHideHalfCallback);
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
                //点击次数加1
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
                if (Constant.NOW_KEY_ITEM_TYPE_NONE == PreferenceUtils.getNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_NONE)) {
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
                    // 处理短拉 事件
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
                saveFloatBallXY();
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
            initCircleBg();
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
     * 返回 Nowkey悬浮球边界
     * @return
     */
    public Rect getFloatBallPosition() {
        return new Rect(mHistoryX, mHistoryY, mHistoryX + mViewWidth, mHistoryY + mViewHeight);
    }

    /**
     * 隐藏 短拉手势介绍 的 视图
     * 只显示一次，显示完了之后就释放资源
     */
    private void hideIntroduceView() {
        if (mIntroduceParams == null) return;

        if (PreferenceUtils.isNeedShowShortDragIntroduce(true)) {
            try {
                if (mIntroduceView != null && mIntroduceView.isAttachedToWindow()) {
                    getWindowManager().removeView(mIntroduceView);
                }
                PreferenceUtils.setIsNeedShortDragIntroduce(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public void onNowKeyItemDelete(BaseItemInfo item, int page) {
        Log.d(TAG, "anxi onNowKeyItemDelete");
        if (page == 1) {
            if (item == null) {
                mPage1Items.clear();
                mFirstFloatPanel.updateDeleteAll();
                mCurrentPage = 2;
                mFirstFloatPanel.setVisibility(View.GONE);
                mSecondFloatPanel.setVisibility(View.VISIBLE);
                if (mPage1Items.isEmpty() && mPage2Items.isEmpty()) {
                    if (mOnFloatIconUpdateListener != null) {
                        mOnFloatIconUpdateListener.onFloatFinishDeleteAll();
                    }
                }
                return;
            }
            if (mPage1Items.contains(item)) {
                mPage1Items.remove(item);
                Collections.sort(mPage1Items, new BaseModelComparator());
                mFirstFloatPanel.updateDeleteNowKeyItem(item);
            }
        } else if (page == 2) {
            if (item == null) {
                mPage2Items.clear();
                mSecondFloatPanel.updateDeleteAll();
                mCurrentPage = 1;
                mSecondFloatPanel.setVisibility(View.GONE);
                mFirstFloatPanel.setVisibility(View.VISIBLE);
                if (mPage1Items.isEmpty() && mPage2Items.isEmpty()) {
                    if (mOnFloatIconUpdateListener != null) {
                        mOnFloatIconUpdateListener.onFloatFinishDeleteAll();
                    }
                }
                return;
            }
            if (mPage2Items.contains(item)) {
                mPage2Items.remove(item);
                Collections.sort(mPage2Items, new BaseModelComparator());
                mSecondFloatPanel.updateDeleteNowKeyItem(item);
            }
        }

    }

    @Override
    public void onNowKeyItemAdd(BaseItemInfo item, int page, boolean external) {
        if (item == null) {
            if (mOnFloatIconUpdateListener != null) {
                mOnFloatIconUpdateListener.onFloatFinishUpdate(external);
            }
            return;
        }

        if (mPage1Items == null || mPage1Items.size() == 0) {
            return;
        }

        if (page == 1) {
            // add by junye.li for defect 4417934 begin
            if (item.getIndex() < mPage1Items.size()) {
                BaseItemInfo check = mPage1Items.get(item.getIndex());
                if (check != null && "".equals(check.getKey_word()) && check.getType() == 0) {
                    mPage1Items.remove(check);
                }
            }
            // add by junye.li for defect 4417934 end
            if (!mPage1Items.contains(item)) {
                mPage1Items.add(item);
                Collections.sort(mPage1Items, new BaseModelComparator());
                mFirstFloatPanel.updateAddNowKeyItem(item, external);
            }
        } else if (page == 2) {
            // add by junye.li for defect 4417934 begin
            if (item.getIndex() < mPage2Items.size()) {
                BaseItemInfo check = mPage2Items.get(item.getIndex());
                if (check != null && "".equals(check.getKey_word()) && check.getType() == 0) {
                    mPage2Items.remove(check);
                }
            }
            // add by junye.li for defect 4417934 end
            if (!mPage2Items.contains(item)) {
                mPage2Items.add(item);
                Collections.sort(mPage2Items, new BaseModelComparator());
                mSecondFloatPanel.updateAddNowKeyItem(item, external);
            }
        }
        if (mOnFloatIconUpdateListener != null) {
            mOnFloatIconUpdateListener.onFloatFinishUpdate(external);
        }
    }

    @Override
    public void onNowKeyItemReplace(BaseItemInfo item, int page, boolean external) {
        if (item == null) return;
        if (page == 1 || page == 2) {
            onLocaleChanged();
        }
    }

    @Override
    public void onNowKeyItemUpdate(BaseItemInfo item, int page) {
        if (item == null) return;
        if (item.getKey_word().equals("callacontact")) {
            if (page == 1) {
                mFirstFloatPanel.updateCurrentItem(item);
            } else if (page == 2) {
                mSecondFloatPanel.updateCurrentItem(item);
            }
        }
    }

    public BaseItemInfo getItemFromPackage(String pkg) {
        for (BaseItemInfo info : mPage1Items) {
            if (pkg.equals(info.getKey_word())) return info;
        }
        for (BaseItemInfo info : mPage2Items) {
            if (pkg.equals(info.getKey_word())) return info;
        }
        return null;
    }

    public void deleteItemExternal(BaseItemInfo delete) {
        int page = 0;
        for (BaseItemInfo info : mPage1Items) {
            if (delete.getKey_word().equals(info.getKey_word())) {
                page = 1;
                break;
            }
        }
        if (page == 1) {
            mFirstFloatPanel.deleteItemExternal(delete);
        }
        for (BaseItemInfo info : mPage2Items) {
            if (delete.getKey_word().equals(info.getKey_word())) {
                page = 2;
                break;
            }
        }
        if (page == 2) {
            mSecondFloatPanel.deleteItemExternal(delete);
        }
    }

    public void removeAllViews() {
        Log.d(TAG, "anxi removeAllViews");
        mPanelShowed = false;
        try {
            if (mFloatView != null) {
                getWindowManager().removeView(mFloatView);
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
            if (mPanelLayout != null && mPanelLayout.isAttachedToWindow()) {
                getWindowManager().removeView(mPanelLayout);
                mPanelBg.setImageDrawable(null);
            }
            mFloatView.removeCallbacks(mHideHalfCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //add by yangzhong.gong for task-4584915 begin
        /*AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFloatView, "alpha", mCurrentAlpha / 100, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatView, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatView, "scaleY", 1f, 0f);

        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //mFloatView.setAlpha(0f);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //mFloatView.setAlpha(0f);
                try {
                    mWindowManager.removeView(mFloatView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mWindowManager.removeView(mPanelLayout);
                    mPanelBg.setImageDrawable(null);
                    mFloatView.removeCallbacks(mHideHalfCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                //mFloatView.setAlpha(0f);
                try {
                    mWindowManager.removeView(mFloatView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mWindowManager.removeView(mPanelLayout);
                    mPanelBg.setImageDrawable(null);
                    mFloatView.removeCallbacks(mHideHalfCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();*/
        //add by yangzhong.gong for task-4584915 end
    }

    public void showPanel() {
        View panel = null;
        if (mCurrentPage == 1) {
            panel = mFirstFloatPanel;
        } else if (mCurrentPage == 2) {
            panel = mSecondFloatPanel;
        }

        float offsetX = mParams.x - (mScreenWidth - mPanelWidth) / 2;
        float offsetY = mParams.y - (mScreenHeight - mPanelHeight) / 2;

        if (panel != null) {
            final View fPanel = panel;
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(fPanel, "alpha", 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fPanel, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fPanel, "scaleY", 0f, 1f);
            final ObjectAnimator transX = ObjectAnimator.ofFloat(fPanel, "translationX", offsetX, 0);
            ObjectAnimator transY = ObjectAnimator.ofFloat(fPanel, "translationY", offsetY, 0);

            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY, transX, transY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    fPanel.setPivotX(0.5f);
                    fPanel.setPivotY(0.5f);
                    fPanel.setAlpha(0f);
                    try {
                        if (mFloatView != null && mFloatView.isAttachedToWindow()) {
                            getWindowManager().removeView(mFloatView);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        getWindowManager().addView(mPanelLayout, mPanelParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //startBlur();
                    mPanelShowed = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    fPanel.setAlpha(1f);
                    try {
                        mFirstFloatPanel.onShow();
                        mSecondFloatPanel.onShow();
                    } catch (Exception e){
                      e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    fPanel.setAlpha(1f);
                    try {
                        mFirstFloatPanel.onShow();
                        mSecondFloatPanel.onShow();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();
        }
    }

    public boolean showWaring() {
        if (mWarningDialog == null) return false;
        if (mPage1Items == null || mPage2Items == null) return false;
        if (mPage1Items.size() + mPage2Items.size() > 1) return false;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mWarningDialog, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mWarningDialog, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mWarningDialog, "scaleY", 0f, 1f);

        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mWarningDialog.setPivotX(0.5f);
                mWarningDialog.setPivotY(0.5f);
                mWarningDialog.setAlpha(0f);
                mWarningDialog.setVisibility(View.VISIBLE);
                mFirstFloatPanel.setVisibility(View.GONE);
                mSecondFloatPanel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mWarningDialog.setAlpha(1f);
                mWarningDialog.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mWarningDialog.setAlpha(1f);
                mWarningDialog.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
        return true;
    }

    public void hideWaring(boolean anim) {
        if (mWarningDialog == null) return;
        if (anim) {
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(mWarningDialog, "alpha", 1f, 0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mWarningDialog, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mWarningDialog, "scaleY", 1f, 0f);

            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mWarningDialog.setPivotX(0.5f);
                    mWarningDialog.setPivotY(0.5f);
                    mWarningDialog.setAlpha(1f);
                    mWarningDialog.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mWarningDialog.setAlpha(0f);
                    mWarningDialog.setVisibility(View.GONE);
                    //modify by yangzhong.gong for defect-4641685 begin
                    if (mCurrentPage == 1 && mFirstFloatPanel != null) {
                        mFirstFloatPanel.setVisibility(View.VISIBLE);
                    } else if (mCurrentPage == 2 && mSecondFloatPanel != null) {
                        mSecondFloatPanel.setVisibility(View.VISIBLE);
                    }
                    //modify by yangzhong.gong for defect-4641685 end
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    mWarningDialog.setAlpha(0f);
                    mWarningDialog.setVisibility(View.GONE);
                    if (mCurrentPage == 1) {
                        mFirstFloatPanel.setVisibility(View.VISIBLE);
                    } else if (mCurrentPage == 2) {
                        mSecondFloatPanel.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();
        } else {
            mWarningDialog.setAlpha(0f);
            mWarningDialog.setVisibility(View.GONE);
            if (mCurrentPage == 1) {
                mFirstFloatPanel.setVisibility(View.VISIBLE);
            } else if (mCurrentPage == 2) {
                mSecondFloatPanel.setVisibility(View.VISIBLE);
            }
        }

    }

    public void hidePanel() {
        if (mHidingPanel) return;
        View panel = null;
        if (mCurrentPage == 1) {
            panel = mFirstFloatPanel;
        } else if (mCurrentPage == 2) {
            panel = mSecondFloatPanel;
        }

        float offsetX = mParams.x - (mScreenWidth - mPanelWidth) / 2;
        float offsetY = mParams.y - (mScreenHeight - mPanelHeight) / 2;

        //if (mFirstFloatPanel.isEditing()) mFirstFloatPanel.hideEdit();
        //if (mSecondFloatPanel.isEditing()) mSecondFloatPanel.hideEdit();
        if (panel != null) {
            final View fPanel = panel;
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(fPanel, "alpha", 1f, 0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fPanel, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fPanel, "scaleY", 1f, 0f);
            final ObjectAnimator transX = ObjectAnimator.ofFloat(fPanel, "translationX", 0, offsetX);
            ObjectAnimator transY = ObjectAnimator.ofFloat(fPanel, "translationY", 0, offsetY);

            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY, transX, transY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mPanelShowed = false;
                    mHidingPanel = true;
                    fPanel.setPivotX(0.5f);
                    fPanel.setPivotY(0.5f);
                    fPanel.setAlpha(1f);
                    if (mScreenShotTask != null && !mScreenShotTask.isCancelled()) {
                        mScreenShotTask.cancel(true);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    fPanel.setAlpha(0f);
                    try {
                        if (mPanelLayout != null && mPanelLayout.isAttachedToWindow()) {
                            getWindowManager().removeView(mPanelLayout);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHidingPanel = false;
                    try {
                        mFirstFloatPanel.onFinishHide();
                        mSecondFloatPanel.onFinishHide();
                        mPanelBg.setImageDrawable(null);
                        showFloatBall();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    fPanel.setAlpha(0f);
                    setFloatBallAlpha(mCurrentAlpha / 100);
                    getWindowManager().addView(mFloatView, mParams);
                    try {
                        if (mPanelLayout != null && mPanelLayout.isAttachedToWindow()) {
                            getWindowManager().removeView(mPanelLayout);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHidingPanel = false;
                    mFirstFloatPanel.onFinishHide();
                    mSecondFloatPanel.onFinishHide();
                    mPanelBg.setImageDrawable(null);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();

        }
    }

    /**
     * 设置悬浮求的透明度
     *
     * @param f
     */
    public void setFloatBallAlpha(float f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                mFloatView.setAlpha(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public void showPanelEdit() {
        if (mFirstFloatPanel != null && !mFirstFloatPanel.isEditing()) {
            mFirstFloatPanel.showEdit();
        }
        if (mSecondFloatPanel != null && !mSecondFloatPanel.isEditing()) {
            mSecondFloatPanel.showEdit();
        }
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

    public void resetPanel() {
        NowKeyPanelModel.getInstance().initExtraData();

        mPage1Items = NowKeyPanelModel.getInstance().loadData(1);
        if (mFirstFloatPanel != null) {
            mFirstFloatPanel.setPage(1);
            mFirstFloatPanel.resetNowKeyData(mPage1Items);
        }

        mPage2Items = NowKeyPanelModel.getInstance().loadData(2);
        if (mSecondFloatPanel != null) {
            mSecondFloatPanel.setPage(2);
            mSecondFloatPanel.resetNowKeyData(mPage2Items);
        }
    }

    public void onThemeChange() {
        int color = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME, -1);
        if (color == -1) {
            color = Color.parseColor(Constant.DEFAULT_THEME);
        }
        if (mIconBgImg != null) {
            PorterDuffColorFilter pf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            mIconBgImg.setColorFilter(pf);
            mIconBgView.setImageDrawable(mIconBgImg);
        }
        if (mCirque != null) {
            mCirque.setTint(color);
            mCircleBg.setImageDrawable(mCirque);
        }
        if (mPanelBgImg != null) {
            PorterDuffColorFilter pf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            mPanelBgImg.setColorFilter(pf);
            mFirstFloatPanel.onThemeChanged(color);
            mSecondFloatPanel.onThemeChanged(color);
        }
    }

    private int getThemeColor() {
        int color = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME, -1);
        if (color == -1) {
            color = Color.parseColor(Constant.DEFAULT_THEME);
        }
        return color;
    }

    /**
     * 当数据变化时调用这个方法，重置转盘
     */
    public void onLocaleChanged() {
        NowKeyPanelModel.getInstance().initExtraData();

        mPage1Items = NowKeyPanelModel.getInstance().loadData(1);
        if (mFirstFloatPanel != null) {
            mFirstFloatPanel.setPage(1);
            mFirstFloatPanel.resetNowKeyData(mPage1Items);
        }

        mPage2Items = NowKeyPanelModel.getInstance().loadData(2);
        if (mSecondFloatPanel != null) {
            mSecondFloatPanel.setPage(2);
            mSecondFloatPanel.resetNowKeyData(mPage2Items);
        }

        if (mWarningDialog != null) {
            mWarningDialog.invalidate();
            if (mWarningDialog.getVisibility() == View.VISIBLE) {
                hideWaring(false);
            }
            mWarningDialog = null;
        }
        if (mDialogNegative != null) {
            mDialogNegative.setOnClickListener(null);
        }
        if (mDialogPositive != null) {
            mDialogPositive.setOnClickListener(null);
        }
        initWarningDialog();
    }

    public boolean isPanelEdit() {
        if (mFirstFloatPanel == null && mSecondFloatPanel == null) {
            return false;
        }
        if (mFirstFloatPanel != null && mFirstFloatPanel.isEditing()) {
            return true;
        }
        if (mSecondFloatPanel != null && mSecondFloatPanel.isEditing()) {
            return true;
        }
        return false;
    }

    public void exitPanelEdit() {
        if (mFirstFloatPanel.isEditing() && !mFirstFloatPanel.isEmpty()) {
            mFirstFloatPanel.hideEdit();
        }
        if (mSecondFloatPanel.isEditing() && !mSecondFloatPanel.isEmpty()) {
            mSecondFloatPanel.hideEdit();
        }
    }

    /**
     * 屏幕旋转调用
     */
    public void onConfigChange() {
        setFloatBallAlpha(mCurrentAlpha);
        getWindowManager();
        mScreenWidth = getScreenWidth(mContext);
        mScreenHeight = getScreenHeight(mContext);
        mScreenRealHeight = getDpi(mContext);

        if (mHistoryX == 0 && mHistoryY == 0) {
            mParams.x = mScreenWidth;
            mParams.y = mScreenHeight / 3;
        } else {
            mParams.x = (int) (mScreenWidth * mWidthPercent);
            mParams.y = (int) (mScreenHeight * mHeightPercent);
        }

        mPanelParams.width = mScreenWidth;
        mPanelParams.height = mScreenRealHeight;

        try {
            getWindowManager().updateViewLayout(mFloatView, mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stepAside();
    }

    public void setBorderOptions() {
        stepAside();
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
     * 变淡的动画
     */
    private class HideHalfCallback implements Runnable {

        public HideHalfCallback() {
        }

        @Override
        public void run() {
            if (null != mParams) {
                mCurrentAlpha = PreferenceUtils.getFloat(PreferenceUtils.NOW_KEY_FLOAT_VIEW_OPACITY, 50f) / 100f;
                if (mCurrentAlpha > 0.3f) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    ObjectAnimator alpha = ObjectAnimator.ofFloat(mFloatView, "alpha", mCurrentAlpha, 0.3f);
                    animatorSet.setDuration(1000);
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
                saveFloatBallXY();
            }
        }
    }

/*    private void startBlur() {
        final AsyncTask<Void, Drawable, Drawable> screenshotTask = new AsyncTask<Void, Drawable, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... voids) {
                Bitmap bmp = getScreenshot();
                if (bmp != null) {
//                    Bitmap blurScreen = fastblur(comp(bmp), 12);
                    Drawable drawable = new BitmapDrawable(mContext.getResources(),
                            BlurBitmap.blur(mContext, bmp) *//*comp(bmp)*//*);
                    return drawable;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Drawable... values) {

            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                if (this != mScreenShotTask) return;
                if (drawable != null && mPanelLayout != null && !mHidingPanel) {
                    int[] location = new int[2];
                    mPanelLayout.getLocationOnScreen(location);
                    if (location[1] != 0) {
                        mPanelParams.y = -location[1];
                        try {
                            getWindowManager().updateViewLayout(mPanelLayout, mPanelParams);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    addBlurBg(drawable);
//                    mPanelBg.setImageDrawable(drawable);
                }
            }
        };
        if (mScreenShotTask != screenshotTask) {
            mScreenShotTask = screenshotTask;
            screenshotTask.execute();
        }
    }*/


    public void addBlurBg(final Drawable drawableBg) {
        if (mPanelBg != null) {
            final ImageView fPanel = mPanelBg;
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(fPanel, "alpha", 0f, 1f);
            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(alpha);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    fPanel.setAlpha(0f);
                    fPanel.setImageDrawable(drawableBg);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    fPanel.setAlpha(1f);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    fPanel.setAlpha(1f);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();

        }
    }


    private Bitmap getScreenshot() {
        try {
            Class<?> testClass = Class.forName("android.view.SurfaceControl");

            Method saddMethod1 = testClass.getMethod("screenshot", new Class[]{int.class, int.class});

            Bitmap bmp = (Bitmap) saddMethod1.invoke(null, new Object[]{mScreenWidth, mScreenRealHeight});
            if (bmp != null) {
                return bmp;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;

        int scale = 15;
        if (image.getWidth() > 500) {
            scale = image.getWidth() / 500;
        }

        newOpts.inSampleSize = scale;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}