package com.kuding.superball.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kuding.superball.NowKeyApplication;

/**
 * Created by user on 17-1-9.
 */

public class PreferenceUtils {

    static NowKeyApplication mApp = NowKeyApplication.getInstance();

    public static final String NOW_KEY_THEME = "now_key_theme";
    public static final String NOW_KEY_OPTION = "now_key_option";
    public static final String NOW_KEY_MODE = "now_key_mode";
    public static final String NOW_KEY_THEME_INDEX = "now_key_theme_index";

    // 普通模式 悬浮球 双击 事件
    public static final String NORMAL_GESTURE_DOUBLE_CLICK = "normal_gesture_double_click";
    public static final String NORMAL_GESTURE_DOUBLE_CLICK_TYPE = "normal_gesture_double_click_type";
    public static final String NORMAL_GESTURE_DOUBLE_CLICK_ACTION = "normal_gesture_double_click_action";
    // 普通模式 悬浮球 长按 事件
    public static final String NORMAL_GESTURE_LONG_PRESS = "normal_gesture_long_press";
    public static final String NORMAL_GESTURE_LONG_PRESS_TYPE = "normal_gesture_long_press_type";
    public static final String NORMAL_GESTURE_LONG_PRESS_ACTION = "normal_gesture_long_press_action";
    // 普通模式 悬浮球 短拉 事件
    public static final String NORMAL_GESTURE_SHORT_DRAG = "normal_gesture_short_drag";
    public static final String NORMAL_GESTURE_SHORT_DRAG_TYPE = "normal_gesture_short_drag_type";
    public static final String NORMAL_GESTURE_SHORT_DRAG_ACTION = "normal_gesture_short_drag_action";
    // 迷你模式 悬浮球 双击 事件
    public static final String MINI_GESTURE_DOUBLE_CLICK = "mini_gesture_double_click";
    public static final String MINI_GESTURE_DOUBLE_CLICK_TYPE = "mini_gesture_double_click_type";
    public static final String MINI_GESTURE_DOUBLE_CLICK_ACTION = "mini_gesture_double_click_action";
    // 迷你模式 悬浮球 长按 事件
    public static final String MINI_GESTURE_LONG_PRESS = "mini_gesture_long_press";
    public static final String MINI_GESTURE_LONG_PRESS_TYPE = "mini_gesture_long_press_type";
    public static final String MINI_GESTURE_LONG_PRESS_ACTION = "mini_gesture_long_press_action";
    // 迷你模式 悬浮球 短拉 事件
    public static final String MINI_GESTURE_SHORT_DRAG = "mini_gesture_short_drag";
    public static final String MINI_GESTURE_SHORT_DRAG_TYPE = "mini_gesture_short_drag_type";
    public static final String MINI_GESTURE_SHORT_DRAG_ACTION = "mini_gesture_short_drag_action";


    public static final String NOW_KEY_ITEM_ACTION_PAGE_1 = "now_key_item_action_page1";//普通悬浮求 第一页数据
    public static final String NOW_KEY_ITEM_ACTION_PAGE_1_BACKUP = "now_key_item_action_page1_backup";//普通悬浮求 第一页数据的备份
    public static final String NOW_KEY_ITEM_ACTION_PAGE_2 = "now_key_item_action_page2";//普通悬浮球 第二页数据
    public static final String NOW_KEY_ITEM_ACTION_PAGE_2_BACKUP = "now_key_item_action_page2_backup";//普通悬浮球 第二页数据的备份
    public static final String NOW_KEY_ITEM_DATA_MINI = "now_key_item_data_mini";   // 迷你悬浮球的数据
    public static final String NOW_KEY_ITEM_DATA_MINI_BACKUP = "now_key_item_data_mini_backup";   // 迷你悬浮球的数据的备份

    public static final String NOW_KEY_CALL_A_CONTACT_ITEM = "now_key_call_a_contact_item";             //Normal转盘里面 快捷联系人 的信息
    public static final String NOW_KEY_CALL_A_CONTACT_ITEM_MINI = "now_key_call_a_contact_item_mini";   //Mini  转盘里面 快捷联系人 的信息

