package com.kuding.superball.floatview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

/**
 * Created by user on 17-3-20.
 */

public class FloatingPanelFrameLayout extends FrameLayout {
    private DispatchKeyEventListener mDistpatchKeyEventListener;

    public FloatingPanelFrameLayout(Context context) {
        this(context, null);
    }

    public FloatingPanelFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingPanelFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDispatchKeyEventListener(DispatchKeyEventListener listener) {
        mDistpatchKeyEventListener = listener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mDistpatchKeyEventListener != null) {
            mDistpatchKeyEventListener.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    public interface DispatchKeyEventListener {
        boolean dispatchKeyEvent(KeyEvent event);
    }
}
