package com.kuding.superball.floatview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.kuding.superball.info.BaseItemInfo;

/**
 * Created by user on 17-1-12.
 */

public class FloatItemView extends RelativeLayout {

    private BaseItemInfo info;
    private int index;

    public FloatItemView(Context context) {
        this(context, null);
    }

    public FloatItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public BaseItemInfo getInfo() {
        return info;
    }

    public void setInfo(BaseItemInfo info) {
        this.info = info;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
