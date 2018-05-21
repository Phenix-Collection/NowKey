package com.kuding.nowkey.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kuding.nowkey.FunctionActivity;
import com.kuding.nowkey.MiniModeEditActivity;
import com.kuding.nowkey.R;
import com.kuding.nowkey.Utils.BaseModelComparator;
import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.JsonParser;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.Utils.Utils;
import com.kuding.nowkey.floatview.FloatItemView;
import com.kuding.nowkey.floatview.NowKeyPanelModel;
import com.kuding.nowkey.info.AppItemInfo;
import com.kuding.nowkey.info.BaseItemInfo;
import com.kuding.nowkey.info.FunctionItemInfo;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 编辑 子菜单 的视图
 */

public class EditMenuItemView_Mini extends RelativeLayout {
    private static final String TAG = "FloatingBallPanel";

    private Context mContext;

    private float mPadding;             // 该容器的内边距,无视padding属性，如需边距请用该变量
    private double mStartAngle = 0;     // 布局时的开始角度
    private int mMenuItemCount = 8;     // 菜单的个数
    private boolean mStartingAction;    // 是否在执行点击动作
    private int mChildItemWidth;        // 子菜单的大小
    private int mLayoutWidth;           // 整个布局的宽度

    private ArrayList<BaseItemInfo> mItemInfos = new ArrayList<>();     // 存放子菜单数据的列表
    private ArrayList<BaseItemInfo> mBackupData = new ArrayList<>();    // 存放迷你模式的备份数据，有备份数据，当用户增加子菜单的时候，可以快速从子菜单补充
    private ArrayList<RelativeLayout> mItemVies = new ArrayList<>();    // 存在子菜单布局的列表

    public EditMenuItemView_Mini(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mChildItemWidth = Utils.dip2px(mContext, 36);
        mPadding = 0;
        mLayoutWidth = Utils.dip2px(mContext, 172);

        setPadding(0, 0, 0, 0);

        loadData(getMenuCount());
    }

    private void initData() {
        String backup = PreferenceUtils.getMiniDataBackup("");
        if ("".equals(backup)) {
            mBackupData = NowKeyPanelModel.getInstance().loadMiniXMLData();
        } else {
            mBackupData = JsonParser.loadItemsFromString(backup);
        }
    }