    public static final String NORMAL_CALL_A_CONTACT_DOUBLE_CLICK = "normal_call_a_contact_double_click";// Normal 悬浮球双击事件 快捷联系人 的信息
    public static final String NORMAL_CALL_A_CONTACT_LONG_PRESS = "normal_call_a_contact_long_press";    // Normal 悬浮球长按事件 快捷联系人 的信息
    public static final String NORMAL_CALL_A_CONTACT_SHORT_DRAG = "normal_call_a_contact_short_drag";    // Normal 悬浮球短拉事件 快捷联系人 的信息

    public static final String MINI_CALL_A_CONTACT_DOUBLE_CLICK = "mini_call_a_contact_double_click";    // Mini   悬浮球双击事件 快捷联系人 的信息
    public static final String MINI_CALL_A_CONTACT_LONG_PRESS = "mini_call_a_contact_long_press";        // Mini   悬浮球长按事件 快捷联系人 的信息
    public static final String MINI_CALL_A_CONTACT_SHORT_DRAG = "mini_call_a_contact_short_drag";        // Mini   悬浮球短拉事件 快捷联系人 的信息

    public static final String NOW_KEY_BORDER_OPTION = "now_key_border_option";
    public static final String NOW_KEY_FIRST_SET_DOUBLE_CLICK = "now_key_first_set_double_click";
    public static final String NOW_KEY_FIRST_SET_LONG_CLICK = "now_key_first_set_long_click";
    public static final String NOW_KEY_FLOAT_VIEW_SIZE = "now_key_float_view_size";         // 悬浮球 的 大小
    public static final String NOW_KEY_FLOAT_VIEW_OPACITY = "now_key_float_view_opacity";   // 悬浮球的透明度
    public static final String NOW_KEY_MINI_MODE_ANIMATION_MODE = "now_key_mini_mode_animation_mode";
    public static final String NOW_KEY_MINI_PANEL_MENU_COUNT = "now_key_mini_panel_menu_count"; // 迷你转盘子菜单的个数
    public static final String NOW_KEY_NORMAL_PANEL_MENU_COUNT1 = "now_key_normal_panel_menu_count1"; // 普通模式转盘第一页子菜单的个数
    public static final String NOW_KEY_NORMAL_PANEL_MENU_COUNT2 = "now_key_normal_panel_menu_count2"; // 普通模式转盘第二页子菜单的个数
    public static final String IS_FIRST_TIME_USE_NOW_KEY = "IS_FIRST_TIME_USE_NOW_KEY";  // 是否第一次使用nowkey
    public static final String IS_NEED_SHOW_SHORT_DRAG_INTRODUCE = "is_need_show_short_drag_introduce";  // 是否需要显示短拉介绍

    public static final String MINI_FLOAT_BALL_X = "mini_float_ball_x";      // mini悬浮球的x坐标
    public static final String MINI_FLOAT_BALL_Y = "mini_float_ball_y";      // mini悬浮球的y坐标
    public static final String NORMAL_FLOAT_BALL_X = "normal_float_ball_x";  // normal悬浮球的x坐标
    public static final String NORMAL_FLOAT_BALL_Y = "normal_float_ball_y";  // normal悬浮球的y坐标


    public static void setBoolean(String key, boolean val) {
        PreferenceManager.getDefaultSharedPreferences(mApp).edit().putBoolean(key, val).apply();
    }

    public static boolean getBoolean(String key, boolean defVal) {
        return PreferenceManager.getDefaultSharedPreferences(mApp).getBoolean(key, defVal);
    }

    public static void setInt(String key, int val) {
        PreferenceManager.getDefaultSharedPreferences(mApp).edit().putInt(key, val).apply();
    }

    public static int getInt(String key, int defVal) {
        return PreferenceManager.getDefaultSharedPreferences(mApp).getInt(key, defVal);
    }

