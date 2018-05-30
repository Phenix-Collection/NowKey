package com.kuding.superball;

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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.FunctionUtils;
import com.kuding.superball.Utils.JsonParser;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.info.FunctionItemInfo;
import com.kuding.superball.info.UserPreference;
import com.kuding.superball.views.EditMenuItemView_Mini;
import com.kuding.superball.R;
import com.kuding.superball.floatview.GestureController;
import com.kuding.superball.floatview.MiniBallController;
import com.kuding.superball.floatview.NowKeyPanelModel;
import com.kuding.superball.service.NowKeyService;

/**
 * 编辑 迷你模式 的 Activity
 */

public class MiniModeEditActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private final static String TAG = "MiniModeEditActivity";
    private int ADD_ITEM_REQ_CODE = 100;
    private int ADD_QESTRUE_CODE = 200;
    private int OVERLAY_PERMISSION_CODE = 300;
    private static final long INTERVAL_TIME_RESET = 3000;   // 重置按钮点击有效的时间间隔

    private long mLastResetTime = 0;        // 上一次重置Nowkey的时间

    private ActionBar mActionBar;
    private int mViewNumber = 8;                // 子菜单数量
    private boolean mIsMiniMode = true;         // 悬浮球是否迷你模式

    private EditMenuItemView_Mini mMenuItemEditView;    // 设置迷你模式 转盘 子菜单 的控件
    private RadioGroup mRgNumber;                       // 设置迷你模式 转盘 子菜单数量 的单选框
    private TextView mTvDoubleTap;
    private TextView mTvLongPress;
    private TextView mTvLittleDrag;
    private TextView mTvUndo;                           // 点击重置之后，恢复重置之前的按钮

    private LinearLayout mLayoutUndo;
    private UserPreference mUserPreference;             // 用户配置
    private HideUndoLayoutRunable mHideUndoLayoutRunable;// 隐藏Undo 布局 的Runable
    private Handler mHandler = new Handler();
    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mini_mode_edit_activity);
        mIsMiniMode = PreferenceUtils.isMiniMode(false);
        mViewNumber = PreferenceUtils.getMiniMenuItemCount(8);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.now_key_option_gesture_and_function_title);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGesture();
    }

    private void initView() {
        mMenuItemEditView = (EditMenuItemView_Mini) findViewById(R.id.view_menu_item_edit);
        mRgNumber = (RadioGroup) findViewById(R.id.rg_number);
        initRadioGroup();

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

    private void initRadioGroup() {
        mRgNumber.setOnCheckedChangeListener(null);
        //根据显示子菜单的数量，初始化界面
        int count = PreferenceUtils.getMiniMenuItemCount(8);
        switch (count) {
            case 3:
                mRgNumber.check(R.id.rb_3);
                mMenuItemEditView.setMenuItemCount(3);
                break;
            case 4:
                mRgNumber.check(R.id.rb_4);
                mMenuItemEditView.setMenuItemCount(4);
                break;
            case 5:
                mRgNumber.check(R.id.rb_5);
                mMenuItemEditView.setMenuItemCount(5);
                break;
            case 8:
                mRgNumber.check(R.id.rb_8);
                mMenuItemEditView.setMenuItemCount(8);
                break;
            default:
                mRgNumber.check(R.id.rb_8);
                mMenuItemEditView.setMenuItemCount(8);
                break;
        }

        mRgNumber.setOnCheckedChangeListener(this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_reset) {
            reset();
        }
        return super.onOptionsItemSelected(item);
    }

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
        NowKeyPanelModel.getInstance().resetMinidata();
        MiniBallController.getController(getApplication()).resetPanel();

        //重置手势
        GestureController.getInstance(this).resetMiniModeDefaultGesture();
        loadGesture();

        // 重置快捷联系人
        PreferenceUtils.setCallContactItemMini("");

        mRgNumber.setOnCheckedChangeListener(null);
        mRgNumber.check(R.id.rb_8);
        mRgNumber.setOnCheckedChangeListener(this);

        mMenuItemEditView.resetData();

        restartFloatBall();
        showUndoLayout();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, " onActivityResult ");
        if (requestCode == ADD_ITEM_REQ_CODE) {
            initRadioGroup();
        } else if (requestCode == ADD_QESTRUE_CODE) {
            loadGesture();
        } else if (requestCode == OVERLAY_PERMISSION_CODE) {
            if (Settings.canDrawOverlays(this)) {
                restartFloatBall();
            }
        }
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
                    Intent serviceIntent = new Intent(MiniModeEditActivity.this, NowKeyService.class);
                    serviceIntent.setAction(NowKeyService.START_NOW_KEY);
                    startService(serviceIntent);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, " onConfigurationChanged");
        if (mLayoutUndo.getVisibility() == View.VISIBLE) {
            mLayoutUndo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        boolean isGestrueOperate = false;
        Intent intent = new Intent();
        intent.setClass(MiniModeEditActivity.this, FunctionActivity.class);
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
        mViewNumber = mUserPreference.getMiniMenuCount();
        PreferenceUtils.setMiniData(mUserPreference.getMiniData());
        PreferenceUtils.setMiniMenuItemCount(mViewNumber);
        PreferenceUtils.setMiniDataBackup(mUserPreference.getMiniData_backup());
        PreferenceUtils.setCallContactItemMini(mUserPreference.getMiniCallAContact());
        MiniBallController.getController(getApplication()).resetPanel();

        // 恢复 编辑转盘的数据
        mRgNumber.setOnCheckedChangeListener(null);
        switch (mViewNumber) {
            case 3:
                mRgNumber.check(R.id.rb_3);
                break;
            case 4:
                mRgNumber.check(R.id.rb_4);
                break;
            case 5:
                mRgNumber.check(R.id.rb_5);
                break;
            case 8:
                mRgNumber.check(R.id.rb_8);
                break;
        }
        mRgNumber.setOnCheckedChangeListener(this);
        mMenuItemEditView.setMenuItemCount(mViewNumber);

        // 恢复手势
        GestureController.getInstance(this).restoreMiniGestrue(mUserPreference);
        loadGesture();

        // 清空数据
        mUserPreference = null;
        if (mLayoutUndo != null && (mLayoutUndo.getVisibility() == View.VISIBLE)) {
            mLayoutUndo.setVisibility(View.GONE);
        }

        restartFloatBall();
    }

    /**
     * 备份当前的用户 配置
     */
    private void backUpPreference() {
        mUserPreference = new UserPreference();
        String miniData = JsonParser.getJsonStringFromObject(NowKeyPanelModel.getInstance().loadMiniData());
        String miniData_backup = PreferenceUtils.getMiniDataBackup("");
        String callaContact = PreferenceUtils.getCallContactItemMini("");

        mUserPreference.setMiniData(miniData);
        mUserPreference.setMiniData_backup(miniData_backup);
        mUserPreference.setMiniMenuCount(mViewNumber);
        mUserPreference.setMiniCallAContact(callaContact);
        mUserPreference = GestureController.getInstance(this).backUpMiniGestrue(mUserPreference);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_3:
                mViewNumber = 3;
                break;
            case R.id.rb_4:
                mViewNumber = 4;
                break;
            case R.id.rb_5:
                mViewNumber = 5;
                break;
            case R.id.rb_8:
                mViewNumber = 8;
                break;
            default:
                mViewNumber = 8;
                break;
        }
        mMenuItemEditView.setMenuItemCount(mViewNumber);
        PreferenceUtils.setMiniMenuItemCount(mViewNumber);
        if (mIsMiniMode) {
            MiniBallController.getController(getApplication()).resetPanel();
        }
        Log.d(TAG, "mViewNumber = " + mViewNumber);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMenuItemEditView != null) {
            mMenuItemEditView.onDestory();
        }

        mRgNumber.setOnCheckedChangeListener(null);
        mRgNumber = null;

        mTvDoubleTap = null;
        mTvLongPress = null;
        mTvLittleDrag = null;
        mTvUndo = null;
        mLayoutUndo = null;
        mUserPreference = null;
        if (mHideUndoLayoutRunable != null) {
            mHandler.removeCallbacks(mHideUndoLayoutRunable);
            mHideUndoLayoutRunable = null;
            mHandler = null;
        }
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
