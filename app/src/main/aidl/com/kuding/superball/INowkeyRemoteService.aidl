// INowkeyRemoteService.aidl
package com.kuding.superball;

// Declare any non-default types here with import statements

interface INowkeyRemoteService {
    // 外部程序隐藏Nowkey悬浮球的接口
    void hideNowkeyFloatBall();

    // 外部应用显示恢复Nowkey悬浮球的接口 和 hideNowkeyFloatBall 对应使用
    void showNowkeyFloatBall();

    // 外部应用获取Nowkey悬浮球的位置
    Rect getFloatBallRect();
}
