package com.kuding.superball.interfaces;

/**
 * 悬浮按钮点击 监听 接口
 */
public interface OnFloatIconClickListener {
    void onFloatIconClick();

    void onFloatIconPress();

    void onFloatIconShortDrag();

    void onFloatIconDoubleClick();

    void onFloatOutsideClick();

    void onConfirmDialogClick();
}