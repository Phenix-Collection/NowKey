package com.kuding.superball.applist;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import com.kuding.superball.R;

public class ClearEditText extends EditText implements
        View.OnFocusChangeListener, TextWatcher {

    /**
     * 定义删除按钮的引用
     */
    private Drawable mClearDrawable;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        //这个构造方法很重要，不加这个很多属性不能再XML中定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {

        //获取EidtText的DrawableRight，加入没有设置我们就使用自己的默认图片，getCompoundDrawables返回的是四个方向的图片信息
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources()
                    .getDrawable(R.drawable.ic_clear_search_api_holo_light);
        }
        //设置图片的宽高，前两个是组件左上角在容器中的坐标 后两个是组件的宽度和高度
        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        //默认初始化情况清空当前的图片信息
        setClearIconVisible(false);
        //类似于文本框里面hint文字在初始化的时候显示或者隐藏的操作，就要用到setOnFocusChangeListener，同时监听有字符出现时就显示删除按钮
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }


    /**
     * 判断是否有删除按钮
     * 设置删除按钮的点击事件：制空
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean touchable = event.getX() > (getWidth()
                        - getPaddingRight() - mClearDrawable.getIntrinsicWidth())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 当焦点发生变化的情况设置删除按钮
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * 设置删除按钮的显示或隐藏场景
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    /**
     * 判断是否有字符，如果有存在字符添加删除按钮
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count,
                              int after) {
        setClearIconVisible(s.length() > 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    public void setShakeAnimation() {
        this.setAnimation(shakeAnimation(5));
    }


    public static Animation shakeAnimation(int counts) {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }


}