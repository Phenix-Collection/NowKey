package com.kuding.nowkey.floatview;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kuding.nowkey.FunctionActivity;
import com.kuding.nowkey.Utils.BaseModelComparator;
import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.Utils.Utils;
import com.kuding.nowkey.info.AppItemInfo;
import com.kuding.nowkey.info.BaseItemInfo;
import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.interfaces.OnMenuItemClickListener;
import com.kuding.nowkey.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by user on 17-1-9.
 */

public class FloatingBallPanel extends ViewGroup {
    private static final String TAG = "FloatingBallPanel";
    private int mRadius;
    /**
     * 该容器内child item的默认尺寸
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 6f;
    /**
     * 菜单的中心child的默认尺寸
     */
    private float RADIO_DEFAULT_CENTERITEM_DIMENSION = 1 / 3f;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     * 如果移动角度达到该值，则屏蔽点击
     */
    private static final int NOCLICK_VALUE = 10;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private int mFlingableValue = FLINGABLE_VALUE;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private float mPadding;

    /**
     * 布局时的开始角度
     */
    private double mStartAngle = 0;

//    /**
//     * 菜单的个数
//     */
//    private int mMenuItemCount;

    /**
     * 检测按下到抬起时旋转的角度
     */
    private float mTmpAngle;
    /**
     * 检测按下到抬起时使用的时间
     */
    private long mDownTime;

    /**
     * 判断是否正在自动滚动
     */
    private boolean isFling;

    /**
     * 判断是否显示删除图标
     */
    private boolean mShowingDelete = false;

    private Context mContext;

    private VectorDrawable mDeleteIconBg;

    private ArrayList<BaseItemInfo> mItemInfos = new ArrayList<>();
    private BaseItemInfo mDeleteItem = null;

    private boolean mTouchMoving;
    private boolean mStartingAction;

    private int mPage = 1;
    private int PANEl_PAGE_ITEM_SIZE = 8;
    private int mIconImgHeight;
    private int mIconImgWidth;
    private int mChildItemHeight;
    private int mChildItemWidth;