    /**
     * 设置布局的宽高，并策略menu item宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure");

        if (mMenuItemCount == 0) return;

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
        // menu item数量
        final int count = getChildCount();
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;
        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeausreSpecWidth = -1;
            int makeMeausreSpecHeight = -1;

            makeMeausreSpecWidth = MeasureSpec.makeMeasureSpec(mChildItemWidth, childMode);
            makeMeausreSpecHeight = MeasureSpec.makeMeasureSpec(mChildItemWidth, childMode);
            child.measure(makeMeausreSpecWidth, makeMeausreSpecHeight);
        }
    }

    public void onDestory() {
        mContext = null;

        if (mItemInfos != null) {
            mItemInfos.clear();
            mItemInfos = null;
        }

        if (mItemVies != null) {
            for (int i = 0; i < mItemVies.size(); i++) {
                View child = mItemVies.get(i);
                View icon = child.findViewById(R.id.app_layout_normal);
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
            mItemVies.clear();
            mItemVies = null;
        }

        if (mBackupData != null) {
            mBackupData.clear();
            mBackupData = null;
        }

        removeAllViews();
    }

    /**
     * 设置menu item的位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout");

        if (mMenuItemCount == 0) return;

        int radius = mLayoutWidth / 2;          // 布局的半径
        int childRadius = mChildItemWidth / 2;  // 子菜单的半径

        // 1. 先设置 中间的圆的位置
        final View center = getChildAt(0);
        center.layout(radius - childRadius, radius - childRadius, radius + childRadius, radius + childRadius);


        // 2. 然后 设置子菜单的位置
        int angleDelay = 0;
        int count = mMenuItemCount;

        // 用反正切求出偏移的角度
        int offsetAngle = (int) (Math.round(Math.toDegrees(Math.atan2(childRadius + mPadding, radius))));
        Log.d(TAG, " offsetAngle = " + offsetAngle);
        // 根据菜单的个数，计算间隔角度
        if (count == 8) {
            mStartAngle = 270;
            angleDelay = count == 1 ? 360 : 360 / (count);
        } else if (count == 5) {
            mStartAngle = 270;
            angleDelay = 180 / (count - 1);
        } else if (count == 4 || count == 3) {
            angleDelay = (180 + 2 * offsetAngle) / (count + 1);
            mStartAngle = 270 + offsetAngle - angleDelay;
        }

        int left, top;
        int cWidth = mChildItemWidth;
        int cHeight = mChildItemWidth;

        // 遍历去设置menuitem的位置
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i + 1);
            if (child.getVisibility() == GONE) {
                continue;
            }
            mStartAngle %= 360;
            mItemInfos.get(i).setAngle((int) mStartAngle);

            // 计算，中心点到menu item中心的距离
            float tmp = radius - cWidth / 2 - mPadding;

            // tmp cosa 即menu item中心点的横坐标
            left = radius + (int) Math.round(tmp * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            // tmp sina 即menu item的纵坐标
            top = radius + (int) Math.round(tmp * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * cHeight);
            child.layout(left, top, left + cWidth, top + cHeight);
            mStartAngle -= angleDelay;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
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
     */
    private void loadData(int targetNumber) {
        removeAllViews();
        initData();
        ArrayList<BaseItemInfo> itemInfos = NowKeyPanelModel.getInstance().loadMiniData();
        if (itemInfos == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        int dataSize = itemInfos.size();

        //如果当前保存的子菜单列表数量少于,用户拖动条选择的数量，则自动从备份数据里面取出子菜单，进行填充
        if (dataSize < targetNumber) {
            Log.d(TAG, "anxi dataSize < number");
            if (mBackupData.size() < targetNumber) {
                mBackupData = NowKeyPanelModel.getInstance().loadMiniXMLData();
            }
            for (int i = dataSize; i < targetNumber; i++) {
                BaseItemInfo info = mBackupData.get(i);
                info.setIndex(i);
                itemInfos.add(info);
            }
        }

        mItemInfos.clear();
        Collections.sort(itemInfos, new BaseModelComparator());
        // 根据用户选择的数量，设置 mItemInfos
        for (int i = 0; i < targetNumber; i++) {
            mItemInfos.add(itemInfos.get(i));
        }
        // 把数据保存
        PreferenceUtils.setMiniData(JsonParser.getJsonStringFromObject(mItemInfos));
        PreferenceUtils.setMiniMenuItemCount(mItemInfos.size());
        getMenuCount();

        // 转化为更详细的mItemInfos
        mItemInfos = Utils.convertBaseItemInfos(mContext, mItemInfos);
        initMenuItems();
    }

    /**
     * 初始化 子菜单
     */
    private void initMenuItems() {
        mItemVies.clear();
        BaseItemInfo[] addedInfos = new BaseItemInfo[mMenuItemCount];
        /**
         * 根据用户设置的参数，初始化view
         */
        for (int i = 0; i < mItemInfos.size(); i++) {
            final BaseItemInfo bInfo = mItemInfos.get(i);
            addedInfos[bInfo.getIndex()] = bInfo;
        }
        // 背景
        //VectorDrawable bg = (VectorDrawable) mContext.getDrawable(R.drawable.mini_ball_icon_bg_73);

        // 中间先添加一个圆
        final RelativeLayout centerLayout = (RelativeLayout) View.inflate(mContext, R.layout.edit_menu_item_mini, null);
        ImageView centerIcon = (ImageView) centerLayout.findViewById(R.id.app_icon_img_mini);
        centerIcon.setImageResource(R.drawable.ic_clear_black_24px);
        addView(centerLayout, 0);

        // 然后 绘制 子菜单
        for (int i = 0; i < addedInfos.length; i++) {
            final int j = i;
            final BaseItemInfo added = addedInfos[j];
            final RelativeLayout layout = (RelativeLayout) View.inflate(mContext, R.layout.edit_menu_item_mini, null);
            //layout.setBackground(bg);
            layout.setAlpha(1);
            final FloatItemView appLayout = (FloatItemView) layout.findViewById(R.id.app_layout_mini);
            final ImageView icon = (ImageView) layout.findViewById(R.id.app_icon_img_mini);

            if (added != null) {
                if (added instanceof FunctionItemInfo) {
                    FunctionItemInfo fInfo = (FunctionItemInfo) added;
                    appLayout.setInfo(fInfo);
                    Drawable d = fInfo.getIcon();
                    icon.setImageDrawable(d);
                    fInfo.setIconView(icon);
                    fInfo.onAdd(mContext);
                } else if (added instanceof AppItemInfo) {
                    AppItemInfo aInfo = (AppItemInfo) added;
                    appLayout.setInfo(aInfo);
                    Drawable d = aInfo.getIcon();
                    icon.setImageDrawable(d);
                }
                appLayout.setVisibility(View.VISIBLE);
                appLayout.setIndex(added.getIndex());
                setMenuItemClickListener(appLayout, added);
            } else {
                layout.setVisibility(GONE);
                appLayout.setInfo(null);
                appLayout.setIndex(j);
            }
            // 添加view到容器中
            mItemVies.add(layout);
            //因为前面中间添加了一个圆，所以这边的索引从1 开始。
            addView(layout, j + 1);
        }
    }

    /**
     * 设置 可见的 子菜单 的监听器
     *
     * @param layout
     * @param bInfo
     */
    private void setMenuItemClickListener(final FloatItemView layout, final BaseItemInfo bInfo) {
        Log.d(TAG, "setMenuItemClickListener");
        final int index = bInfo.getIndex();
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mStartingAction) {
                    mStartingAction = true;
                    Intent i = new Intent();
                    i.setClass(mContext, FunctionActivity.class);
                    i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.NOW_KEY_ACTION);
                    i.putExtra(Constant.NOW_KEY_MENU_OPERATE, Constant.NOW_KEY_MENU_OPERATE_UPDATE);
                    i.putExtra(Constant.NOW_KEY_ITEM_INDEX, index);
                    i.putExtra(Constant.NOW_KEY_ITEM_PAGE, 0);
                    i.putExtra(Constant.NOW_KEY_ADD_EXTERNAL, false);
                    i.putExtra(Constant.NOW_KEY_KEY_WORD, bInfo.getKey_word());
                    ((MiniModeEditActivity) mContext).startActivityForResult(i, 100);
                }
            }
        });
    }

    /**
     * 外部调用此方法来设置 转盘 子菜单的数量
     *
     * @param count
     */
    public void setMenuItemCount(int count) {
        PreferenceUtils.setMiniMenuItemCount(count);
        mMenuItemCount = count;
        loadData(mMenuItemCount);
        mStartingAction = false;
    }

    public int getMenuCount() {
        mMenuItemCount = PreferenceUtils.getMiniMenuItemCount(8);
        return mMenuItemCount;
    }


    /**
     * 用户选择了重置
     */
    public void resetData() {
        mMenuItemCount = 8;

        ArrayList<BaseItemInfo> itemInfos = NowKeyPanelModel.getInstance().loadMiniXMLData();
        if (itemInfos == null) {
            throw new IllegalArgumentException("data must not be null");
        }

        mItemInfos.clear();
        Collections.sort(itemInfos, new BaseModelComparator());

        for (int i = 0; i < mMenuItemCount; i++) {
            mItemInfos.add(itemInfos.get(i));
        }
        // 把数据保存
        PreferenceUtils.setMiniData(JsonParser.getJsonStringFromObject(mItemInfos));
        PreferenceUtils.setMiniMenuItemCount(mItemInfos.size());

        // 转化为更详细的mItemInfos
        mItemInfos = Utils.convertBaseItemInfos(mContext, mItemInfos);

        // 移除所有数据
        removeAllViews();
        initMenuItems();
    }
}
