package com.kuding.superball.info;

/**
 * 用于数据备份的实体类
 */
public class UserPreference {
    // 主题
    private int NowKeyTheme;
    private int NowKeyThemeIndex;

    // 开关
    private boolean isMiniMode;
    private boolean isShowNowKey;
    private boolean isShowIntroduce;

    // mini模式的手势
    private String miniLongPress;
    private int miniLongPressType;
    private String miniLongPressAction;
    private String miniDoubleClick;
    private int miniDoubleClickType;
    private String miniDoubleClickAction;
    private String miniShortDrag;
    private int miniShortDragType;
    private String miniShortDragAction;

    // normal 模式手势
    private String normalLongPress;
    private int normalLongPressType;
    private String normalLongPressAction;
    private String normalDoubleClick;
    private int normalDoubleClickType;
    private String normalDoubleClickAction;
    private String normalShortDrag;
    private int normalShortDragType;
    private String normalShortDragAction;

    // 转盘数据
    private String miniData;
    private String normalData1;
    private String normalData2;

    // 转盘备份数据
    private String miniData_backup;
    private String normalData1_backup;
    private String normalData2_backup;

    // 快捷联系人的数据
    private String miniCallAContact;
    private String normalCallAContact;

    private int miniMenuCount;
    private int data1MenuCount;
    private int data2MenuCount;

    private float mFloatBallSize;
    private float mFloatBallOpacity;

    public int getNowKeyTheme() {
        return NowKeyTheme;
    }

    public void setNowKeyTheme(int nowKeyTheme) {
        NowKeyTheme = nowKeyTheme;
    }

    public int getNowKeyThemeIndex() {
        return NowKeyThemeIndex;
    }

    public void setNowKeyThemeIndex(int nowKeyThemeIndex) {
        NowKeyThemeIndex = nowKeyThemeIndex;
    }

    public String getMiniLongPress() {
        return miniLongPress;
    }

    public void setMiniLongPress(String miniLongPress) {
        this.miniLongPress = miniLongPress;
    }

    public int getMiniLongPressType() {
        return miniLongPressType;
    }

    public void setMiniLongPressType(int miniLongPressType) {
        this.miniLongPressType = miniLongPressType;
    }

    public String getMiniLongPressAction() {
        return miniLongPressAction;
    }

    public void setMiniLongPressAction(String miniLongPressAction) {
        this.miniLongPressAction = miniLongPressAction;
    }

    public String getMiniDoubleClick() {
        return miniDoubleClick;
    }

    public void setMiniDoubleClick(String miniDoubleClick) {
        this.miniDoubleClick = miniDoubleClick;
    }

    public int getMiniDoubleClickType() {
        return miniDoubleClickType;
    }

    public void setMiniDoubleClickType(int miniDoubleClickType) {
        this.miniDoubleClickType = miniDoubleClickType;
    }

    public String getMiniDoubleClickAction() {
        return miniDoubleClickAction;
    }

    public void setMiniDoubleClickAction(String miniDoubleClickAction) {
        this.miniDoubleClickAction = miniDoubleClickAction;
    }

    public String getMiniShortDrag() {
        return miniShortDrag;
    }

    public void setMiniShortDrag(String miniShortDrag) {
        this.miniShortDrag = miniShortDrag;
    }

    public int getMiniShortDragType() {
        return miniShortDragType;
    }

    public void setMiniShortDragType(int miniShortDragType) {
        this.miniShortDragType = miniShortDragType;
    }

    public String getMiniShortDragAction() {
        return miniShortDragAction;
    }

    public void setMiniShortDragAction(String miniShortDragAction) {
        this.miniShortDragAction = miniShortDragAction;
    }

    public String getNormalLongPress() {
        return normalLongPress;
    }

    public void setNormalLongPress(String normalLongPress) {
        this.normalLongPress = normalLongPress;
    }

    public int getNormalLongPressType() {
        return normalLongPressType;
    }

    public void setNormalLongPressType(int normalLongPressType) {
        this.normalLongPressType = normalLongPressType;
    }

    public String getNormalLongPressAction() {
        return normalLongPressAction;
    }

    public void setNormalLongPressAction(String normalLongPressAction) {
        this.normalLongPressAction = normalLongPressAction;
    }