    public FloatingBallPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mIconImgHeight = Utils.dip2px(context, 48);
        mIconImgWidth = Utils.dip2px(context, 48);
        mChildItemHeight = Utils.dip2px(context, 68);
        mChildItemWidth = Utils.dip2px(context, 72);
        mPadding = Utils.dip2px(context, 10);
        mDeleteIconBg = (VectorDrawable) context.getDrawable(R.drawable.delete_icon_bg_drawable);
        // 无视padding
        setPadding(0, 0, 0, 0);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuItemClickListener != null) {
                    mOnMenuItemClickListener.panelClick();
                }
            }
        });
    }

    /**
     * 设置布局的宽高，并策略menu item宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = 0;
        int resHeight = 0;

        /**
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            resWidth = width;
            resHeight = height;
        }

        setMeasuredDimension(resWidth, resHeight);

        // 获得半径
        mRadius = getMeasuredWidth();

        // menu item数量
        final int count = getChildCount();
        // menu item尺寸
//        int childSize = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeasureSpec = -1;
            int makeMeausreSpecWidth = -1;
            int makeMeausreSpecHeight = -1;

            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(
                        (int) (mRadius * RADIO_DEFAULT_CENTERITEM_DIMENSION),
                        childMode);
                child.measure(makeMeasureSpec, makeMeasureSpec);
            } else {
                makeMeausreSpecWidth = MeasureSpec.makeMeasureSpec(mChildItemWidth,
                        childMode);
                makeMeausreSpecHeight = MeasureSpec.makeMeasureSpec(mChildItemHeight,
                        childMode);
                child.measure(makeMeausreSpecWidth, makeMeausreSpecHeight);
            }
        }
    }

    /**
     * 设置menu item的位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutRadius = mRadius;

        // Laying out the child views
        final int childCount = getChildCount();

        int count = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            count++;
        }

        final int visibleChildCount = count;

        int left, top;
        // menu item 的尺寸
        int cWidth = mChildItemWidth;
        int cHeight = mChildItemHeight;

        // 根据menu item的个数，计算角度
        int angleDelay = visibleChildCount == 1 ? 360 : 360 / (visibleChildCount - 1);

        // 遍历去设置menuitem的位置
        boolean first = false;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second)
                continue;

            if (child.getVisibility() == GONE) {
                continue;
            }
            mStartAngle %= 360;

            // 计算，中心点到menu item中心的距离
            float tmp = layoutRadius / 2f - cWidth / 2 - mPadding;

            // tmp cosa 即menu item中心点的横坐标
            left = layoutRadius
                    / 2
                    + (int) Math.round(tmp
                    * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f
                    * cWidth);
            // tmp sina 即menu item的纵坐标
            top = layoutRadius
                    / 2
                    + (int) Math.round(tmp
                    * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f
                    * cHeight);

            child.layout(left, top, left + cWidth, top + cHeight);
            // 叠加尺寸
            if (visibleChildCount == 8 && !first) {
                angleDelay = 54;
                first = true;
            } else if (visibleChildCount == 8) {
                angleDelay = 51;
            }
            mStartAngle += angleDelay;

        }

        // 找到中心的view，如果存在设置onclick事件
        View cView = findViewById(R.id.id_circle_menu_item_center_first);
        if (cView == null) {
            cView = findViewById(R.id.id_circle_menu_item_center_second);
        }
        if (cView != null) {
            // 设置center item位置
            int cl = layoutRadius / 2 - cView.getMeasuredWidth() / 2;
            int cr = cl + cView.getMeasuredWidth();
            cView.layout(cl, cl, cr, cr);
        }
    }

    /**
     * 记录上一次的x，y坐标
     */
    private float mLastX;
    private float mLastY;

    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchMoving = false;

                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;

                // 如果当前已经在快速滚动
                if (isFling) {
                    // 移除快速滚动的回调
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
//                    return true;
                }

                break;
            case MotionEvent.ACTION_MOVE:

                /**
                 * 获得开始的角度
                 */
                float start = getAngle(mLastX, mLastY);

                /**
                 * 获得当前的角度
                 */
                float end = getAngle(x, y);

                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mTmpAngle += end - start;
                } else
                // 二、三象限，色角度值是付值
                {
                    mTmpAngle += start - end;
                }

                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchMoving = false;

                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;

                // 如果当前已经在快速滚动
                if (isFling) {
                    // 移除快速滚动的回调
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
                    return true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                /**
                 * 获得开始的角度
                 */
                float start = getAngle(mLastX, mLastY);

                /**
                 * 获得当前的角度
                 */
                float end = getAngle(x, y);

                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle += end - start;
                    mTmpAngle += end - start;
                } else
                // 二、三象限，色角度值是付值
                {
                    mStartAngle += start - end;
                    mTmpAngle += start - end;
                }

                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mTmpAngle) != 0) {
                    requestLayout();
                    mLastX = x;
                    mLastY = y;
                }
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    mTouchMoving = true;
                }
                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:
                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000
                        / (System.currentTimeMillis() - mDownTime);


                // 如果达到该值认为是快速移动
                if (Math.abs(anglePerSecond) > mFlingableValue && !isFling) {
                    // post一个任务，去自动滚动
                    post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));

                    return true;
                }

                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (mTouchMoving) {
                    return true;
                }

                mTouchMoving = false;

                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mOnMenuItemClickListener != null) {
                mOnMenuItemClickListener.startTouching();
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP
                || ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mOnMenuItemClickListener != null) {
                mOnMenuItemClickListener.finishTouching();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mRadius / 2d);
        double y = yTouch - (mRadius / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mRadius / 2);
        int tmpY = (int) (y - mRadius / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }

    }


    /**
     * 获得默认该layout的尺寸
     *
     * @return
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }


    /**
     * init data
     *
     * @param itemInfos
     */
    public void setNowKeyData(ArrayList<BaseItemInfo> itemInfos) {
        if (itemInfos == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        mItemInfos.clear();
        Collections.sort(itemInfos, new BaseModelComparator());
        mItemInfos.addAll(Utils.convertBaseItemInfos(mContext, itemInfos));
        addMenuItems();
    }

    public void resetNowKeyData(ArrayList<BaseItemInfo> itemInfos) {
        if (itemInfos == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        mItemInfos.clear();
        Collections.sort(itemInfos, new BaseModelComparator());
        mItemInfos.addAll(Utils.convertBaseItemInfos(mContext, itemInfos));
        int childCount = getChildCount();
        View child;
        ArrayList<View> itemChild = new ArrayList<View>();
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                continue;
            }
            itemChild.add(child);
        }
        for (View item : itemChild) {
            removeView(item);
        }
        addMenuItems();
    }

    public void updateAddNowKeyItem(BaseItemInfo added, boolean external) {
        BaseItemInfo converted = Utils.convertBaseItemInfo(mContext, added);
        if (converted == null) {
            showEdit();
            return;
        }
        if (!mItemInfos.contains(converted)) {
            mItemInfos.add(converted);
            Collections.sort(mItemInfos, new BaseModelComparator());
            int index = converted.getIndex();
            final View child = getChildAt(index + 1);
            //add by yangzhong.gong for defect-4433958 begin
            if (child == null) {
                return;
            }
            //add by yangzhong.gong for defect-4433958 end
            final View v = child.findViewById(R.id.app_layout);
            if (v != null && v instanceof FloatItemView) {
                FloatItemView floatItemView = (FloatItemView) v;
                if (floatItemView.getInfo() == null) {
                    floatItemView.setInfo(converted);
                    floatItemView.setIndex(index);
                    final ImageView icon = (ImageView) child.findViewById(R.id.app_icon_img);
                    final TextView tv = (TextView) child.findViewById(R.id.app_icon);
                    final FrameLayout delete = (FrameLayout) child.findViewById(R.id.delete_icon);
                    final ImageView addIcon = (ImageView) child.findViewById(R.id.add_icon);
                    delete.setVisibility(GONE);
                    addIcon.setVisibility(GONE);
                    floatItemView.setVisibility(VISIBLE);
                    child.setVisibility(VISIBLE);
                    if (converted instanceof FunctionItemInfo) {
                        FunctionItemInfo fInfo = (FunctionItemInfo) converted;
                        Drawable d = fInfo.getIcon();
                        d.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                        icon.setImageDrawable(d);
                        tv.setText(((FunctionItemInfo) converted).getText());

                        fInfo.setIconView(icon);
                        fInfo.setTitleView(tv);
                        fInfo.onAdd(mContext);

                    } else if (converted instanceof AppItemInfo) {
                        Drawable d = ((AppItemInfo) converted).getIcon();
                        d.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                        icon.setImageDrawable(d);
                        tv.setText(((AppItemInfo) converted).getText());
                    } else {
                        Log.e("NowKey", "Type error!!! " + converted.toString());
                        return;
                    }
                    setVisibleItemListener(floatItemView, delete, addIcon, converted);
                    mStartAngle = 0;
                    mTmpAngle = 0;
                    requestLayout();
                } else {
                    Log.e("NowKey", "this is not an empty view!!! " + floatItemView.getInfo().toString());
                    return;
                }
            } else {
                Log.e("NowKey", "It is not FloatItemView!!!");
                return;
            }
        } else {
            Log.e("NowKey", "It is not FloatItemView!!! " + converted.toString());
            return;
        }
        if (!external) {
            showEdit();
        }
    }

    public void updateDeleteNowKeyItem(BaseItemInfo delete) {
        BaseItemInfo converted = Utils.convertBaseItemInfo(mContext, delete);
        if (converted == null && mDeleteItem != null) {
            converted = mDeleteItem;
            mDeleteItem = null;
        }
        if (mItemInfos.contains(converted)) {
            mItemInfos.remove(delete);
            Collections.sort(mItemInfos, new BaseModelComparator());
        }
        if (!mItemInfos.isEmpty()) {
            updateDeleteView();
        } else {
            updateDeleteDeleteAllView();
        }
    }

    public void updateDeleteAll() {
        mItemInfos.clear();
        updateDeleteDeleteAllView();
    }

    public void updateCurrentItem(BaseItemInfo update) {
        BaseItemInfo converted = Utils.convertBaseItemInfo(mContext, update);
        if (converted == null) {
            return;
        }
        if (mItemInfos.contains(converted)) {
            int index = converted.getIndex();
            final View child = getChildAt(index + 1);
            final View v = child.findViewById(R.id.app_layout);
            if (v != null && v instanceof FloatItemView) {
                final TextView tv = (TextView) child.findViewById(R.id.app_icon);
                if (converted instanceof FunctionItemInfo) {
                    tv.setText(((FunctionItemInfo) converted).getText());
                }
            }
        }
    }

    public void setPage(int page) {
        mPage = page;
    }

    public void onShow() {
        if (mItemInfos == null || mItemInfos.isEmpty()) {
            showEdit();
        }
    }

    public void onFinishHide() {
        mStartingAction = false;
    }

    public void onThemeChanged(int color) {
        PorterDuffColorFilter pf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mDeleteIconBg.setColorFilter(pf);
    }


    /**
     * 添加菜单项
     */
    private void addMenuItems() {
        final int infoItemCount = mItemInfos.size();
        int themeColor = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME, -1);
        if (themeColor == -1) {
            themeColor = Color.parseColor(Constant.DEFAULT_THEME);
        }
        PorterDuffColorFilter pf = new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
        mDeleteIconBg.setColorFilter(pf);

        // modify by junye.li for defect 4417934 begin
        BaseItemInfo[] addedInfos = new BaseItemInfo[PANEl_PAGE_ITEM_SIZE];
        /**
         * 根据用户设置的参数，初始化view
         */
        for (int i = 0; i < infoItemCount; i++) {
            final BaseItemInfo bInfo = mItemInfos.get(i);
            addedInfos[bInfo.getIndex()] = bInfo;
        }

        String callContact = mContext.getString(R.string.now_key_function_alcatel_callacontact);

        for (int i = 0; i < addedInfos.length; i++) {
            final int j = i;
            final BaseItemInfo added = addedInfos[j];
            final RelativeLayout layout = (RelativeLayout) View.inflate(mContext, R.layout.circle_menu_item, null);
            final FloatItemView appLayout = (FloatItemView) layout.findViewById(R.id.app_layout);
            final ImageView icon = (ImageView) layout.findViewById(R.id.app_icon_img);
            final TextView tv = (TextView) layout.findViewById(R.id.app_icon);
            final FrameLayout delete = (FrameLayout) layout.findViewById(R.id.delete_icon);
            final ImageView deleteBg = (ImageView) layout.findViewById(R.id.delete_icon_bg);
            deleteBg.setImageDrawable(mDeleteIconBg);
            final ImageView addIcon = (ImageView) layout.findViewById(R.id.add_icon);

            if (added != null) {
                if (added instanceof FunctionItemInfo) {
                    FunctionItemInfo fInfo = (FunctionItemInfo) added;
                    appLayout.setInfo(fInfo);
                    tv.setText(fInfo.getText());
                    Drawable d = fInfo.getIcon();
                    d.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                    icon.setImageDrawable(d);
                    fInfo.setIconView(icon);
                    fInfo.setTitleView(tv);
                    fInfo.onAdd(mContext);
                } else if (added instanceof AppItemInfo) {
                    AppItemInfo aInfo = (AppItemInfo) added;
                    appLayout.setInfo(aInfo);
                    tv.setText(aInfo.getText());
                    Drawable d = aInfo.getIcon();
                    d.setBounds(0, 0, mIconImgWidth, mIconImgHeight);
                    icon.setImageDrawable(d);
                }
                appLayout.setVisibility(View.VISIBLE);
                appLayout.setIndex(added.getIndex());
                setVisibleItemListener(appLayout, delete, addIcon, added);
            } else {
                layout.setVisibility(GONE);
                appLayout.setInfo(null);
                appLayout.setIndex(j);
                addIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnMenuItemClickListener != null) {
                            mOnMenuItemClickListener.addItemClick();
                        }
                        Intent i = new Intent();
                        i.setClass(mContext, FunctionActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.NOW_KEY_ACTION);
                        i.putExtra(Constant.NOW_KEY_ITEM_INDEX, j);
                        i.putExtra(Constant.NOW_KEY_ITEM_PAGE, mPage);
                        mContext.startActivity(i);
                    }
                });
            }
            // 添加view到容器中
            addView(layout, j + 1);
        }
    }

    private void setVisibleItemListener(final FloatItemView layout, final FrameLayout delete,
                                        final ImageView addIcon, final BaseItemInfo bInfo) {
        final int index = bInfo.getIndex();
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShowingDelete && !mTouchMoving && !mItemInfos.isEmpty()) {
                    hideEdit();
                } else if (!mTouchMoving && view instanceof FloatItemView && !mStartingAction) {
                    mStartingAction = true;
                    BaseItemInfo info = ((FloatItemView) view).getInfo();
                    if (info != null) {
                        if (info instanceof AppItemInfo) {
                            //modify by yangzhong.gong for defect-4455564 begin
                            try {
                                Intent intent = ((AppItemInfo) info).getIntent();
                                if (intent == null && Utils.checkPackageExist(mContext, info.getKey_word())) {
                                    intent = Utils.getApplicationIntent(info.getKey_word(), mContext.getPackageManager());
                                }
                                //modify by yangzhong.gong for defect-5132154 begin
                                if (info.getKey_word().equals("com.tct.email")) {
                                    intent = new Intent();
                                    ComponentName cn = new ComponentName("com.tct.email", "com.tct.email.activity.Welcome");
                                    intent.setComponent(cn);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //modify by yangzhong.gong for defect-5132154 end
                                mContext.startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(mContext,
                                        R.string.now_key_app_not_exist, Toast.LENGTH_SHORT).show();
                            }
                            //modify by yangzhong.gong for defect-4455564 end
                        } else if (info instanceof FunctionItemInfo) {
                            if (info instanceof FunctionItemInfo.FunctionCallAContact) {
                                ((FunctionItemInfo.FunctionCallAContact) info).doAction(
                                        mContext, index, mPage);
                            } else {
                                ((FunctionItemInfo) info).doAction(mContext);
                            }
                        }
                    }

                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.itemClick();
                    }
                }
            }
        });
       /* layout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!mShowingDelete && !mTouchMoving) {
                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.onItemLongClick();
                    }
                    return true;
                }
                return false;
            }
        });
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTouchMoving) {
                    boolean showWaring = false;
                    if (mOnMenuItemClickListener != null) {
                        showWaring = mOnMenuItemClickListener.showWarning();
                    }
                    if (!showWaring) {
                        if (bInfo != null && bInfo instanceof FunctionItemInfo) {
                            ((FunctionItemInfo) bInfo).onDelete(mContext);
                        }
                        mDeleteItem = bInfo;
                        layout.setInfo(null);
                        NowKeyPanelModel.getInstance(mContext).deleteItem(bInfo, mPage);
                    }
                }
            }
        });
        addIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShowingDelete && !mTouchMoving) {
//                    hideEdit();
                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.addItemClick();
                    }
                    Intent i = new Intent();
                    i.setClass(mContext, FunctionActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.NOW_KEY_ACTION);
                    i.putExtra(Constant.NOW_KEY_ITEM_INDEX, index);
                    i.putExtra(Constant.NOW_KEY_ITEM_PAGE, mPage);
                    mContext.startActivity(i);
                }
            }
        });*/
    }

    public void showEdit() {
        mShowingDelete = true;
        int childCount = getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                continue;
            }
            View icon = child.findViewById(R.id.app_layout);
            View delete = child.findViewById(R.id.delete_icon);
            View add = child.findViewById(R.id.add_icon);
            if (icon instanceof FloatItemView) {
                FloatItemView floatItemView = (FloatItemView) icon;
                BaseItemInfo info = floatItemView.getInfo();
                if (child.getVisibility() == GONE || info == null) {
                    delete.setVisibility(GONE);
                    icon.setVisibility(GONE);
                    add.setVisibility(VISIBLE);
                    child.setVisibility(VISIBLE);
                } else {
                    delete.setVisibility(VISIBLE);
                    icon.setVisibility(VISIBLE);
                    add.setVisibility(GONE);
                }
            }
        }
    }

    public void hideEdit() {
        mShowingDelete = false;
        int childCount = getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                continue;
            }
            View icon = child.findViewById(R.id.app_layout);
            View delete = child.findViewById(R.id.delete_icon);
            View add = child.findViewById(R.id.add_icon);
            if (add.getVisibility() == VISIBLE) {
                child.setVisibility(GONE);
            } else {
                delete.setVisibility(GONE);
                icon.setVisibility(VISIBLE);
                add.setVisibility(GONE);
                child.setVisibility(VISIBLE);
            }
        }
    }

    public boolean isEditing() {
        return !mItemInfos.isEmpty() && mShowingDelete;
    }

    public boolean isEmpty() {
        return mItemInfos.isEmpty();
    }

    public void deleteItemExternal(BaseItemInfo delete) {
        for (BaseItemInfo info : mItemInfos) {
            if (info.getKey_word().equals(delete.getKey_word())) {
                final View child = getChildAt(delete.getIndex() + 1);
                //add by yangzhong.gong for defect-4433958 begin
                if (child == null) {
                    break;
                }
                //add by yangzhong.gong for defect-4433958 end
                final View v = child.findViewById(R.id.app_layout);
                if (v != null && v instanceof FloatItemView) {
                    if (delete != null && delete instanceof FunctionItemInfo) {
                        ((FunctionItemInfo) delete).onDelete(mContext);
                    }
                    mDeleteItem = delete;
                    ((FloatItemView) v).setInfo(null);
                    NowKeyPanelModel.getInstance().deleteItem(delete, mPage);
                }
                break;
            }
        }

    }

    private void updateDeleteView() {
        int childCount = getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                continue;
            }
            View icon = child.findViewById(R.id.app_layout);
            View delete = child.findViewById(R.id.delete_icon);
            View add = child.findViewById(R.id.add_icon);
            if (icon instanceof FloatItemView) {
                FloatItemView floatItemView = (FloatItemView) icon;
                BaseItemInfo info = floatItemView.getInfo();
                if (info == null) {
                    delete.setVisibility(GONE);
                    icon.setVisibility(GONE);
                    add.setVisibility(VISIBLE);
                    if (mShowingDelete) {
                        child.setVisibility(VISIBLE);
                    } else {
                        child.setVisibility(GONE);
                    }
                } else {
                    if (mShowingDelete) {
                        delete.setVisibility(VISIBLE);
                        icon.setVisibility(VISIBLE);
                        add.setVisibility(GONE);
                    } else {
                        delete.setVisibility(GONE);
                        icon.setVisibility(VISIBLE);
                        add.setVisibility(GONE);
                    }
                    child.setVisibility(VISIBLE);
                }
            }
        }
    }

    private void updateDeleteDeleteAllView() {
        int childCount = getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                continue;
            }
            View icon = child.findViewById(R.id.app_layout);
            View delete = child.findViewById(R.id.delete_icon);
            View add = child.findViewById(R.id.add_icon);
            if (icon instanceof FloatItemView) {
                FloatItemView floatItemView = (FloatItemView) icon;
                BaseItemInfo info = floatItemView.getInfo();
                if (info != null) {
                    if (info instanceof FunctionItemInfo) {
                        ((FunctionItemInfo) info).onDelete(mContext);
                    }
                    floatItemView.setInfo(null);
                }
                delete.setVisibility(GONE);
                icon.setVisibility(GONE);
                add.setVisibility(VISIBLE);
            }
            child.setVisibility(VISIBLE);
        }
    }

    public void deleteAllItem() {
        NowKeyPanelModel.getInstance().deleteAll(mPage);
    }


    /**
     * 自动滚动的任务
     */
    private class AutoFlingRunnable implements Runnable {

        private float angelPerSecond;

        public AutoFlingRunnable(float velocity) {
            this.angelPerSecond = velocity;
        }

        public void run() {
            // 如果小于20,则停止
            if ((int) Math.abs(angelPerSecond) < 20) {
                isFling = false;
                return;
            }
            isFling = true;
            // 不断改变mStartAngle，让其滚动，/30为了避免滚动太快
            mStartAngle += (angelPerSecond / 30);
            // 逐渐减小这个值
            angelPerSecond /= 1.0666F;
            postDelayed(this, 30);
            // 重新布局
            requestLayout();
        }
    }

    /**
     * MenuItem的点击事件接口
     */
    private OnMenuItemClickListener mOnMenuItemClickListener;

    /**
     * 设置MenuItem的点击事件接口
     *
     * @param mOnMenuItemClickListener
     */
    public void setOnMenuItemClickListener(
            OnMenuItemClickListener mOnMenuItemClickListener) {
        this.mOnMenuItemClickListener = mOnMenuItemClickListener;
    }

    public void onDestroy() {
        int childCount = getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center_first
                    || child.getId() == R.id.id_circle_menu_item_center_second) {
                continue;
            }
            View icon = child.findViewById(R.id.app_layout);
            if (icon instanceof FloatItemView) {
                FloatItemView floatItemView = (FloatItemView) icon;
                BaseItemInfo info = floatItemView.getInfo();
                if (info != null) {
                    if (info instanceof FunctionItemInfo) {
                        ((FunctionItemInfo) info).onDelete(mContext);
                    }
                    floatItemView.setInfo(null);
                }
            }
        }
        removeAllViews();
        mDeleteIconBg = null;
        mOnMenuItemClickListener = null;
        mContext = null;
        mDeleteItem = null;

        if (mItemInfos != null) {
            mItemInfos.clear();
            mItemInfos = null;
        }
    }

}
