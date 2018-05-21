package com.kuding.nowkey.floatview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class MiniBallPanel extends ViewGroup {

    private static final String TAG = "MiniBallPanel";

    private int mLayoutWidth;                           // 布局的宽，因为是正方形，所以高和宽一样
    private static final int FLINGABLE_VALUE = 300;     // 当每秒移动角度达到该值时，认为是快速移动
    private static final int NOCLICK_VALUE = 10;        // 如果移动角度达到该值，则屏蔽点击
    private float mPadding;                             // 该容器的内边距,无视padding属性，如需边距请用该变量
    private double mStartAngle = 270;                   // 布局时的开始角度
    private double[] mStartAngleStandard;               // 标准的开始角度,根据子控件的个数,计算出标准的角度，当停止转动之后，可以停止在标准角度
    private float mTmpAngle;                            // 检测按下到抬起时旋转的角度
    private long mDownTime;                             // 检测按下到抬起时使用的时间
    private boolean isFling;                            // 判断是否正在自动滚动
    private boolean mTouchMoving;                       // 是否触摸移动
    private boolean mStartingAction;
    public boolean mIsAlignStart = true;                // 悬浮球 是否靠在开始那一边（左边）
    private int mMenuItemCount = 8;                     // 子菜单的个数
    private int mIconImgWidth;                          // 子菜单图标的宽度
    private int mChildItemWidth;                        // 子菜单的宽度

    private float mLastX;                               // 记录上一次的x坐标
    private float mLastY;                               // 记录上一次的y坐标

    int mAnimationMode = 1;                             // 动画的模式 1，2，3三种,默认是第三种
    private Context mContext;

    public ArrayList<BaseItemInfo> mItemInfos = new ArrayList<>();  // 子菜单信息
    public ArrayList<RelativeLayout> mItemView = new ArrayList<>(); // 子菜单的布局
    private ArrayList<Integer> mAnimaPlayLists;          //动画播放顺序的列表
    private VectorDrawable mItemBg;                      // 子菜单的背景图片

    public MiniBallPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mIconImgWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_child_icon_height);
        mChildItemWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_child_height);
        mPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_pendding);

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
        initStandardAngle();
        initData();
        mLayoutWidth = getMeasuredWidth();
    }

    private void initData() {
        mItemBg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_icon_bg_73);
    }

    /**
     * 根据子菜单的个数，初始化标准角度
     */
    private void initStandardAngle() {
        if (mMenuItemCount > 5) {
            //根据自控件的数量计算标准的角度
            mStartAngleStandard = new double[mMenuItemCount];
            int angleDelay = 360 / (mMenuItemCount);
            for (int i = 0; i < mMenuItemCount; i++) {
                mStartAngleStandard[i] = (i * angleDelay) % 360;
            }
        }
    }

    /**
     * 设置布局的宽高，并策略menu item宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.d(TAG, "onMeasure");
        int resWidth = 0;
        int resHeight = 0;

        if (mMenuItemCount == 0) return;

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

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
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
        mLayoutWidth = getMeasuredWidth();
        // menu item数量
        final int count = getChildCount();
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeausreSpecWidth = MeasureSpec.makeMeasureSpec(mChildItemWidth, childMode);
            int makeMeausreSpecHeight = MeasureSpec.makeMeasureSpec(mChildItemWidth, childMode);
            child.measure(makeMeausreSpecWidth, makeMeausreSpecHeight);
        }
    }

    /**
     * 设置menu item的位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //Log.d(TAG, "onLayout");

        if (mMenuItemCount == 0) return;

        int layoutRadius = mLayoutWidth;
        final int childCount = getChildCount();

        //Log.d(TAG, "childCount = " + childCount);

        // 根据菜单的个数，计算间隔角度
        int angleDelay = 0;
        // 用反正切求出偏移的角度
        int offsetAngle = (int) (Math.round(Math.toDegrees(Math.atan2(mChildItemWidth / 2 + mPadding, layoutRadius / 2))));
        //Log.d(TAG, " offsetAngle = " + offsetAngle);
        // 子控件的数量不同，初始的角度不同
        if (mMenuItemCount == 8) {
            angleDelay = mMenuItemCount == 1 ? 360 : 360 / (mMenuItemCount);
        } else if (mMenuItemCount == 5) {
            mStartAngle = 270;
            angleDelay = 180 / (mMenuItemCount - 1);
        } else if (mMenuItemCount == 4 || mMenuItemCount == 3) {
            angleDelay = (180 + 2 * offsetAngle) / (mMenuItemCount + 1);
            if (mIsAlignStart) {
                mStartAngle = 270 - offsetAngle + angleDelay;
            } else {
                mStartAngle = 270 + offsetAngle - angleDelay;
            }
        }

        int left, top;

        int cWidth = mChildItemWidth;
        int cHeight = mChildItemWidth;

        // 遍历去设置menuitem的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            mStartAngle %= 360;
            mItemInfos.get(i).setAngle((int) mStartAngle);

            // 计算，中心点到menu item中心的距离
            float tmp = layoutRadius / 2f - cWidth / 2 - mPadding;

            // tmp cosa 即menu item中心点的横坐标
            left = layoutRadius / 2 + (int) Math.round(tmp * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            // tmp sina 即menu item的纵坐标
            top = layoutRadius / 2 + (int) Math.round(tmp * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * cHeight);
            child.layout(left, top, left + cWidth, top + cHeight);
            if (mIsAlignStart) {
                //mStartAngle = (Math.abs(mStartAngle) + angleDelay);
                mStartAngle = mStartAngle + angleDelay;
            } else {
                if (mStartAngle - angleDelay < 0) {
                    mStartAngle = mStartAngle + 360 - angleDelay;
                } else {
                    mStartAngle = mStartAngle - angleDelay;
                }
            }
        }
    }

    // 重置位置
    public void resetPosition() {
        int angleDelay = 0;
        if (mMenuItemCount == 8) {
            angleDelay = mMenuItemCount == 1 ? 360 : 360 / (mMenuItemCount);
        } else {
            return;
        }

        int layoutWidth = getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_width) / 2;

        int left, top;
        int cWidth = mChildItemWidth;
        int cHeight = mChildItemWidth;

        // 遍历去设置menuitem的位置
        for (int i = 0; i < mMenuItemCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            mStartAngle %= 360;
            mItemInfos.get(i).setAngle((int) mStartAngle);

            // 计算，中心点到menu item中心的距离
            float tmp = layoutWidth / 2f - cWidth / 2 - mPadding;

            // tmp cosa 即menu item中心点的横坐标
            left = layoutWidth / 2 + (int) Math.round(tmp * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            // tmp sina 即menu item的纵坐标
            top = layoutWidth / 2 + (int) Math.round(tmp * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * cHeight);
            child.layout(left, top, left + cWidth, top + cHeight);
            if (mIsAlignStart) {
                //mStartAngle = (Math.abs(mStartAngle) + angleDelay);
                mStartAngle = mStartAngle + angleDelay;
            } else {
                if (mStartAngle - angleDelay < 0) {
                    mStartAngle = mStartAngle + 360 - angleDelay;
                } else {
                    mStartAngle = mStartAngle - angleDelay;
                }
            }
        }
    }

    /**
     * 获取与当前角度 差距最小的标准角度，方便转动停止之后矫正。
     *
     * @param currentAngle
     * @return
     */
    private int getNearStandardAngle(double currentAngle) {
        int nearAngle = 0;
        double minDifferential = 9999;
        currentAngle = currentAngle % 360;
        for (int i = 0; i < mStartAngleStandard.length; i++) {
            double differential = Math.abs(currentAngle - mStartAngleStandard[i]);
            if (differential < minDifferential) {
                minDifferential = differential;
                nearAngle = i;
            }
        }
        return nearAngle;
    }

    /**
     * 显示 转盘 时的动画
     *
     * @param isAlignStart 悬浮球是否靠左边
     * @param listener     动画监听器
     */
    public void setShowPanelAnimation(boolean isAlignStart, final OnPanelAnimationListener listener) {
        //Log.d(TAG, " setShowPanelAnimation");

        mIsAlignStart = isAlignStart;
        mAnimaPlayLists = getAnimationPlayList(isAlignStart);// 获得播放动画的顺序

        // 如果animaItems为空，则不显示动画，直接显示转盘
        if (mAnimaPlayLists == null || mAnimaPlayLists.size() == 0) {
            //Log.d(TAG, "animaItems == null,so ignore animation.");
            for (RelativeLayout view : mItemView) {
                view.setAlpha(1);
                view.setScaleX(1);
                view.setScaleY(1);
            }
            return;
        }

        // 修改之后的行为是，使用默认的动画
        post(new ShowAnimationFadeIn(listener, mAnimaPlayLists));

        // 根据当前的动画模式，选择播放的动画效果
        /*mAnimationMode = PreferenceUtils.getAnimationMode(mContext, 1);
        switch (mAnimationMode) {
            case 1:
                post(new ShowAnimationFadeIn(listener, mAnimaPlayLists));
                break;
            case 2:
                post(new ShowAnimationFadeInOneByOne(listener, mAnimaPlayLists));
                break;
            case 3:
                post(new ShowAnimationFlower(listener, mAnimaPlayLists));
                break;
            default:
                break;
        }*/
    }

    /**
     * 设置转盘隐藏起来
     *
     * @param isAlignStart
     */
    public void setHidePanelAnimation(boolean isAlignStart, final OnPanelAnimationListener listener) {
        Log.d(TAG, "anxi setHidePanelAnimation isAlignStart:" + isAlignStart);

        mIsAlignStart = isAlignStart;

        // 消失动画的时候，与显示的时候要相反。
        mAnimaPlayLists = new ArrayList<Integer>();
        ArrayList<Integer> temp = getAnimationPlayList(isAlignStart);
        if (temp != null && temp.size() > 0) {
            for (int i = temp.size() - 1; i >= 0; i--) {
                mAnimaPlayLists.add(temp.get(i));
            }
        }
        post(new HideAnimationFadeIn(listener, mAnimaPlayLists));
        //根据当前的动画模式，选择播放动画的方式。
        /*switch (mAnimationMode) {
            case 1:
                post(new HideAnimationFadeIn(listener, mAnimaPlayLists));
                break;
            case 2:
                post(new HideAnimationFadeInOnebyOne(listener, mAnimaPlayLists));
                break;
            case 3:
                post(new HideAnimationFlower(listener, mAnimaPlayLists));
                break;
            default:
                break;
        }*/
    }

    /**
     * 根据角度，获取子菜单播放动画的顺序
     *
     * @param isAlignStart 悬浮球是否靠着开始那一边
     * @return
     */
    private ArrayList<Integer> getAnimationPlayList(boolean isAlignStart) {
        ArrayList<Integer> animaItems = new ArrayList<Integer>();                 // 存放子菜单动画播放排序的列表

        int[] startAngles = null;
        int[] endAngles = null;

        switch (mMenuItemCount) {
            case 3:
                animaItems.clear();
                animaItems.add(0);
                animaItems.add(1);
                animaItems.add(2);
                return animaItems;
            case 4:
                animaItems.clear();
                animaItems.add(0);
                animaItems.add(1);
                animaItems.add(2);
                animaItems.add(3);
                return animaItems;
            case 5:
                animaItems.clear();
                animaItems.add(0);
                animaItems.add(1);
                animaItems.add(2);
                animaItems.add(3);
                animaItems.add(4);
                return animaItems;
            case 8:
                // 由于最终只使用一种动画，同时浮出的动画和顺序无关，所以直接返回就好了
                //startAngles = new int[]{270, 315, 0, 45, 90, 135, 180, 225};  // 悬浮求在左边时，动画播放的顺序
                //endAngles = new int[]{270, 225, 180, 135, 90, 45, 0, 315};    // 悬浮求在右边时，动画播放的顺序

                // 直接返回
                animaItems.clear();
                animaItems.add(0);
                animaItems.add(1);
                animaItems.add(2);
                animaItems.add(3);
                animaItems.add(4);
                animaItems.add(5);
                animaItems.add(6);
                animaItems.add(7);
                return animaItems;
        }


        // 从子菜单中挑选出需要动画显示的菜单，并且排好顺序
        animaItems.clear();
        if (isAlignStart) {
            for (int angle : startAngles) {
                for (int i = 0; i < mItemInfos.size(); i++) {
                    if (mItemInfos.get(i).getAngle() == angle) {
                        animaItems.add(i);
                        continue;
                    }
                }
            }
        } else {
            for (int angle : endAngles) {
                for (int i = 0; i < mItemInfos.size(); i++) {
                    if (mItemInfos.get(i).getAngle() == angle) {
                        animaItems.add(i);
                        continue;
                    }
                }
            }
        }
        return animaItems;
    }

    /**
     * 子菜单 同时浮出来的动画 和 HideAnimationRunnable1 配对使用
     */
    class ShowAnimationFadeIn implements Runnable {
        int animationNumber;
        OnPanelAnimationListener listener;
        ArrayList<Integer> lists;

        ShowAnimationFadeIn(OnPanelAnimationListener listener, ArrayList<Integer> lists) {
            this.animationNumber = lists.size();
            this.listener = listener;
            this.lists = lists;
        }

        @Override
        public void run() {
            for (int i = 0; i < animationNumber; i++) {
                final int j = i;
                final RelativeLayout view;
                try {
                    view = mItemView.get(lists.get(i));
                } catch(Exception e) {
                    Log.e(TAG,e.toString());
                    continue;
                }

                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 1f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.6f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.6f, 1f);

                animatorSet.setDuration(150);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.playTogether(alpha, scaleX, scaleY);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        //Log.d(TAG, " show onAnimationStart");
                        view.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setAlpha(1f);
                        //Log.d(TAG, " show onAnimationEnd");
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        //Log.d(TAG, " show onAnimationCancel");
                        view.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                animatorSet.start();
            }
        }
    }

    /**
     * 子菜单 同时下沉的动画 和 ShowAnimationRunnable1 配对使用
     */
    class HideAnimationFadeIn implements Runnable {
        int animationNumber;
        int finishNumber;
        OnPanelAnimationListener listener;
        ArrayList<Integer> lists;

        HideAnimationFadeIn(OnPanelAnimationListener listener, ArrayList<Integer> lists) {
            this.animationNumber = lists.size();
            this.finishNumber = 0;
            this.listener = listener;
            this.lists = lists;
        }

        @Override
        public void run() {
            for (int i = 0; i < animationNumber; i++) {
                final int j = i;

                RelativeLayout view = null;

                try {
                    view = mItemView.get(lists.get(i));
                } catch (IndexOutOfBoundsException e) {
                    // 处理异常情况
                    e.printStackTrace();
                    finishNumber++;

                    //view.setAlpha(0.6f);
                    //view.setScaleX(0.6f);
                    //view.setScaleY(0.6f);
                    if (finishNumber == animationNumber) {
                        listener.onHidePanelAnimationEnd();
                        break;
                    } else {
                        continue;
                    }
                } catch (Exception e) {//add by yangzhong.gong for defect-5064405
                    e.printStackTrace();
                    break;
                }
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.6f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.6f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.6f);

                animatorSet.setDuration(200);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.playTogether(alpha, scaleX, scaleY);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        //Log.d(TAG, " hide onAnimationStart");
                        if (j == 0) {
                            listener.onHidePanelAnimationStart();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        //Log.d(TAG, " hide onAnimationEnd");
                        finishNumber++;
                        if (finishNumber == animationNumber) {
                            listener.onHidePanelAnimationEnd();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        //Log.d(TAG, " hide onAnimationCancel");
                        listener.onHidePanelAnimationCancel();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                animatorSet.start();
            }
        }
    }

    /**
     * 子菜单 一个一个浮出来的动画 和 HideAnimationRunnable2 配对使用
     */
    class ShowAnimationFadeInOneByOne implements Runnable {
        int animationNumber;
        int currentNumber = 0;
        OnPanelAnimationListener listener;
        ArrayList<Integer> lists;

        ShowAnimationFadeInOneByOne(OnPanelAnimationListener listener, ArrayList<Integer> lists) {
            this.animationNumber = lists.size();
            this.currentNumber = 0;
            this.listener = listener;
            this.lists = lists;
        }

        @Override
        public void run() {
            if (currentNumber == animationNumber) {
                currentNumber = 0;
                return;
            }
            final RelativeLayout view = mItemView.get(lists.get(currentNumber));

            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 0.6f, 0.8f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 0.6f, 0.8f, 1.1f, 1f);
            //final ObjectAnimator transX = ObjectAnimator.ofFloat(fPanel, "translationX", offsetX, 0);
            //ObjectAnimator transY = ObjectAnimator.ofFloat(fPanel, "translationY", offsetY, 0);

            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    view.setAlpha(0f);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setAlpha(1f);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    view.setAlpha(1f);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();
            currentNumber++;
            postDelayed(this, 50);
        }
    }


    /**
     * 子菜单 一个一个沉下去的动画 和 ShowAnimationRunnable2 配对使用
     */
    class HideAnimationFadeInOnebyOne implements Runnable {
        int animationNumber;
        int currentNumber = 0;
        int finishNumber = 0;
        OnPanelAnimationListener listener;
        ArrayList<Integer> lists;

        HideAnimationFadeInOnebyOne(OnPanelAnimationListener listener, ArrayList<Integer> lists) {
            this.animationNumber = lists.size();
            this.currentNumber = 0;
            this.finishNumber = 0;
            this.listener = listener;
            this.lists = lists;
        }

        @Override
        public void run() {
            if (currentNumber == animationNumber) {
                currentNumber = 0;
                return;
            }
            final RelativeLayout view = mItemView.get(lists.get(currentNumber));

            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.3f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 0.8f, 0.6f, 0.4f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 0.8f, 0.6f, 0.4f);

            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    view.setAlpha(0f);
                    //显示第一个子菜单的时候，回调控制器的 onHidePanelAnimationStart
                    if (currentNumber == 0) {
                        listener.onHidePanelAnimationStart();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setAlpha(0f);
                    finishNumber++;
                    if (finishNumber == animationNumber) {
                        listener.onHidePanelAnimationEnd();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    view.setAlpha(0f);
                    listener.onHidePanelAnimationCancel();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            currentNumber++;
            animatorSet.start();
            postDelayed(this, 50);
        }
    }

    /**
     * 一个一个弹出来的动画 和 HidleAnimationRunnable3 配对使用
     */
    class ShowAnimationFlower implements Runnable {
        int animationNumber;
        int currentNumber = 0;
        OnPanelAnimationListener listener;
        ArrayList<Integer> lists;
        float x;
        float y;
        boolean initSuccess = true;

        ShowAnimationFlower(OnPanelAnimationListener listener, ArrayList<Integer> lists) {
            this.animationNumber = lists.size();
            this.currentNumber = 0;
            this.listener = listener;
            this.lists = lists;
            initSuccess = true;

            if (lists == null || lists.size() == 0) {
                listener.onHidePanelAnimationEnd();
                initSuccess = false;
                return;
            }

            //获得布局中心的坐标，方便后面做偏移的计算
            this.x = getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_width) / 2;
            this.y = this.x;
        }

        @Override
        public void run() {
            //Log.d(TAG, "ShowAnimationRunnable3 run");
            if (currentNumber == animationNumber || !initSuccess) {
                currentNumber = 0;
                return;
            }

            int index = lists.get(currentNumber);
            final RelativeLayout view = mItemView.get(index);

            //根据角度，算出偏移XY
            float circle1X = (view.getLeft() + view.getRight()) / 2f;
            float circle1y = (view.getTop() + view.getBottom()) / 2f;

            float offsetX = circle1X - this.x;
            float offsetY = circle1y - this.y;

            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 0.1f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.6f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.6f, 1f);
            final ObjectAnimator transX = ObjectAnimator.ofFloat(view, "translationX", -offsetX, 0);
            ObjectAnimator transY = ObjectAnimator.ofFloat(view, "translationY", -offsetY, 0);

            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY, transX, transY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    view.setAlpha(0f);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setAlpha(1f);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    view.setAlpha(1f);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();
            currentNumber++;
            postDelayed(this, 50);
        }
    }

    /**
     * 子菜单 一个一个弹回去的动画 和 ShowAnimationRunnable3 配对使用
     */
    class HideAnimationFlower implements Runnable {
        int animationNumber;
        int currentNumber = 0;
        int finishNumber = 0;
        OnPanelAnimationListener listener;
        ArrayList<Integer> lists;
        float x;                        // 中心坐标x
        float y;                        // 中心坐标y
        boolean initSuccess = true;

        HideAnimationFlower(OnPanelAnimationListener listener, ArrayList<Integer> lists) {
            this.animationNumber = lists.size();
            this.currentNumber = 0;
            this.finishNumber = 0;
            this.listener = listener;
            this.lists = lists;
            initSuccess = true;

            if (lists == null || lists.size() == 0) {
                listener.onHidePanelAnimationEnd();
                initSuccess = false;
                return;
            }

            //获得布局中心的坐标，方便后面做偏移的计算
            this.x = getResources().getDimensionPixelOffset(R.dimen.mini_float_panel_width) / 2;
            this.y = this.x;
        }

        @Override
        public void run() {
            if (currentNumber == animationNumber || !initSuccess) {
                currentNumber = 0;
                return;
            }

            final RelativeLayout view = mItemView.get(lists.get(currentNumber));

            float circle1X = (view.getLeft() + view.getRight()) / 2f;
            float circle1y = (view.getTop() + view.getBottom()) / 2f;

            float offsetX = circle1X - this.x;
            float offsetY = circle1y - this.y;

            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1, 0.8f, 0);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.6f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.6f);
            final ObjectAnimator transX = ObjectAnimator.ofFloat(view, "translationX", 0, -offsetX);
            ObjectAnimator transY = ObjectAnimator.ofFloat(view, "translationY", 0, -offsetY);

            animatorSet.setDuration(200);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(alpha, scaleX, scaleY, transX, transY);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (currentNumber == 0) {
                        listener.onHidePanelAnimationStart();
                    }
                    view.setAlpha(0f);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setAlpha(0f);
                    finishNumber++;
                    if (finishNumber == animationNumber) {
                        listener.onHidePanelAnimationEnd();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resetPosition();
                            }
                        }, 100);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    view.setAlpha(0f);
                    listener.onHidePanelAnimationCancel();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();
            currentNumber++;
            postDelayed(this, 50);
        }
    }

    public interface OnPanelAnimationListener {
        void onHidePanelAnimationEnd();

        void onHidePanelAnimationStart();

        void onHidePanelAnimationCancel();
    }

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
                // 如果不是八个子菜单，触摸事件直接给子控件处理 return false
                if (mMenuItemCount != 8) {
                    return false;
                }
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

                if (Math.abs(mTmpAngle) > NOCLICK_VALUE && mMenuItemCount == 8) {
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
                break;
            case MotionEvent.ACTION_MOVE:
                float start = getAngle(mLastX, mLastY);//获得开始的角度
                float end = getAngle(x, y);//获得当前的角度
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
                // 刷新转盘
                if (Math.abs(mTmpAngle) != 0 && mMenuItemCount == 8) {
                    requestLayout();
                    mLastX = x;
                    mLastY = y;
                }
                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE && mMenuItemCount == 8) {
                    mTouchMoving = true;
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000 / (System.currentTimeMillis() - mDownTime);
                // post一个任务，去自动滚动
                if (!isFling && mMenuItemCount == 8) {
                    post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));
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

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mLayoutWidth / 2d);
        double y = yTouch - (mLayoutWidth / 2d);
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
        int tmpX = (int) (x - mLayoutWidth / 2);
        int tmpY = (int) (y - mLayoutWidth / 2);
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

    public void setNowKeyData(ArrayList<BaseItemInfo> itemInfos) {
        if (itemInfos == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        mItemInfos.clear();
        mItemView.clear();
        Collections.sort(itemInfos, new BaseModelComparator());
        itemInfos = Utils.convertBaseItemInfos(mContext, itemInfos);

        int count = PreferenceUtils.getMiniMenuItemCount(8);      // 用户设置了显示子菜单的数量

        if (count > itemInfos.size()) {
            count = itemInfos.size();
        }

        if (count == 8) mStartAngle = 270;  //重置之后，也要重置角度
        for (int i = 0; i < count; i++) {
            mItemInfos.add(itemInfos.get(i));
        }
        removeAllViews();
        addMenuItems();
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

    public void onFinishHide() {
        mStartingAction = false;
    }

    /**
     * 主题变化时调用
     *
     * @param color
     */
    public void onThemeChanged(int color) {
        PorterDuffColorFilter pf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        VectorDrawable bg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_icon_bg_73);
        bg.setTint(color);
        for (RelativeLayout layout : mItemView) {
            layout.setBackground(bg);
        }
    }

    /**
     * 添加菜单项
     */
    private void addMenuItems() {
        mMenuItemCount = mItemInfos.size();
        int themeColor = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME, -1);
        if (themeColor == -1) {
            themeColor = Color.parseColor(Constant.DEFAULT_THEME);
        }
        PorterDuffColorFilter pf = new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_ATOP);
        mItemBg.setColorFilter(pf);
        BaseItemInfo[] addedInfos = new BaseItemInfo[mMenuItemCount];
        /**
         * 根据用户设置的参数，初始化view
         */
        for (int i = 0; i < mMenuItemCount; i++) {
            final BaseItemInfo bInfo = mItemInfos.get(i);
            addedInfos[bInfo.getIndex()] = bInfo;
        }

        for (int i = 0; i < addedInfos.length; i++) {
            final int j = i;
            final BaseItemInfo added = addedInfos[j];
            final RelativeLayout layout = (RelativeLayout) View.inflate(mContext, R.layout.circle_menu_item_mini, null);

            layout.setBackground(mItemBg);
            layout.setAlpha(0);
            final FloatItemView appLayout = (FloatItemView) layout.findViewById(R.id.app_layout_mini);
            final ImageView icon = (ImageView) layout.findViewById(R.id.app_icon_img_mini);

            if (added != null) {
                if (added instanceof FunctionItemInfo) {
                    FunctionItemInfo fInfo = (FunctionItemInfo) added;
                    appLayout.setInfo(fInfo);
                    Drawable d = fInfo.getIcon();
                    d.setBounds(0, 0, mIconImgWidth, mIconImgWidth);
                    icon.setImageDrawable(d);
                    fInfo.setIconView(icon);
                    fInfo.onAdd(mContext);
                } else if (added instanceof AppItemInfo) {
                    AppItemInfo aInfo = (AppItemInfo) added;
                    appLayout.setInfo(aInfo);
                    Drawable d = aInfo.getIcon();
                    d.setBounds(0, 0, mIconImgWidth, mIconImgWidth);
                    icon.setImageDrawable(d);
                }
                appLayout.setVisibility(View.VISIBLE);
                appLayout.setIndex(added.getIndex());
                setVisibleItemListener(appLayout, added);
            } else {
                layout.setVisibility(GONE);
                appLayout.setInfo(null);
                appLayout.setIndex(j);
            }
            // 添加view到容器中
            mItemView.add(layout);
            addView(layout, j);
        }
    }

    private void setVisibleItemListener(final FloatItemView layout, final BaseItemInfo bInfo) {
        final int index = bInfo.getIndex();
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTouchMoving && view instanceof FloatItemView && !mStartingAction) {
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
                                        mContext, index, 1);
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
    }

    public void deleteAllItem() {
        NowKeyPanelModel.getInstance().deleteAll(1);
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
            //Log.d(TAG,"anxi angelPerSecond = "+angelPerSecond);
            // 如果小于60,则开始停止转动, 并且要求要停止在 标准的角度 上面
            if ((int) Math.abs(angelPerSecond) < 60) {
                double standarAngle = mStartAngleStandard[getNearStandardAngle(mStartAngle)];
                if (Math.abs(standarAngle - mStartAngle % 360) < 10) {
                    mStartAngle = standarAngle;
                    isFling = false;
                    requestLayout();
                    return;
                } else {
                    isFling = true;
                    // 调整之后的速度的方向和 悬浮球靠左边和右边有关系， 也和用户转动的方向有关系，需要综合判断
                    /*if ((angelPerSecond >= 0 && mIsAlignStart) || (angelPerSecond < 0 && !mIsAlignStart)) {
                        angelPerSecond = 70;
                    } else if((angelPerSecond < 0 && mIsAlignStart) || (angelPerSecond >= 0 && !mIsAlignStart)){
                        angelPerSecond = -70;
                    }*/
                    if ((angelPerSecond >= 0)) {
                        angelPerSecond = 70;
                    } else if ((angelPerSecond < 0)) {
                        angelPerSecond = -70;
                    }
                }
            }
            isFling = true;
            // 不断改变mStartAngle，让其滚动，/30为了避免滚动太快
            mStartAngle += (angelPerSecond / 30);
            //Log.d(TAG, "mStartAngle = " + mStartAngle);
            // 逐渐减小这个值
            angelPerSecond /= 1.0666F;
            postDelayed(this, 30);
            // 重新布局
            requestLayout();
        }
    }

    public boolean isFling() {
        return isFling;
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
            View icon = child.findViewById(R.id.app_layout);
            if (icon instanceof FloatItemView) {
                FloatItemView floatItemView = (FloatItemView) icon;
                BaseItemInfo info = floatItemView.getInfo();
                if (info != null) {
                    if (info instanceof FunctionItemInfo && mContext != null) {
                        ((FunctionItemInfo) info).onDelete(mContext);
                    }
                    floatItemView.setInfo(null);
                }
            }
        }

        setOnClickListener(null);
        removeAllViews();
        mItemInfos = null;
        mOnMenuItemClickListener = null;
        mContext = null;
        mItemBg = null;

        mStartAngleStandard = null;
        if (mItemInfos != null) {
            mItemInfos.clear();
            mItemInfos = null;
        }


        if (mItemView != null) {
            for (RelativeLayout layout : mItemView) {
                View icon = layout.findViewById(R.id.app_layout_mini);
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
                icon = null;
            }
            mItemView.clear();
            mItemView = null;
        }

        if (mAnimaPlayLists != null) {
            mAnimaPlayLists.clear();
            mAnimaPlayLists = null;
        }
    }
}
