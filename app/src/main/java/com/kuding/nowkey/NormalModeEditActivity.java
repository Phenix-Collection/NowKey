package com.kuding.nowkey;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.FunctionUtils;
import com.kuding.nowkey.Utils.JsonParser;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.info.UserPreference;
import com.kuding.nowkey.views.EditMenuItemView_Normal;
import com.kuding.nowkey.R;
import com.kuding.nowkey.floatview.FloatingBallController;
import com.kuding.nowkey.floatview.GestureController;
import com.kuding.nowkey.floatview.NowKeyPanelModel;
import com.kuding.nowkey.interfaces.OnTextChangeListener;
import com.kuding.nowkey.service.NowKeyService;

/**
 * 编辑 普通模式 的 Activity
 */

public class NormalModeEditActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnTextChangeListener {

    private final static String TAG = "NormalModeEditActivity";
    private int ADD_ITEM_REQ_CODE = 100;
    private int ADD_GESTURE_CODE = 200;
    private int OVERLAY_PERMISSION_CODE = 300;
    private static final long INTERVAL_TIME_RESET = 3000;   // 重置按钮点击有效的时间间隔

    private long mLastResetTime = 0;                        // 上一次重置Nowkey的时间

    private ActionBar mActionBar;

    private boolean mIsMiniMode = false;                    // 悬浮球是否迷你模式
    private int mMenuCount1 = 8;                            // 第一页的子菜单数量
    private int mMenuCount2 = 8;                            // 第二页的子菜单数量
    private EditMenuItemView_Normal mMenuItemEditView;      // 设置迷你模式 转盘 子菜单 的控件
    private SeekBar mSeekBar;
    private TextView mTvMenuCount;                          // 子菜单的数量textview
    private TextView mTvDoubleTap;
    private TextView mTvLongPress;
    private TextView mTvLittleDrag;
    private TextView mTvPage;
    private TextView mTvUndo;                              // 点击重置之后，恢复重置之前的按钮
    private LinearLayout mLayoutUndo;                      // 撤销重置的布局
    private UserPreference mUserPreference;                // 用户配置
    private HideUndoLayoutRunable mHideUndoLayoutRunable;  // 隐藏Undo 布局 的Runable
    private Handler mHandler = new Handler();

    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_mode_edit_activity);
        mIsMiniMode = PreferenceUtils.isMiniMode(false);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.now_key_option_gesture_and_function_title);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }

        initData();
        initView();
    }

    private void initData() {
        mMenuCount1 = PreferenceUtils.getNormalMenuItemCount1(8);
        mMenuCount2 = PreferenceUtils.getNormalMenuItemCount2(8);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGesture();
    }

    private void initView() {
        mTvPage = (TextView) findViewById(R.id.normal_edit_page);
        mTvPage.setText("1");

        mMenuItemEditView = (EditMenuItemView_Normal) findViewById(R.id.view_menu_item_edit);
        mMenuItemEditView.setmTextChangeListener(this);

        mTvMenuCount = (TextView) findViewById(R.id.tv_normal_view_number);
        mTvMenuCount.setText(String.valueOf(mMenuCount1));

        mSeekBar = (SeekBar) findViewById(R.id.seekbar_menu_edit);
        mSeekBar.setMax(7);
        mSeekBar.setProgress(mMenuCount1 - 1);//这个Seekbar 从0-7的，所以需要-1
        mSeekBar.setOnSeekBarChangeListener(this);

        mTvDoubleTap = (TextView) findViewById(R.id.tv_double_tap);
        mTvLongPress = (TextView) findViewById(R.id.tv_long_press);
        mTvLittleDrag = (TextView) findViewById(R.id.tv_little_drag);

        mTvDoubleTap.setOnClickListener(this);
        mTvLongPress.setOnClickListener(this);
        mTvLittleDrag.setOnClickListener(this);

        //撤销重置的布局
        mLayoutUndo = (LinearLayout) findViewById(R.id.layout_undo_main);
        mTvUndo = (TextView) mLayoutUndo.findViewById(R.id.tv_undo);
        mTvUndo.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_reset) {
            reset();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 重置数据
     */
    private void reset() {
        // 避免用户重复点击重置 ，需要设置点击的时间间隔
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastResetTime < INTERVAL_TIME_RESET) {
            return;
        }
        mLastResetTime = currentTime;
        // 备份用户配置
        backUpPreference();

        // 重置数据
        NowKeyPanelModel.getInstance().initExtraData();
        NowKeyPanelModel.getInstance().resetNormalData();
        FloatingBallController.getController(getApplication()).resetPanel();

        mMenuCount1 = 8;
        mMenuCount2 = 8;

        mMenuItemEditView.resetData();

        mSeekBar.setOnSeekBarChangeListener(null);
        mSeekBar.setProgress(8);
        mTvMenuCount.setText(R.string.eight);
        mSeekBar.setOnSeekBarChangeListener(this);

        // 重置快捷联系人
        PreferenceUtils.setCallContactItem("");

        // 重置手势
        GestureController.getInstance(this).resetNormalModeDefaultGesture();
        loadGesture();

        restartFloatBall();
        showUndoLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "anxi onConfigurationChanged");
        if (mLayoutUndo.getVisibility() == View.VISIBLE) {
            mLayoutUndo.setVisibility(View.GONE);
        }
    }


    /**
     * 显示 undo 的布局
     */
    private void showUndoLayout() {
        if (mLayoutUndo == null) return;
        if (mHideUndoLayoutRunable != null) {
            mHandler.removeCallbacks(mHideUndoLayoutRunable);
        }
        mHideUndoLayoutRunable = new HideUndoLayoutRunable();
        mLayoutUndo.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mHideUndoLayoutRunable, 4000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "anxi onActivityResult ");
        if (requestCode == ADD_ITEM_REQ_CODE) {
            mMenuItemEditView.resetView();
        } else if (requestCode == ADD_GESTURE_CODE) {
            loadGesture();
        } else if (requestCode == OVERLAY_PERMISSION_CODE) {
            if (Settings.canDrawOverlays(this)) {
                restartFloatBall();
            }
        }
    }

    /**
     * 重新 加载 手势 信息
     */
    private void loadGesture() {
        // 功能的关键字
        String doubleClick_keyword = "";
        String longPress_keyword = "";
        String shortDrag_keyword = "";

        // 功能的类型
        int doubleClick_type = -1;
        int longPress_type = -1;
        int shortDrag_type = -1;

        // 功能显示的名字 title
        String doubleClick = "";
        String longPress = "";
        String shortDrag = "";

        // 读取key_work,key_work 是唯一标识一个手势功能的关键字
        // 读取type， type是标识这个手势操作类型的
        if (mIsMiniMode) {
            doubleClick_keyword = PreferenceUtils.getMiniGestureDoubleClickAction("");
            shortDrag_keyword = PreferenceUtils.getMiniGestureShortDragAction("");
            longPress_keyword = PreferenceUtils.getMiniGestureLongPressAction("");

            doubleClick_type = PreferenceUtils.getMiniGestureDoubleClickType(-1);
            longPress_type = PreferenceUtils.getMiniGestureLongPressType(-1);
            shortDrag_type = PreferenceUtils.getMiniGestureShortDragType(-1);
        } else {
            doubleClick_keyword = PreferenceUtils.getNormalGestureDoubleClickAction("");
            shortDrag_keyword = PreferenceUtils.getNormalGestureShortDragAction("");
            longPress_keyword = PreferenceUtils.getNormalGestureLongPressAction("");

            doubleClick_type = PreferenceUtils.getNormalGestureDoubleClickType(-1);
            longPress_type = PreferenceUtils.getNormalGestureLongPressType(-1);
            shortDrag_type = PreferenceUtils.getNormalGestureShortDragType(-1);
        }

        // 双击手势
        if (doubleClick_keyword.equals("")) {
            doubleClick = getString(R.string.action_none);
        } else {
            // 如果手势时app类型的，则通过包名获取应用名
            if (doubleClick_type == Constant.NOW_KEY_ITEM_TYPE_APP) {
                doubleClick = getApplicationName(doubleClick_keyword);
            } else if ("callacontact".equals(doubleClick_keyword)) {
                // 如果手势类型时 callacontact
                if (mIsMiniMode) {
                    doubleClick = PreferenceUtils.getMiniGestureDoubleClick("");
                } else {
                    doubleClick = PreferenceUtils.getNormalGestureDoubleClick("");
                }
            } else {
                // 如果是功能，则根据key_work,获得功能的名字
                FunctionItemInfo info = FunctionUtils.functionFilter(this, doubleClick_keyword);
                doubleClick = info.getText();
            }
        }

        // 长按手势
        if (longPress_keyword.equals("")) {
            longPress = getString(R.string.action_none);
        } else if ("callacontact".equals(longPress_keyword)) {
            // 如果手势类型时 callacontact
            if (mIsMiniMode) {
                longPress = PreferenceUtils.getMiniGestureLongPress("");
            } else {
                longPress = PreferenceUtils.getNormalGestureLongPress("");
            }
        } else {
            // 如果手势时app类型的，则通过包名获取应用名
            if (longPress_type == Constant.NOW_KEY_ITEM_TYPE_APP) {
                longPress = getApplicationName(longPress_keyword);
            } else {
                // 如果是功能，则根据key_work,获得功能的名字
                FunctionItemInfo info = FunctionUtils.functionFilter(this, longPress_keyword);
                longPress = info.getText();
            }
        }

        // 短拉手势
        if (shortDrag_keyword.equals("")) {
            shortDrag = getString(R.string.action_none);
        } else if ("callacontact".equals(shortDrag_keyword)) {
            // 如果手势类型时 callacontact
            if (mIsMiniMode) {
                shortDrag = PreferenceUtils.getMiniGestureShortDrag("");
            } else {
                shortDrag = PreferenceUtils.getNormalGestureShortDrag("");
            }
        } else {
            // 如果手势时app类型的，则通过包名获取应用名
            if (shortDrag_type == Constant.NOW_KEY_ITEM_TYPE_APP) {
                shortDrag = getApplicationName(shortDrag_keyword);
            } else {
                // 如果是功能，则根据key_work,获得功能的名字
                FunctionItemInfo info = FunctionUtils.functionFilter(this, shortDrag_keyword);
                shortDrag = info.getText();
            }
        }

        // 把手势功能的名字保存起来， callacontact 类型比较特它的内容是"联系人名字,电话号码"不需要保存起来
        if (mIsMiniMode) {
            PreferenceUtils.setMiniGestureDoubleClick(doubleClick);
            PreferenceUtils.setMiniGestureLongPress(longPress);
            PreferenceUtils.setMiniGestureShortDrag(shortDrag);
        } else {
            PreferenceUtils.setNormalGestureDoubleClick(doubleClick);
            PreferenceUtils.setNormalGestureLongPress(longPress);
            PreferenceUtils.setNormalGestureShortDrag(shortDrag);
        }

        mTvDoubleTap.setText(doubleClick);
        mTvLongPress.setText(longPress);
        mTvLittleDrag.setText(shortDrag);
    }

    /**
     * 获取应用的名称
     */
    private String getApplicationName(String packageName) {
        String applicationName = null;
        try {
            ApplicationInfo applicationInfo = getPackageManager2().getApplicationInfo(packageName, 0);
            applicationName = (String) getPackageManager2().getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {

        }
        return applicationName;
    }

    /**
     * 获得 PackageManager
     *
     * @return
     */
    private PackageManager getPackageManager2() {
        if (mPackageManager == null) {
            mPackageManager = getApplicationContext().getPackageManager();
        }
        return mPackageManager;
    }

    @Override
    public void onClick(View v) {
        boolean isGestrueOperate = false;
        Intent intent = new Intent();
        intent.setClass(NormalModeEditActivity.this, FunctionActivity.class);
        switch (v.getId()) {
            case R.id.tv_double_tap:
                intent.putExtra(Constant.OPERATE_BEHAVIOR, Constant.GESTURE_DOUBLE_CLICK);
                isGestrueOperate = true;
                break;
            case R.id.tv_long_press:
                intent.putExtra(Constant.OPERATE_BEHAVIOR, Constant.GESTURE_LONG_CLICK);
                isGestrueOperate = true;
                break;
            case R.id.tv_little_drag:
                intent.putExtra(Constant.OPERATE_BEHAVIOR, Constant.GESTURE_SHORT_DRAG);
                isGestrueOperate = true;
                break;
            case R.id.tv_undo:
                restoredPreference();
                break;
        }
        if (isGestrueOperate) startActivityForResult(intent, 200);
    }

    /**
     * 恢复 用户 配置
     */
    private void restoredPreference() {
        if (mUserPreference == null) return;

        // 恢复转盘数据
        mMenuCount1 = mUserPreference.getData1MenuCount();
        mMenuCount2 = mUserPreference.getData2MenuCount();

        PreferenceUtils.setNormalData1(mUserPreference.getNormalData1());
        PreferenceUtils.setNormalData2(mUserPreference.getNormalData2());
        PreferenceUtils.setNormalData1Backup(mUserPreference.getNormalData1_backup());
        PreferenceUtils.setNormalData2Backup(mUserPreference.getNormalData2_backup());

        PreferenceUtils.setNormalMenuItemCount1(mMenuCount1);
        PreferenceUtils.setNormalMenuItemCount2(mMenuCount2);
        PreferenceUtils.setCallContactItem(mUserPreference.getNormalCallAContact());
        FloatingBallController.getController(getApplication()).resetPanel();

        mMenuItemEditView.resetView();

        mSeekBar.setOnSeekBarChangeListener(null);
        if (mMenuItemEditView.getCurrentPage() == 1) {
            mSeekBar.setProgress(mMenuCount1 - 1);
            mTvMenuCount.setText(String.valueOf(mMenuCount1));
        } else {
            mSeekBar.setProgress(mMenuCount2 - 1);
            mTvMenuCount.setText(String.valueOf(mMenuCount2));
        }
        mSeekBar.setOnSeekBarChangeListener(this);

        // 恢复手势
        GestureController.getInstance(this).restoreNormalGestrue(mUserPreference);
        loadGesture();

        // 清空数据
        mUserPreference = null;
        if (mLayoutUndo != null && (mLayoutUndo.getVisibility() == View.VISIBLE)) {
            mLayoutUndo.setVisibility(View.GONE);
        }

        restartFloatBall();
    }

    /**
     * 显示悬浮球
     */
    public void restartFloatBall() {
        boolean isNowkeyEnable = PreferenceUtils.isShowNowKey(true);
        if (isNowkeyEnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
                } else {
                    Intent serviceIntent = new Intent(NormalModeEditActivity.this, NowKeyService.class);
                    serviceIntent.setAction(NowKeyService.START_NOW_KEY);
                    startService(serviceIntent);
                }
            }
        }
    }

    /**
     * 备份当前的用户 配置
     */
    private void backUpPreference() {
        mUserPreference = new UserPreference();
        String data1 = JsonParser.getJsonStringFromObject(NowKeyPanelModel.getInstance().loadData(1));
        String data2 = JsonParser.getJsonStringFromObject(NowKeyPanelModel.getInstance().loadData(2));
        String data1_backup = PreferenceUtils.getNormalData1Backup("");
        String data2_backup = PreferenceUtils.getNormalData2Backup("");
        String callaContact = PreferenceUtils.getCallContactItem("");

        mUserPreference.setNormalData1(data1);
        mUserPreference.setNormalData2(data2);
        mUserPreference.setNormalData1_backup(data1_backup);
        mUserPreference.setNormalData2_backup(data2_backup);
        mUserPreference.setData1MenuCount(mMenuCount1);
        mUserPreference.setData2MenuCount(mMenuCount2);
        mUserPreference.setNormalCallAContact(callaContact);

        mUserPreference = GestureController.getInstance(this).backUpNormalGestrue(mUserPreference);
    }

    // SeekBak 监听实现以下三个方法
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int currentPage = mMenuItemEditView.getCurrentPage();
        if (currentPage == 1) {
            mMenuCount1 = progress + 1;
            mTvMenuCount.setText(String.valueOf(mMenuCount1));
            PreferenceUtils.setNormalMenuItemCount1(mMenuCount1);
        } else if (currentPage == 2) {
            mMenuCount2 = progress + 1;
            mTvMenuCount.setText(String.valueOf(mMenuCount2));
            PreferenceUtils.setNormalMenuItemCount2(mMenuCount2);
        }

        mMenuItemEditView.resetView();
        FloatingBallController.getController(getApplication()).resetPanel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMenuItemEditView != null) {
            mMenuItemEditView.onDestory();
        }

        mSeekBar = null;
        mTvMenuCount = null;                          // 子菜单的数量textview
        mTvDoubleTap = null;
        mTvLongPress = null;
        mTvLittleDrag = null;
        mTvPage = null;

        mTvUndo = null;                           // 点击重置之后，恢复重置之前的按钮

        mLayoutUndo = null;                 // 撤销重置的布局
        mUserPreference = null;             // 用户配置
        if (mHideUndoLayoutRunable != null) {
            mHandler.removeCallbacks(mHideUndoLayoutRunable);
            mHideUndoLayoutRunable = null;
            mHandler = null;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void changeText(String text, String currentPage) {
        mTvMenuCount.setText(String.valueOf(text));
        mSeekBar.setOnSeekBarChangeListener(null);
        mSeekBar.setProgress(Integer.valueOf(text) - 1);
        mSeekBar.setOnSeekBarChangeListener(this);
        mTvPage.setText(currentPage);
    }

    /**
     * 隐藏 Undo 布局的 R
     */
    class HideUndoLayoutRunable implements Runnable {
        @Override
        public void run() {
            if (mLayoutUndo.getVisibility() == View.VISIBLE) {
                mLayoutUndo.setVisibility(View.GONE);
            }
        }
    }
}
