package com.kuding.nowkey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.JsonParser;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.info.UserPreference;
import com.kuding.nowkey.R;
import com.kuding.nowkey.floatview.GestureController;
import com.kuding.nowkey.floatview.NowKeyPanelModel;
import com.kuding.nowkey.service.NowKeyService;

/**
 * Created by user on 17-2-8.
 */

public class NowKeyActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
        , CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "NowKeyActivity";
    private static final int OVERLAY_PERMISSION_CODE = 1;
    private static final int ALL_REQUEST_CODE = 111;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 112;
    private static final int READ_CONTACT_REQUEST_CODE = 113;
    private static final int CALL_PHONE_REQUEST_CODE = 114;
    private static final long INTERVAL_TIME_RESET = 3000;   // 重置按钮点击有效的时间间隔

    private boolean mShowNowKey;            // 是否显示悬浮球
    private boolean mIsMiniMode = true;     // now key 有两个模式，一种是正常模式，一种是迷你模式，默认是迷你模式。
    private long mLastResetTime = 0;        // 上一次重置Nowkey的时间
    private Context mContext;
    private Switch mNowKeySwitcher;

    private LinearLayout mNowKeyOption;                   // 总开关的布局
    private LinearLayout mNowKeySetting;                  // 总开关下面所有设置项的布局

    //private LinearLayout mNowKeyGesture;
    private RelativeLayout mNowKeyDisplay;                // 显示设置的布局
    //private RelativeLayout mNowKeyHelp;                 // 使用帮助的布局
    private RelativeLayout mNowKeyEdit;                   // 功能编辑的布局
    private RelativeLayout mNowKeyTheme;                  // 主题切换的布局
    private RelativeLayout mLayoutModeSelect;             // 选择模式的视图
    private LinearLayout mLayoutModeSelectDetail;         // 选择模式的详细视图
    private RelativeLayout mLayoutNormal;                 // 选择模式的详细视图 的普通模式
    private RelativeLayout mLayoutMini;                   // 选择模式的详细视图 的迷你模式
    private LinearLayout mLayoutUndo;                     // 撤销重置的布局

    private boolean mIsSelectMode = false;
    private ImageView mImgMode;

    private RadioButton mRbNormal;
    private RadioButton mRbMini;
    private TextView mTvCurrentMode;
    private TextView mTvUndo;                           // 点击重置之后，恢复重置之前的按钮
    private MenuItem mResetItem;
    Handler mHandler = new Handler();

    private UserPreference mUserPreference;              // 用户配置
    private HideUndoLayoutRunable mHideUndoLayoutRunable;// 隐藏Undo 布局 的Runable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_key_main);
        mContext = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //add by yangzhong.gong for task-4394007 begin
            Intent intent = getIntent();
            boolean displayHomeAsUpEnabled = intent.getBooleanExtra("displayHomeAsUpEnabled", false);
            actionBar.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);
            actionBar.setTitle(R.string.application_name);
        }
        PreferenceUtils.registObserver(this);
        checkPermission();


        // 从定制值中 获取 默认值
        //add by yangzhong.gong for task-4596342 begin
        mShowNowKey = this.mContext.getResources().getBoolean(R.bool.def_NowKey_enable_now_key);
        mShowNowKey = PreferenceUtils.isShowNowKey(mShowNowKey);
        PreferenceUtils.setIsShowNowKey(mShowNowKey);
        //add by yangzhong.gong for task-4596342 end

        mIsMiniMode = this.mContext.getResources().getBoolean(R.bool.def_NowKey_is_mini_mode);
        mIsMiniMode = PreferenceUtils.isMiniMode(mIsMiniMode);
        PreferenceUtils.setIsMiniMode(mIsMiniMode);

        initView();

        if (mShowNowKey) {
            mNowKeySetting.setVisibility(View.VISIBLE);
            showFloatBall();
        } else {
            mNowKeySetting.setVisibility(View.GONE);
            Intent serviceIntent = new Intent(mContext, NowKeyService.class);
            stopService(serviceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        mResetItem = menu.findItem(R.id.action_reset);
        //add by yangzhong.gong for defect-5067436 begin
        if (mShowNowKey) {
            if (mResetItem != null) {
                mResetItem.setVisible(true);
            }
        } else {
            if (mResetItem != null) {
                mResetItem.setVisible(false);
            }
        }
        //add by yangzhong.gong for defect-5067436 end
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceUtils.unRegistObserver(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //add by yangzhong.gong for task-4394007 begin
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_reset:
                resetAll();
                break;
            default:
                break;
        }
        //add by yangzhong.gong for task-4394007 end
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initView() {
        mIsMiniMode = PreferenceUtils.isMiniMode(false);
        mShowNowKey = PreferenceUtils.isShowNowKey(true);
        // 总开关
        mNowKeyOption = (LinearLayout) findViewById(R.id.now_key_option_layout);
        mNowKeyOption.setOnClickListener(this);
        mNowKeySwitcher = (Switch) findViewById(R.id.now_key_switcher);
        mNowKeySwitcher.setChecked(mShowNowKey);
        mNowKeySwitcher.setOnCheckedChangeListener(this);

        // 包含以下所有开关的布局 根据总开关 来判断 是否显示
        mNowKeySetting = (LinearLayout) findViewById(R.id.layout_mian_setting);
        if (mShowNowKey) {
            mNowKeySetting.setVisibility(View.VISIBLE);
        } else {
            mNowKeySetting.setVisibility(View.GONE);
        }

        // 模式选择
        mLayoutNormal = (RelativeLayout) findViewById(R.id.now_key_layout_mode_normal);
        mLayoutMini = (RelativeLayout) findViewById(R.id.now_key_layout_mode_mini);
        mLayoutNormal.setOnClickListener(this);
        mLayoutMini.setOnClickListener(this);
        mLayoutModeSelect = (RelativeLayout) findViewById(R.id.now_key_mode_layout);
        mLayoutModeSelect.setOnClickListener(this);
        mImgMode = (ImageView) findViewById(R.id.img_mode);

        // 模式选择 详细
        mLayoutModeSelectDetail = (LinearLayout) this.findViewById(R.id.layout_mode_select);
        mRbNormal = (RadioButton) findViewById(R.id.rb_mode_normal);
        mRbMini = (RadioButton) findViewById(R.id.rb_mode_mini);
        mTvCurrentMode = (TextView) findViewById(R.id.tv_mode_current);
        if (mIsMiniMode) {
            mRbNormal.setChecked(false);
            mRbMini.setChecked(true);
            mTvCurrentMode.setText(R.string.nowkey_mode_mini);
        } else {
            mRbNormal.setChecked(true);
            mRbMini.setChecked(false);
            mTvCurrentMode.setText(R.string.nowkey_mode_normal);
        }
        mRbNormal.setOnCheckedChangeListener(this);
        mRbMini.setOnCheckedChangeListener(this);


        // 主题
        mNowKeyTheme = (RelativeLayout) findViewById(R.id.nowkey_theme_layout);
        mNowKeyTheme.setOnClickListener(this);

        // 手势
        //mNowKeyGestureContainer = (LinearLayout) findViewById(R.id.gesture_container);
        //mNowKeyGesture = (LinearLayout) findViewById(R.id.now_key_gesture_layout);
        //mNowKeyGesture.setOnClickListener(this);

        // 显示
        mNowKeyDisplay = (RelativeLayout) findViewById(R.id.now_key_display_layout);
        mNowKeyDisplay.setOnClickListener(this);

        // 编辑
        mNowKeyEdit = (RelativeLayout) findViewById(R.id.now_key_edit_layout);
        mNowKeyEdit.setOnClickListener(this);

        // 帮助
        //mNowKeyHelp = (RelativeLayout) findViewById(R.id.now_key_help_layout);
        //mNowKeyHelp.setOnClickListener(this);

        //撤销重置的布局
        mLayoutUndo = (LinearLayout) findViewById(R.id.layout_undo_main);
        mTvUndo = (TextView) mLayoutUndo.findViewById(R.id.tv_undo);
        mTvUndo.setOnClickListener(this);
    }

    /**
     * 显示悬浮球
     */
    public void showFloatBall() {
        mShowNowKey = PreferenceUtils.isShowNowKey(true);
        if (mShowNowKey) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
                } else {
                    Intent serviceIntent = new Intent(mContext, NowKeyService.class);
                    serviceIntent.setAction(NowKeyService.START_NOW_KEY);
                    startService(serviceIntent);
                }
            }
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CALL_PHONE},
                    ALL_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            // 申请授权。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            // 申请授权。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACT_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            // 申请授权。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ALL_REQUEST_CODE
                || requestCode == WRITE_STORAGE_PERMISSION_REQUEST_CODE
                || requestCode == READ_CONTACT_REQUEST_CODE
                || requestCode == CALL_PHONE_REQUEST_CODE) {
            showFloatBall();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            if (Settings.canDrawOverlays(this)) {
                showFloatBall();
            } else {
                mNowKeySwitcher.setChecked(false);
                Toast.makeText(mContext, R.string.overlay_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetAll() {
        // 避免用户重复点击重置 ，需要设置点击的时间间隔
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastResetTime < INTERVAL_TIME_RESET) {
            return;
        }
        mLastResetTime = currentTime;

        // 先备份当前的数据，以便用户恢复
        backUpPreference();

        //重置主题
        //PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        PreferenceUtils.setNowKeyTheme(Color.parseColor(Constant.DEFAULT_THEME));
        PreferenceUtils.setNowKeyThemeIndex(0);

        // 设置默认的nowkey 开关
        boolean isNowKeyEnable = this.getResources().getBoolean(R.bool.def_NowKey_enable_now_key);
        PreferenceUtils.isShowNowKey(isNowKeyEnable);

        // 设置默认的Nowkey 模式
        boolean isMiniMode = this.getResources().getBoolean(R.bool.def_NowKey_is_mini_mode);
        PreferenceUtils.setIsMiniMode(isMiniMode);

        //重置数据
        NowKeyPanelModel.getInstance().initExtraData();
        NowKeyPanelModel.getInstance().resetAlldata();

        initView();

        // 重置透明度和大小
        PreferenceUtils.setFloatBallViewOpacity(50);
        PreferenceUtils.setFloatBallViewSize(50);

        // 重置手势
        GestureController.getInstance(this).resetGesture();

        // 重置短拉介绍
        PreferenceUtils.setIsNeedShortDragIntroduce(true);

        // 重置快捷联系人信息
        PreferenceUtils.setCallContactItem("");
        PreferenceUtils.setCallContactItemMini("");

        // 重置Nowkey的开关,还有默认显示迷你模式 并且重新启动悬浮求服务
        showFloatBall();

        showUndoLayout();
        //Toast.makeText(this, R.string.now_key_reset_toast, Toast.LENGTH_SHORT).show();
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
     * 备份当前的用户 配置
     */
    private void backUpPreference() {
        mUserPreference = new UserPreference();

        int theme = PreferenceUtils.getNowKeyTheme(Color.parseColor(Constant.DEFAULT_THEME));
        int themeIndex = PreferenceUtils.getNowKeyThemeIndex(0);
        boolean isMiniMode = PreferenceUtils.isMiniMode(false);
        boolean isShowNowkey = true;
        String data1 = JsonParser.getJsonStringFromObject(NowKeyPanelModel.getInstance().loadData(1));
        String data2 = JsonParser.getJsonStringFromObject(NowKeyPanelModel.getInstance().loadData(2));
        String miniData = JsonParser.getJsonStringFromObject(NowKeyPanelModel.getInstance().loadMiniData());
        String CallaContactNromal = PreferenceUtils.getCallContactItem("");
        String CallaContactMini = PreferenceUtils.getCallContactItemMini("");

        mUserPreference.setNowKeyTheme(theme);
        mUserPreference.setNowKeyThemeIndex(themeIndex);
        mUserPreference.setMiniMode(isMiniMode);
        mUserPreference.setShowNowKey(isShowNowkey);
        mUserPreference.setMiniData(miniData);
        mUserPreference.setNormalData1(data1);
        mUserPreference.setNormalData2(data2);

        mUserPreference.setMiniMenuCount(PreferenceUtils.getMiniMenuItemCount(8));
        mUserPreference.setData1MenuCount(PreferenceUtils.getNormalMenuItemCount1(8));
        mUserPreference.setData2MenuCount(PreferenceUtils.getNormalMenuItemCount2(8));
        mUserPreference.setNormalCallAContact(CallaContactNromal);
        mUserPreference.setMiniCallAContact(CallaContactMini);

        mUserPreference.setmFloatBallOpacity(PreferenceUtils.getFloatBallViewOpacity(50));
        mUserPreference.setmFloatBallSize(PreferenceUtils.getFloatBallViewSize(50));

        mUserPreference.setShowIntroduce(PreferenceUtils.isNeedShowShortDragIntroduce(true));
        mUserPreference = GestureController.getInstance(mContext).backUpAllGestrue(mUserPreference);
    }

    /**
     * 恢复 用户 配置
     */
    private void restoredPreference() {
        if (mUserPreference == null) return;

        // 恢复主题
        PreferenceUtils.setNowKeyTheme(mUserPreference.getNowKeyTheme());
        PreferenceUtils.setNowKeyThemeIndex(mUserPreference.getNowKeyThemeIndex());

        // 恢复转盘数据
        PreferenceUtils.setMiniData(mUserPreference.getMiniData());
        PreferenceUtils.setNormalData1(mUserPreference.getNormalData1());
        PreferenceUtils.setNormalData2(mUserPreference.getNormalData2());

        PreferenceUtils.setMiniMenuItemCount(mUserPreference.getMiniMenuCount());
        PreferenceUtils.setNormalMenuItemCount1(mUserPreference.getData1MenuCount());
        PreferenceUtils.setNormalMenuItemCount2(mUserPreference.getData2MenuCount());
        // 恢复开关
        PreferenceUtils.setIsShowNowKey(mUserPreference.isShowNowKey());
        PreferenceUtils.setIsMiniMode(mUserPreference.isMiniMode());

        //重置视图
        initView();

        // 恢复手势
        GestureController.getInstance(this).restorepAllGestrue(mUserPreference);

        // 恢复透明度和大小
        PreferenceUtils.setFloatBallViewSize(mUserPreference.getmFloatBallSize());
        PreferenceUtils.setFloatBallViewOpacity(mUserPreference.getmFloatBallOpacity());

        // 恢复短拉介绍
        PreferenceUtils.setIsNeedShortDragIntroduce(mUserPreference.isShowIntroduce());

        // 恢复 快捷联系人
        PreferenceUtils.setCallContactItemMini(mUserPreference.getMiniCallAContact());
        PreferenceUtils.setCallContactItem(mUserPreference.getNormalCallAContact());

        mUserPreference = null;

        // 启动悬浮求服务
        showFloatBall();

        if (mLayoutUndo != null && (mLayoutUndo.getVisibility() == View.VISIBLE)) {
            mLayoutUndo.setVisibility(View.GONE);
        }
    }

    /**
     * 处理 NowKey 总开关
     *
     * @param isChecked
     */
    private void handleNowKeySwitch(boolean isChecked) {
        Log.d(TAG, " handleNowKeySwitch");
        mShowNowKey = PreferenceUtils.isShowNowKey(true);
        if (mShowNowKey != isChecked) {
            mShowNowKey = isChecked;
            PreferenceUtils.setIsShowNowKey(isChecked);

            if (isChecked) {
                mNowKeySetting.setVisibility(View.VISIBLE);
                showFloatBall();
                // 开启Nowkey之后，显示重置按钮
                if (mResetItem != null) {
                    mResetItem.setVisible(true);
                }
            } else {
                Intent intent = new Intent(mContext, NowKeyService.class);
                mNowKeySetting.setVisibility(View.GONE);
                stopService(intent);

                // 关闭Nowkey之后，隐藏重置按钮
                if (mResetItem != null) {
                    mResetItem.setVisible(false);
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceUtils.NOW_KEY_OPTION)) {
            Log.d(TAG, "anxi onSharedPreferenceChanged NOW_KEY_OPTION");

            boolean show = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
            if (mShowNowKey != show) {
                if (mNowKeySwitcher != null) {
                    mNowKeySwitcher.setChecked(show);
                    //add by yangzhong.gong for defect-4451135 begin
                    if (show == false) {
                        mNowKeySetting.setVisibility(View.GONE);
                        // 关闭Nowkey之后，隐藏重置按钮
                        if (mResetItem != null) {
                            mResetItem.setVisible(false);
                        }
                    } else {
                        mNowKeySetting.setVisibility(View.VISIBLE);
                        showFloatBall();
                        // 开启Nowkey之后，显示重置按钮
                        if (mResetItem != null) {
                            mResetItem.setVisible(true);
                        }
                    }
                    //add by yangzhong.gong for defect-4451135 end
                }
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        Bundle bundle = new Bundle();
        switch (id) {
            case R.id.now_key_switcher:
                handleNowKeySwitch(isChecked);
                break;
            case R.id.rb_mode_normal:
                if (isChecked) {
                    mRbMini.setChecked(false);
                    PreferenceUtils.setIsMiniMode(false);
                }

                // 隐藏菜单
                mIsSelectMode = false;
                mImgMode.setBackgroundResource(R.drawable.expander_open_holo_light);
                mLayoutModeSelectDetail.setVisibility(View.GONE);

                // 符合条件 重启 normal模式的悬浮求
                if (mIsMiniMode == true && isChecked == true && mShowNowKey) {
                    mIsMiniMode = false;
                    showFloatBall();
                    mTvCurrentMode.setText(R.string.nowkey_mode_normal);
                }
                break;
            case R.id.rb_mode_mini:
                if (isChecked) {
                    mRbNormal.setChecked(false);
                    PreferenceUtils.setIsMiniMode(true);
                }

                // 隐藏菜单
                mIsSelectMode = false;
                mImgMode.setBackgroundResource(R.drawable.expander_open_holo_light);
                mLayoutModeSelectDetail.setVisibility(View.GONE);

                // 符合条件 重启 mini模式的悬浮求
                if (mIsMiniMode == false && isChecked == true && mShowNowKey) {
                    mIsMiniMode = true;
                    showFloatBall();
                    mTvCurrentMode.setText(R.string.nowkey_mode_mini);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = null;
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.now_key_option_layout:
                if (mNowKeySwitcher != null) {
                    mNowKeySwitcher.setChecked(!mShowNowKey);
                }
                break;
            case R.id.nowkey_theme_layout:
                if (!mShowNowKey) return;
                i = new Intent();
                i.setClass(NowKeyActivity.this, ThemeSettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
            /*case R.id.now_key_gesture_layout:
                if (!mShowNowKey) return;
                i.setClass(NowKeyActivity.this, GestureSettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;*/
            case R.id.now_key_display_layout:
                if (!mShowNowKey) return;
                i = new Intent();
                i.setClass(NowKeyActivity.this, DisplaySettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
            case R.id.now_key_edit_layout:
                if (!mShowNowKey) return;
                i = new Intent();
                if (mIsMiniMode) {
                    i.setClass(NowKeyActivity.this, MiniModeEditActivity.class);
                } else {
                    i.setClass(NowKeyActivity.this, NormalModeEditActivity.class);
                }
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
            /*case R.id.now_key_help_layout:
                if (!mShowNowKey) return;
                i = new Intent();
                i.setClass(NowKeyActivity.this, NowKeyHelpActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;*/
            case R.id.now_key_mode_layout:
                if (mIsSelectMode) {
                    mIsSelectMode = false;
                    mImgMode.setBackgroundResource(R.drawable.expander_open_holo_light);
                    mLayoutModeSelectDetail.setVisibility(View.GONE);
                } else {
                    mIsSelectMode = true;
                    mImgMode.setBackgroundResource(R.drawable.expander_close_holo_light);
                    mLayoutModeSelectDetail.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.now_key_layout_mode_normal:
                mRbNormal.setChecked(true);
                break;
            case R.id.now_key_layout_mode_mini:
                mRbMini.setChecked(true);
                break;
            case R.id.tv_undo:
                restoredPreference();
                break;
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
        mHandler.postDelayed(mHideUndoLayoutRunable, INTERVAL_TIME_RESET);
    }

    /**
     * 隐藏 Undo 布局
     */
    class HideUndoLayoutRunable implements Runnable {
        @Override
        public void run() {
            if (mLayoutUndo.getVisibility() == View.VISIBLE) {
                Log.d(TAG, "anxi mLayoutUndo.setVisibility(View.GONE)");
                mLayoutUndo.setVisibility(View.GONE);
            }
        }
    }
}

