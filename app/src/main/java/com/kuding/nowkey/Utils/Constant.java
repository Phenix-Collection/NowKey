package com.kuding.nowkey.Utils;

/**
 * Created by user on 17-1-10.
 */

public class Constant {
    public static final String OPERATE_BEHAVIOR = "OPERATE_BEHAVIOR";//标识用户的操作行为 比如是 选择快捷事件 还是 手势之类的事件

    public static final String NOW_KEY_ITEM_INDEX = "now_key_item_index";
    public static final String NOW_KEY_ITEM_PAGE = "now_key_item_page";
    public static final String NOW_KEY_ADD_EXTERNAL = "now_key_add_external";
    public static final String NOW_KEY_KEY_WORD = "now_key_key_word";

    public static final String DEFAULT_THEME = "#000000";

    public static final String BROADCAST_THEME_CHANGE = "action.broadcast.theme.change";

    public static final int GESTURE_DOUBLE_CLICK = 1;
    public static final int GESTURE_LONG_CLICK = 2;
    public static final int GESTURE_SHORT_DRAG = 7;         // 短拉
    public static final int NOW_KEY_ACTION = 3;
    public static final int GET_APP_REQUEST = 4;
    public static final int GET_APP_RESULT_OK = 5;
    public static final int GET_APP_RESULT_CANCEL = 6;


    public static final int GET_CONTACT_REQUEST = 7;

    public static final int NOW_KEY_ITEM_TYPE_NONE = 0;
    public static final int NOW_KEY_ITEM_TYPE_APP = 1;
    public static final int NOW_KEY_ITEM_TYPE_FUNCTION = 2;

    // 更新子菜单的操作
    public static final String NOW_KEY_MENU_OPERATE = "now_key_menu_operate";
    public static final int NOW_KEY_MENU_OPERATE_UPDATE = 0;
    public static final int NOW_KEY_MENU_OPERATE_ADD = 1;
    public static final int NOW_KEY_MENU_OPERATE_DELETE = 2;

    public static final String[] FUNCTION_APP_LIST = {
            "apps"
    };

    public static final String[] OPERATION_LIST = {
            "home", "recent"
    };

    public static final String[] FUNCTION_LIST = {
            "bluetooth", "network", "wifi",
            "rotation", "gps", "flightmode",
            "settings", "sound", "lock",
            "notification", "display", "sync",
            "screenshot"
    };

    public static final String[] TOOL_LIST = {
            "alarm", "nsettings",
            "booster", "colorcatcher", "alexa"
    };

    public static final String[] PHONE_LIST = {
            "call", "chat", "recentcontact",
            "contact"
    };

    public static final String[] ALCATEL_LIST = {
            "callacontact", "recentcontact", "micorvideo",
            "camera", "light", "soundrecording",
            "timer", "calculator", "navigatehome",
            "gallery", "musicplaylist", "chat",
            "email", "addacontact", "event",
            "googlevoice"
//            "calculator", "calendar"
//            "selfie", "micorvideo", "googlevoice",
//            "event", "email", "musicplaylist","recognisesongs",
//            "navigatehome", "timer", "soundrecording", "light","camera"
    };

    public static final String[] DEFAULT_EXTRA_LIST = {
            "back", "recent", "home",
            "light", "bluetooth", "settings",
            "wifi", "gps", "rotation",
            "flightmode", "network", "addacontact",
            "splitscreen", "event", "lock",
            "gallery"
    };

    public static final int[] ALL_INDEX_LIST = {
            0, 1, 2, 3, 4, 5, 6, 7
    };
}
