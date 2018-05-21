package com.kuding.nowkey.interfaces;

/**
 * 子菜单被点击的处理
 */
public interface OnMenuItemClickListener {
    void itemClick();

    boolean showWarning();

    void panelClick();

    void addItemClick();

    void startTouching();

    void finishTouching();

    void onItemLongClick();
}