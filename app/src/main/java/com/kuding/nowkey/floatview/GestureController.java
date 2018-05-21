package com.kuding.nowkey.floatview;

import android.content.Context;

import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.FunctionUtils;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.info.UserPreference;

import java.util.ArrayList;

/**
 * Created by user on 17-1-12.
 */

public class GestureController {
    private static final String TAG = "GestureController";

    private static GestureController sInstance;

    private Context mContext;

    private String[] mMiniDefaultGesture = {"home", "recent", "back"};     // 迷你悬浮球的默认手势 默认长按为Home，双击为recent，短拉为back（如有）
    private String[] mNormalDefaultGesture = {"home", "recent", "back"};   // 普通悬浮球的默认手势 默认长按为Home，双击为recent，短拉为back（如有）


    private GestureController(Context context) {
        mContext = context;
    }

    public static final GestureController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GestureController(context);
        }
        return sInstance;
    }

    /**
     * 重置所有手势
     */
    public boolean resetGesture() {
        return (resetMiniModeDefaultGesture() && resetNormalModeDefaultGesture());
    }

    /**
     * 备份所有手势
     *
     * @param preference
     * @return
     */
    public UserPreference backUpAllGestrue(UserPreference preference) {
        if (preference == null) return null;
        preference = backUpMiniGestrue(preference);
        preference = backUpNormalGestrue(preference);
        return preference;
    }

    /**
     * 恢复所有手势
     *
     * @param preference
     */
    public void restorepAllGestrue(UserPreference preference) {
        if (preference == null) return;
        restoreMiniGestrue(preference);
        restoreNormalGestrue(preference);
    }

    /**
     * 恢复 普通模式 的手势
     *
     * @param preference
     */
    public void restoreNormalGestrue(UserPreference preference) {
        PreferenceUtils.setNormalGestureDoubleClick(preference.getNormalDoubleClick());
        PreferenceUtils.setNormalGestureDoubleClickType(preference.getNormalDoubleClickType());
        PreferenceUtils.setNormalGestureDoubleClickAction(preference.getNormalDoubleClickAction());

        PreferenceUtils.setNormalGestureLongPress(preference.getNormalLongPress());
        PreferenceUtils.setNormalGestureLongPressType(preference.getNormalLongPressType());
        PreferenceUtils.setNormalGestureLongPressAction(preference.getNormalLongPressAction());

        PreferenceUtils.setNormalGestureShortDrag(preference.getNormalShortDrag());
        PreferenceUtils.setNormalGestureShortDragType(preference.getNormalShortDragType());
        PreferenceUtils.setNormalGestureShortDragAction(preference.getNormalShortDragAction());
    }

    /**
     * 恢复 迷你模式的手势
     *
     * @param preference
     */
    public void restoreMiniGestrue(UserPreference preference) {
        PreferenceUtils.setMiniGestureDoubleClick(preference.getMiniDoubleClick());
        PreferenceUtils.setMiniGestureDoubleClickType(preference.getMiniDoubleClickType());
        PreferenceUtils.setMiniGestureDoubleClickAction(preference.getMiniDoubleClickAction());

        PreferenceUtils.setMiniGestureLongPress(preference.getMiniLongPress());
        PreferenceUtils.setMiniGestureLongPressType(preference.getMiniLongPressType());
        PreferenceUtils.setMiniGestureLongPressAction(preference.getMiniLongPressAction());

        PreferenceUtils.setMiniGestureShortDrag(preference.getMiniShortDrag());
        PreferenceUtils.setMiniGestureShortDragType(preference.getMiniShortDragType());
        PreferenceUtils.setMiniGestureShortDragAction(preference.getMiniShortDragAction());
    }

    /**
     * 备份 普通模式的手势
     *
     * @param preference
     * @return
     */
    public UserPreference backUpNormalGestrue(UserPreference preference) {
        preference.setNormalDoubleClick(PreferenceUtils.getNormalGestureDoubleClick(""));
        preference.setNormalDoubleClickType(PreferenceUtils.getNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION));
        preference.setNormalDoubleClickAction(PreferenceUtils.getNormalGestureDoubleClickAction(""));

        preference.setNormalLongPress(PreferenceUtils.getNormalGestureLongPress(""));
        preference.setNormalLongPressType(PreferenceUtils.getNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION));
        preference.setNormalLongPressAction(PreferenceUtils.getNormalGestureLongPressAction(""));

        preference.setNormalShortDrag(PreferenceUtils.getNormalGestureShortDrag(""));
        preference.setNormalShortDragType(PreferenceUtils.getNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION));
        preference.setNormalShortDragAction(PreferenceUtils.getNormalGestureShortDragAction(""));
        return preference;
    }

    /**
     * 备份迷你模式的手势
     *
     * @param preference
     * @return
     */
    public UserPreference backUpMiniGestrue(UserPreference preference) {
        preference.setMiniDoubleClick(PreferenceUtils.getMiniGestureDoubleClick(""));
        preference.setMiniDoubleClickType(PreferenceUtils.getMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION));
        preference.setMiniDoubleClickAction(PreferenceUtils.getMiniGestureDoubleClickAction(""));

        preference.setMiniLongPress(PreferenceUtils.getMiniGestureLongPress(""));
        preference.setMiniLongPressType(PreferenceUtils.getMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION));
        preference.setMiniLongPressAction(PreferenceUtils.getMiniGestureLongPressAction(""));

        preference.setMiniShortDrag(PreferenceUtils.getMiniGestureShortDrag(""));
        preference.setMiniShortDragType(PreferenceUtils.getMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION));
        preference.setMiniShortDragAction(PreferenceUtils.getMiniGestureShortDragAction(""));
        return preference;
    }


    /**
     * 重置Mini模式的手势
     */
    public boolean resetMiniModeDefaultGesture() {
        boolean result = false;
        ArrayList<FunctionItemInfo> functionList = new ArrayList<FunctionItemInfo>();
        try {
            for (String gesture : mMiniDefaultGesture) {
                functionList.add(FunctionUtils.functionFilter(mContext, gesture));
            }
            if (functionList != null && functionList.size() == 3) {
                FunctionItemInfo longPressFunction = functionList.get(0);
                PreferenceUtils.setMiniGestureLongPress(longPressFunction.getText());
                PreferenceUtils.setMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setMiniGestureLongPressAction(mMiniDefaultGesture[0]);

                FunctionItemInfo doubleClickFunction = functionList.get(1);
                PreferenceUtils.setMiniGestureDoubleClick(doubleClickFunction.getText());
                PreferenceUtils.setMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setMiniGestureDoubleClickAction(mMiniDefaultGesture[1]);

                FunctionItemInfo ShortDragFunction = functionList.get(2);
                PreferenceUtils.setMiniGestureShortDrag(ShortDragFunction.getText());
                PreferenceUtils.setMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setMiniGestureShortDragAction(mMiniDefaultGesture[2]);
                result = true;
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 清除Mini模式的手势
     */
    public void cleanMiniModeGesture(int gesture) {
        switch (gesture) {
            case Constant.GESTURE_DOUBLE_CLICK:
                PreferenceUtils.setMiniGestureDoubleClick("");
                PreferenceUtils.setMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                PreferenceUtils.setMiniGestureDoubleClickAction("");
                break;
            case Constant.GESTURE_LONG_CLICK:
                PreferenceUtils.setMiniGestureLongPress("");
                PreferenceUtils.setMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                PreferenceUtils.setMiniGestureLongPressAction("");
                break;
            case Constant.GESTURE_SHORT_DRAG:
                PreferenceUtils.setMiniGestureShortDrag("");
                PreferenceUtils.setMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                PreferenceUtils.setMiniGestureShortDragAction("");
                break;
        }
    }

    /**
     * 清除Normal模式的手势
     */
    public void cleanNormalModeGesture(int gesture) {
        switch (gesture) {
            case Constant.GESTURE_DOUBLE_CLICK:
                PreferenceUtils.setNormalGestureDoubleClick("");
                PreferenceUtils.setNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                PreferenceUtils.setNormalGestureDoubleClickAction("");
                break;
            case Constant.GESTURE_LONG_CLICK:
                PreferenceUtils.setNormalGestureLongPress("");
                PreferenceUtils.setNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                PreferenceUtils.setNormalGestureLongPressAction("");
                break;
            case Constant.GESTURE_SHORT_DRAG:
                PreferenceUtils.setNormalGestureShortDrag("");
                PreferenceUtils.setNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_NONE);
                PreferenceUtils.setNormalGestureShortDragAction("");
                break;
        }
    }

    /**
     * 重置Normal模式的手势
     */
    public boolean resetNormalModeDefaultGesture() {
        boolean result = false;
        ArrayList<FunctionItemInfo> functionList = new ArrayList<FunctionItemInfo>();
        try {
            for (String gesture : mNormalDefaultGesture) {
                functionList.add(FunctionUtils.functionFilter(mContext, gesture));
            }
            if (functionList != null && functionList.size() == 3) {
                FunctionItemInfo longPressFunction = functionList.get(0);
                PreferenceUtils.setNormalGestureLongPress(longPressFunction.getText());
                PreferenceUtils.setNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setNormalGestureLongPressAction(mNormalDefaultGesture[0]);

                FunctionItemInfo doubleClickFunction = functionList.get(1);
                PreferenceUtils.setNormalGestureDoubleClick(doubleClickFunction.getText());
                PreferenceUtils.setNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setNormalGestureDoubleClickAction(mNormalDefaultGesture[1]);

                FunctionItemInfo ShortDragFunction = functionList.get(2);
                PreferenceUtils.setNormalGestureShortDrag(ShortDragFunction.getText());
                PreferenceUtils.setNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setNormalGestureShortDragAction(mNormalDefaultGesture[2]);
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
}