    public static void setString(String key, String val) {
        PreferenceManager.getDefaultSharedPreferences(mApp).edit().putString(key, val).apply();
    }

    public static String getString(String key, String defVal) {
        return PreferenceManager.getDefaultSharedPreferences(mApp).getString(key, defVal);
    }

    public static void registObserver(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(mApp).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unRegistObserver(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(mApp).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void setFloat(String key, float val) {
        PreferenceManager.getDefaultSharedPreferences(mApp).edit()
                .putFloat(key, val).apply();
    }

    public static Float getFloat(String key, float defVal) {
        return PreferenceManager.getDefaultSharedPreferences(mApp).getFloat(key, defVal);
    }

    public static int getAnimationMode(int dafaultMode) {
        return getInt(NOW_KEY_MINI_MODE_ANIMATION_MODE, dafaultMode);
    }

    public static void setAnimationMode(int values) {
        setInt(NOW_KEY_MINI_MODE_ANIMATION_MODE, values);
    }

    /**
     * 设置Nowkey 是否显示 迷你模式
     */
    public static void setIsMiniMode(boolean values) {
        setBoolean(NOW_KEY_MODE, values);
    }

    /**
     * 读取Nowkey是否显示 迷你 模式
     */
    public static boolean isMiniMode(boolean dafaultValue) {
        return getBoolean(NOW_KEY_MODE, dafaultValue);
    }

    /**
     * 设置是否显示悬浮球
     */
    public static void setIsShowNowKey(boolean values) {
        setBoolean(NOW_KEY_OPTION, values);
    }

    /**
     * 读取是否显示悬浮球
     */
    public static boolean isShowNowKey(boolean dafaultValue) {
        return getBoolean(NOW_KEY_OPTION, dafaultValue);
    }

    /**
     * 读取 迷你 悬浮球 的数据
     *
     * @param defaultValues
     * @return
     */
    public static String getMiniData(String defaultValues) {
        return getString(NOW_KEY_ITEM_DATA_MINI, defaultValues);
    }

    /**
     * 保存 迷你 悬浮球 的数据
     *
     * @param values
     */
    public static void setMiniData(String values) {
        setString(NOW_KEY_ITEM_DATA_MINI, values);
    }

    /**
     * 保存 迷你 悬浮球 的数据，同时根据数量，选择是否备份
     *
     * @param values
     */
    public static void setMiniDataWithNumber(String values, int number) {
        setString(NOW_KEY_ITEM_DATA_MINI, values);
        if (number == 8) {
            setMiniDataBackup(values);
        }
        setMiniMenuItemCount(number);
    }

    /**
     * 读取 迷你 悬浮球 的 备份 数据
     *
     * @param defaultValues
     * @return
     */
    public static String getMiniDataBackup(String defaultValues) {
        return getString(NOW_KEY_ITEM_DATA_MINI_BACKUP, defaultValues);
    }

    /**
     * 保存 迷你 悬浮球 的 备份 数据
     *
     * @param values
     */
    public static void setMiniDataBackup(String values) {
        setString(NOW_KEY_ITEM_DATA_MINI_BACKUP, values);
    }


    /**
     * 获取 普通模式 悬浮球 第一页 的数据
     *
     * @param defaultValues
     * @return
     */
    public static String getNormalData1(String defaultValues) {
        return getString(NOW_KEY_ITEM_ACTION_PAGE_1, defaultValues);
    }

    /**
     * 保存 普通模式 悬浮球 第一页 的数据
     * 如果当前保存的子菜单有 8个 ，则备份一下数据
     *
     * @param values
     */
    public static void setNormalData1(String values) {
        setString(NOW_KEY_ITEM_ACTION_PAGE_1, values);
        if (getNormalMenuItemCount1(0) == 8) {
            setNormalData1Backup(values);
        }
    }


    /**
     * 获取 普通模式 悬浮球 第一页的 备份 数据
     * 设置这个备份的原因是方便用户在 编辑界面 快速添加新的子菜单
     *
     * @param defaultValues
     * @return
     */
    public static String getNormalData1Backup(String defaultValues) {
        return getString(NOW_KEY_ITEM_ACTION_PAGE_1_BACKUP, defaultValues);
    }

    /**
     * 保存 普通模式 悬浮球 第一页的 备份 数据
     * 当子菜单有8个数据的时候，进行数据备份
     * 设置这个备份的原因是方便用户在 编辑界面 快速添加新的子菜单
     *
     * @param values
     */
    public static void setNormalData1Backup(String values) {
        Log.d("anxi", "utils setNormalData1Backup ");
        setString(NOW_KEY_ITEM_ACTION_PAGE_1_BACKUP, values);
    }

    /**
     * 获取 普通模式 悬浮球 第二页 的数据
     *
     * @param defaultValues
     * @return
     */
    public static String getNormalData2(String defaultValues) {
        return getString(NOW_KEY_ITEM_ACTION_PAGE_2, defaultValues);
    }

    /**
     * 保存 普通模式 悬浮球 第二页 的数据
     * 如果当前保存的子菜单有 8个 ，则备份一下数据
     *
     * @param values
     */
    public static void setNormalData2(String values) {
        setString(NOW_KEY_ITEM_ACTION_PAGE_2, values);
        if (getNormalMenuItemCount2(0) == 8) {
            setNormalData2Backup(values);
        }
    }


    /**
     * 获取 普通模式 悬浮球 第二页的 备份 数据
     * 设置这个备份的原因是方便用户在 编辑界面 快速添加新的子菜单
     *
     * @param defaultValues
     * @return
     */
    public static String getNormalData2Backup(String defaultValues) {
        return getString(NOW_KEY_ITEM_ACTION_PAGE_2_BACKUP, defaultValues);
    }

    /**
     * 保存 普通模式 悬浮球 第二页 备份 的数据
     * 当子菜单有8个数据的时候，进行数据备份
     * 设置这个备份的原因是方便用户在 编辑界面 快速添加新的子菜单
     *
     * @param values
     */
    public static void setNormalData2Backup(String values) {
        setString(NOW_KEY_ITEM_ACTION_PAGE_2_BACKUP, values);
    }

    /**
     * 获取 迷你模式 悬浮球 子菜单 的个数
     *
     * @param defaultValues
     * @return
     */
    public static int getMiniMenuItemCount(int defaultValues) {
        return getInt(NOW_KEY_MINI_PANEL_MENU_COUNT, defaultValues);
    }

    /**
     * 设置 迷你模式 子菜单的数量
     *
     * @param values
     */
    public static void setMiniMenuItemCount(int values) {
        setInt(NOW_KEY_MINI_PANEL_MENU_COUNT, values);
    }

    /**
     * 获取 普通模式 第一页 子菜单的数量
     *
     * @param defaultValues
     * @return
     */
    public static int getNormalMenuItemCount1(int defaultValues) {
        return getInt(NOW_KEY_NORMAL_PANEL_MENU_COUNT1, defaultValues);
    }

    /**
     * 设置 普通模式 第一页 子菜单的数量
     *
     * @param values
     */
    public static void setNormalMenuItemCount1(int values) {
        setInt(NOW_KEY_NORMAL_PANEL_MENU_COUNT1, values);
    }

    /**
     * 获取 普通模式 第二页 子菜单的数量
     *
     * @param defaultValues
     * @return
     */
    public static int getNormalMenuItemCount2(int defaultValues) {
        return getInt(NOW_KEY_NORMAL_PANEL_MENU_COUNT2, defaultValues);
    }

    /**
     * 设置 普通模式 第二页 子菜单的数量
     *
     * @param values
     */
    public static void setNormalMenuItemCount2(int values) {
        setInt(NOW_KEY_NORMAL_PANEL_MENU_COUNT2, values);
    }

    /**
     * 设置 普通模式 悬浮求 双击事件
     *
     * @param values
     */
    public static void setNormalGestureDoubleClick(String values) {
        setString(NORMAL_GESTURE_DOUBLE_CLICK, values);
    }

    /**
     * 读取 普通模式 悬浮求 双击事件
     *
     * @param defaultValues
     */
    public static String getNormalGestureDoubleClick(String defaultValues) {
        return getString(NORMAL_GESTURE_DOUBLE_CLICK, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 双击事件 Action
     *
     * @param values
     */
    public static void setNormalGestureDoubleClickAction(String values) {
        setString(NORMAL_GESTURE_DOUBLE_CLICK_ACTION, values);
    }

    /**
     * 读取 普通模式 悬浮求 双击事件 Action
     *
     * @param defaultValues
     */
    public static String getNormalGestureDoubleClickAction(String defaultValues) {
        return getString(NORMAL_GESTURE_DOUBLE_CLICK_ACTION, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 双击事件 TYPE
     *
     * @param values
     */
    public static void setNormalGestureDoubleClickType(int values) {
        setInt(NORMAL_GESTURE_DOUBLE_CLICK_TYPE, values);
    }

    /**
     * 读取 普通模式 悬浮求 双击事件 TYPE
     *
     * @param defaultValues
     */
    public static int getNormalGestureDoubleClickType(int defaultValues) {
        return getInt(NORMAL_GESTURE_DOUBLE_CLICK_TYPE, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 长按事件
     *
     * @param values
     */
    public static void setNormalGestureLongPress(String values) {
        setString(NORMAL_GESTURE_LONG_PRESS, values);
    }

    /**
     * 读取 普通模式 悬浮求 长按事件
     *
     * @param defaultValues
     */
    public static String getNormalGestureLongPress(String defaultValues) {
        return getString(NORMAL_GESTURE_LONG_PRESS, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 长按事件 Action
     *
     * @param values
     */
    public static void setNormalGestureLongPressAction(String values) {
        setString(NORMAL_GESTURE_LONG_PRESS_ACTION, values);
    }

    /**
     * 读取 普通模式 悬浮求 长按事件 Action
     *
     * @param defaultValues
     */
    public static String getNormalGestureLongPressAction(String defaultValues) {
        return getString(NORMAL_GESTURE_LONG_PRESS_ACTION, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 长按事件 TYPE
     *
     * @param values
     */
    public static void setNormalGestureLongPressType(int values) {
        setInt(NORMAL_GESTURE_LONG_PRESS_TYPE, values);
    }

    /**
     * 读取 普通模式 悬浮求 长按事件 TYPE
     *
     * @param defaultValues
     */
    public static int getNormalGestureLongPressType(int defaultValues) {
        return getInt(NORMAL_GESTURE_LONG_PRESS_TYPE, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 短拉事件
     *
     * @param values
     */
    public static void setNormalGestureShortDrag(String values) {
        setString(NORMAL_GESTURE_SHORT_DRAG, values);
    }

    /**
     * 读取 普通模式 悬浮求 短拉事件
     *
     * @param defaultValues
     */
    public static String getNormalGestureShortDrag(String defaultValues) {
        return getString(NORMAL_GESTURE_SHORT_DRAG, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 短拉事件
     *
     * @param values
     */
    public static void setNormalGestureShortDragAction(String values) {
        setString(NORMAL_GESTURE_SHORT_DRAG_ACTION, values);
    }

    /**
     * 读取 普通模式 悬浮求 短拉事件
     *
     * @param defaultValues
     */
    public static String getNormalGestureShortDragAction(String defaultValues) {
        return getString(NORMAL_GESTURE_SHORT_DRAG_ACTION, defaultValues);
    }

    /**
     * 设置 普通模式 悬浮求 短拉事件 TYPE
     *
     * @param values
     */
    public static void setNormalGestureShortDragType(int values) {
        setInt(NORMAL_GESTURE_SHORT_DRAG_TYPE, values);
    }

    /**
     * 读取 普通模式 悬浮求 短拉事件 TYPE
     *
     * @param defaultValues
     */
    public static int getNormalGestureShortDragType(int defaultValues) {
        return getInt(NORMAL_GESTURE_SHORT_DRAG_TYPE, defaultValues);
    }


    // =============


    /**
     * 设置 迷你模式 悬浮求 双击事件
     *
     * @param values
     */
    public static void setMiniGestureDoubleClick(String values) {
        setString(MINI_GESTURE_DOUBLE_CLICK, values);
    }

    /**
     * 读取 迷你模式 悬浮求 双击事件
     *
     * @param defaultValues
     */
    public static String getMiniGestureDoubleClick(String defaultValues) {
        return getString(MINI_GESTURE_DOUBLE_CLICK, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 双击事件 Action
     *
     * @param values
     */
    public static void setMiniGestureDoubleClickAction(String values) {
        setString(MINI_GESTURE_DOUBLE_CLICK_ACTION, values);
    }

    /**
     * 读取 迷你模式 悬浮求 双击事件 Action
     *
     * @param defaultValues
     */
    public static String getMiniGestureDoubleClickAction(String defaultValues) {
        return getString(MINI_GESTURE_DOUBLE_CLICK_ACTION, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 双击事件 TYPE
     *
     * @param values
     */
    public static void setMiniGestureDoubleClickType(int values) {
        setInt(MINI_GESTURE_DOUBLE_CLICK_TYPE, values);
    }

    /**
     * 读取 迷你模式 悬浮求 双击事件 TYPE
     *
     * @param defaultValues
     */
    public static int getMiniGestureDoubleClickType(int defaultValues) {
        return getInt(MINI_GESTURE_DOUBLE_CLICK_TYPE, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 长按事件
     *
     * @param values
     */
    public static void setMiniGestureLongPress(String values) {
        setString(MINI_GESTURE_LONG_PRESS, values);
    }

    /**
     * 读取 迷你模式 悬浮求 长按事件
     *
     * @param defaultValues
     */
    public static String getMiniGestureLongPress(String defaultValues) {
        return getString(MINI_GESTURE_LONG_PRESS, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 长按事件 Action
     *
     * @param values
     */
    public static void setMiniGestureLongPressAction(String values) {
        setString(MINI_GESTURE_LONG_PRESS_ACTION, values);
    }

    /**
     * 读取 迷你模式 悬浮求 长按事件 Action
     *
     * @param defaultValues
     */
    public static String getMiniGestureLongPressAction(String defaultValues) {
        return getString(MINI_GESTURE_LONG_PRESS_ACTION, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 长按事件 TYPE
     *
     * @param values
     */
    public static void setMiniGestureLongPressType(int values) {
        setInt(MINI_GESTURE_LONG_PRESS_TYPE, values);
    }

    /**
     * 读取 迷你模式 悬浮求 长按事件 TYPE
     *
     * @param defaultValues
     */
    public static int getMiniGestureLongPressType(int defaultValues) {
        return getInt(MINI_GESTURE_LONG_PRESS_TYPE, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 短拉事件
     *
     * @param values
     */
    public static void setMiniGestureShortDrag(String values) {
        setString(MINI_GESTURE_SHORT_DRAG, values);
    }

    /**
     * 读取 迷你模式 悬浮求 短拉事件
     *
     * @param defaultValues
     */
    public static String getMiniGestureShortDrag(String defaultValues) {
        return getString(MINI_GESTURE_SHORT_DRAG, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 短拉事件
     *
     * @param values
     */
    public static void setMiniGestureShortDragAction(String values) {
        setString(MINI_GESTURE_SHORT_DRAG_ACTION, values);
    }

    /**
     * 读取 迷你模式 悬浮求 短拉事件
     *
     * @param defaultValues
     */
    public static String getMiniGestureShortDragAction(String defaultValues) {
        return getString(MINI_GESTURE_SHORT_DRAG_ACTION, defaultValues);
    }

    /**
     * 设置 迷你模式 悬浮求 短拉事件 TYPE
     *
     * @param values
     */
    public static void setMiniGestureShortDragType(int values) {
        setInt(MINI_GESTURE_SHORT_DRAG_TYPE, values);
    }

    /**
     * 读取 迷你模式 悬浮求 短拉事件 TYPE
     *
     * @param defaultValues
     */
    public static int getMiniGestureShortDragType(int defaultValues) {
        return getInt(MINI_GESTURE_SHORT_DRAG_TYPE, defaultValues);
    }


    // 设置普通模式 联系人 点击事件
    public static void setNormalGestureDoubleClickCall(String values) {
        setString(NORMAL_CALL_A_CONTACT_DOUBLE_CLICK, values);
    }

    public static String getNormalGestureDoubleClickCall(String defaultValues) {
        return getString(NORMAL_CALL_A_CONTACT_DOUBLE_CLICK, defaultValues);
    }

    public static void setNormalGestureLongPressCall(String values) {
        setString(NORMAL_CALL_A_CONTACT_LONG_PRESS, values);
    }

    public static String getNormalGestureLongPressCall(String defaultValues) {
        return getString(NORMAL_CALL_A_CONTACT_LONG_PRESS, defaultValues);
    }

    public static void setNormalGestureShortDragCall(String values) {
        setString(NORMAL_CALL_A_CONTACT_SHORT_DRAG, values);
    }

    public static String getNormalGestureShortDragCall(String defaultValues) {
        return getString(NORMAL_CALL_A_CONTACT_SHORT_DRAG, defaultValues);
    }


    // 设置迷你模式 联系人 点击事件
    public static void setMiniGestureDoubleClickCall(String values) {
        setString(MINI_CALL_A_CONTACT_DOUBLE_CLICK, values);
    }

    public static String getMiniGestureDoubleClickCall(String defaultValues) {
        return getString(MINI_CALL_A_CONTACT_DOUBLE_CLICK, defaultValues);
    }

    public static void setMiniGestureLongPressCall(String values) {
        setString(MINI_CALL_A_CONTACT_LONG_PRESS, values);
    }

    public static String getMiniGestureLongPressCall(String defaultValues) {
        return getString(MINI_CALL_A_CONTACT_LONG_PRESS, defaultValues);
    }

    public static void setMiniGestureShortDragCall(String values) {
        setString(MINI_CALL_A_CONTACT_SHORT_DRAG, values);
    }

    public static String getMiniGestureShortDragCall(String defaultValues) {
        return getString(MINI_CALL_A_CONTACT_SHORT_DRAG, defaultValues);
    }

    public static boolean isFirstTimeUseNowKey(Context ctx) {
        return getBoolean(IS_FIRST_TIME_USE_NOW_KEY, true);
    }

    public static void setIsFirstTimeUseNowKey(boolean values) {
        setBoolean(IS_FIRST_TIME_USE_NOW_KEY, values);
    }

    public static void setNowKeyTheme(int values) {
        setInt(NOW_KEY_THEME, values);
    }

    public static int getNowKeyTheme(int defaultValues) {
        return getInt(NOW_KEY_THEME, defaultValues);
    }

    public static void setNowKeyThemeIndex(int values) {
        setInt(NOW_KEY_THEME_INDEX, values);
    }

    public static int getNowKeyThemeIndex(int defaultValues) {
        return getInt(NOW_KEY_THEME_INDEX, defaultValues);
    }

    public static void setIsNeedShortDragIntroduce(boolean values) {
        setBoolean(IS_NEED_SHOW_SHORT_DRAG_INTRODUCE, values);
    }

    public static boolean isNeedShowShortDragIntroduce(boolean defaultValues) {
        return getBoolean(IS_NEED_SHOW_SHORT_DRAG_INTRODUCE, defaultValues);
    }

    /**
     * 设置 普通模式的悬浮球是否靠边
     * @param values
     */
    public static void setIsNormalBallBorder(boolean values) {
        setBoolean(NOW_KEY_BORDER_OPTION, values);
    }

    /**
     * 判断普通模式的悬浮球是否靠边
     * @param defaultValues
     * @return
     */
    public static boolean isNormalBallBorder(boolean defaultValues) {
        return getBoolean(NOW_KEY_BORDER_OPTION, defaultValues);
    }
    /**
     * 设置 悬浮球的大小
     * @param values
     */
    public static void setFloatBallViewSize(float values) {
        setFloat(NOW_KEY_FLOAT_VIEW_SIZE, values);
    }

    /**
     * 获得悬浮求的大小
     * @param defaultValues
     * @return
     */
    public static float getFloatBallViewSize(float defaultValues) {
        return getFloat(NOW_KEY_FLOAT_VIEW_SIZE, defaultValues);
    }

    /**
     * 设置 悬浮球的透明度
     * @param values
     */
    public static void setFloatBallViewOpacity(float values) {
        setFloat(NOW_KEY_FLOAT_VIEW_OPACITY, values);
    }

    /**
     * 获得悬浮球的透明度
     * @param defaultValues
     * @return
     */
    public static float getFloatBallViewOpacity(float defaultValues) {
        return getFloat(NOW_KEY_FLOAT_VIEW_OPACITY, defaultValues);
    }
    /**
     * 设置 Normal模式 拨打电话的字符串
     * @param values
     */
    public static void setCallContactItem(String values) {
        setString(NOW_KEY_CALL_A_CONTACT_ITEM, values);
    }

    /**
     * 获得 Normal模式 拨打电话的字符串
     * @param defaultValues
     * @return
     */
    public static String getCallContactItem(String defaultValues) {
        return getString(NOW_KEY_CALL_A_CONTACT_ITEM, defaultValues);
    }
    /**
     * 设置 Mini模式 拨打电话的字符串
     * @param values
     */
    public static void setCallContactItemMini(String values) {
        setString(NOW_KEY_CALL_A_CONTACT_ITEM_MINI, values);
    }

    /**
     * 获得 Mini模式 拨打电话的字符串
     * @param defaultValues
     * @return
     */
    public static String getCallContactItemMini(String defaultValues) {
        return getString(NOW_KEY_CALL_A_CONTACT_ITEM_MINI, defaultValues);
    }
    /**
     * 设置 mini 悬浮球 的 x坐标
     * @param values
     */
    public static void setMiniFloatBallX(int values) {
        setInt(MINI_FLOAT_BALL_X, values);
    }

    /**
     * 获得 mini 悬浮球的 y坐标
     * @param defaultValues
     * @return
     */
    public static int getMiniFloatBallX(int defaultValues) {
        return getInt(MINI_FLOAT_BALL_X, defaultValues);
    }
    /**
     * 设置 mini 悬浮球 的 y坐标
     * @param values
     */
    public static void setMiniFloatBallY(int values) {
        setInt(MINI_FLOAT_BALL_Y, values);
    }

    /**
     * 获得 mini 悬浮球的 y坐标
     * @param defaultValues
     * @return
     */
    public static int getMiniFloatBallY(int defaultValues) {
        return getInt(MINI_FLOAT_BALL_Y, defaultValues);
    }
    /**
     * 设置 normal 悬浮球 的 x坐标
     * @param values
     */
    public static void setNormalFloatBallX(int values) {
        setInt(NORMAL_FLOAT_BALL_X, values);
    }

    /**
     * 获得 normal 悬浮球的 x坐标
     * @param defaultValues
     * @return
     */
    public static int getNormalFloatBallX(int defaultValues) {
        return getInt(NORMAL_FLOAT_BALL_X, defaultValues);
    }

    /**
     * 设置 normal 悬浮球 的 y坐标
     * @param values
     */
    public static void setNormalFloatBallY(int values) {
        setInt(NORMAL_FLOAT_BALL_Y, values);
    }

    /**
     * 获得 normal 悬浮球的 y坐标
     * @param defaultValues
     * @return
     */
    public static int getNormalFloatBallY(int defaultValues) {
        return getInt(NORMAL_FLOAT_BALL_Y, defaultValues);
    }
}