    public String getNormalDoubleClick() {
        return normalDoubleClick;
    }

    public void setNormalDoubleClick(String normalDoubleClick) {
        this.normalDoubleClick = normalDoubleClick;
    }

    public int getNormalDoubleClickType() {
        return normalDoubleClickType;
    }

    public void setNormalDoubleClickType(int normalDoubleClickType) {
        this.normalDoubleClickType = normalDoubleClickType;
    }

    public String getNormalDoubleClickAction() {
        return normalDoubleClickAction;
    }

    public void setNormalDoubleClickAction(String normalDoubleClickAction) {
        this.normalDoubleClickAction = normalDoubleClickAction;
    }

    public String getNormalShortDrag() {
        return normalShortDrag;
    }

    public void setNormalShortDrag(String normalShortDrag) {
        this.normalShortDrag = normalShortDrag;
    }

    public int getNormalShortDragType() {
        return normalShortDragType;
    }

    public void setNormalShortDragType(int normalShortDragType) {
        this.normalShortDragType = normalShortDragType;
    }

    public String getNormalShortDragAction() {
        return normalShortDragAction;
    }

    public void setNormalShortDragAction(String normalShortDragAction) {
        this.normalShortDragAction = normalShortDragAction;
    }

    public String getMiniData() {
        return miniData;
    }

    public void setMiniData(String miniData) {
        this.miniData = miniData;
    }

    public String getNormalData1() {
        return normalData1;
    }

    public void setNormalData1(String normalData1) {
        this.normalData1 = normalData1;
    }

    public String getNormalData2() {
        return normalData2;
    }

    public void setNormalData2(String normalData2) {
        this.normalData2 = normalData2;
    }


    public boolean isMiniMode() {
        return isMiniMode;
    }

    public void setMiniMode(boolean miniMode) {
        isMiniMode = miniMode;
    }

    public boolean isShowNowKey() {
        return isShowNowKey;
    }

    public void setShowNowKey(boolean showNowKey) {
        isShowNowKey = showNowKey;
    }

    public int getMiniMenuCount() {
        return miniMenuCount;
    }

    public void setMiniMenuCount(int miniMenuCount) {
        this.miniMenuCount = miniMenuCount;
    }

    public int getData1MenuCount() {
        return data1MenuCount;
    }

    public void setData1MenuCount(int data1MenuCount) {
        this.data1MenuCount = data1MenuCount;
    }

    public int getData2MenuCount() {
        return data2MenuCount;
    }

    public void setData2MenuCount(int data2MenuCount) {
        this.data2MenuCount = data2MenuCount;
    }

    public float getmFloatBallSize() {
        return mFloatBallSize;
    }

    public void setmFloatBallSize(float mFloatBallSize) {
        this.mFloatBallSize = mFloatBallSize;
    }

    public float getmFloatBallOpacity() {
        return mFloatBallOpacity;
    }

    public void setmFloatBallOpacity(float mFloatBallOpacity) {
        this.mFloatBallOpacity = mFloatBallOpacity;
    }

    public String getMiniData_backup() {
        return miniData_backup;
    }

    public void setMiniData_backup(String miniData_backup) {
        this.miniData_backup = miniData_backup;
    }

    public String getNormalData1_backup() {
        return normalData1_backup;
    }

    public void setNormalData1_backup(String normalData1_backup) {
        this.normalData1_backup = normalData1_backup;
    }

    public String getNormalData2_backup() {
        return normalData2_backup;
    }

    public void setNormalData2_backup(String normalData2_backup) {
        this.normalData2_backup = normalData2_backup;
    }

    public boolean isShowIntroduce() {
        return isShowIntroduce;
    }

    public void setShowIntroduce(boolean showIntroduce) {
        isShowIntroduce = showIntroduce;
    }

    public String getMiniCallAContact() {
        return miniCallAContact;
    }

    public void setMiniCallAContact(String miniCallAContact) {
        this.miniCallAContact = miniCallAContact;
    }

    public String getNormalCallAContact() {
        return normalCallAContact;
    }

    public void setNormalCallAContact(String normalCallAContact) {
        this.normalCallAContact = normalCallAContact;
    }
}
