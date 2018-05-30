package com.kuding.superball;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.R;
import com.kuding.superball.floatview.FloatingBallController;
import com.kuding.superball.floatview.MiniBallController;

/**
 * Created by yeah017 on 2017/3/14.
 */

public class DisplaySettingsActivity extends AppCompatActivity {

    private Switch mBorderSwitch;
    private LinearLayout mBorderLayout;
    /**
     * List of entries corresponding the settings being set.
     */
    protected String[] mEntries;

    /**
     * Index of the entry corresponding to initial value of the settings.
     */
    protected int mInitialIndex;

    /**
     * Index of the entry corresponding to current value of the settings.
     */
    protected int mCurrentIndex;
    private float[] mValues;
    private PreviewPagerAdapter mPreviewPagerAdapter;
    private SeekBar seekBar;
    private float currentOpacity = 50f;
    private boolean mIsMiniMode = true;
    private TextView borderTv;
    private LinearLayout mBorderLayoutAll;
    private ImageView mImgBorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsMiniMode = PreferenceUtils.isMiniMode(false);
        setContentView(R.layout.now_key_display_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.now_key_option_display_title);
        }

        mImgBorder = (ImageView) this.findViewById(R.id.img_broder);
        mBorderSwitch = (Switch) findViewById(R.id.now_key_border_switcher);
        mBorderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //add by yangzhong.gong for defect-4433958 begin
                if (mIsMiniMode) {

                } else {
                    PreferenceUtils.setIsNormalBallBorder(b);
                    FloatingBallController.getController(getApplication()).setBorderOptions();
                }
                //add by yangzhong.gong for defect-4433958 end
            }
        });
        mBorderLayout = (LinearLayout) findViewById(R.id.now_key_border_layout);
        mBorderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBorderSwitch != null) {
                    boolean checked = mBorderSwitch.isChecked();
                    mBorderSwitch.setChecked(!checked);
                }
            }
        });
        initView();
        borderTv = (TextView)findViewById(R.id.now_key_border_textview);
        mBorderLayoutAll = (LinearLayout) findViewById(R.id.now_key_border_all_layout);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(60);

        currentOpacity = PreferenceUtils.getFloat(PreferenceUtils.NOW_KEY_FLOAT_VIEW_OPACITY, 50);
        //设置当前值为进度条当前值
        seekBar.setProgress((int) currentOpacity - 20);
        //seekbar设置监听
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress  = progress +20;
                PreferenceUtils.setFloat(PreferenceUtils.NOW_KEY_FLOAT_VIEW_OPACITY, (float) progress);

                if (mIsMiniMode) {
                    MiniBallController.getController(getApplication()).updateFloatBallAlpha((float) progress / 100);
                } else {
                    FloatingBallController.getController(getApplication()).updateFloatBallAlpha((float) progress / 100);
                }
            }

            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(DisplaySettingsActivity.this,"按住seekbar",Toast.LENGTH_SHORT).show();
            }

            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(DisplaySettingsActivity.this,"放开seekbar",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        Resources res = getResources();
        // Mark the appropriate item in the preferences list.
        mEntries = res.getStringArray(R.array.entries_floatview_size);
        final String[] strEntryValues = res.getStringArray(R.array.entryvalues_floatview_size);
        final float currentScale = PreferenceUtils.getFloatBallViewSize(50f);
        mInitialIndex = floatSizeValueToIndex(currentScale, strEntryValues);
        mValues = new float[strEntryValues.length];
        for (int i = 0; i < strEntryValues.length; ++i) {
            mValues[i] = Float.parseFloat(strEntryValues[i]);
        }

        //mLabel = (TextView) findViewById(R.id.current_label);

        // The maximum SeekBar value always needs to be non-zero. If there's
        // only one available value, we'll handle this by disabling the
        // seek bar.
        final int max = Math.max(1, mEntries.length - 1);

        final LabeledSeekBar seekBar = (LabeledSeekBar) findViewById(R.id.seek_bar);
        seekBar.setLabels(mEntries);
        seekBar.setMax(max);
        seekBar.setProgress(mInitialIndex);
        seekBar.setOnSeekBarChangeListener(new onPreviewSeekBarChangeListener());

        if (mEntries.length == 1) {
            // The larger and smaller buttons will be disabled when we call
            // setPreviewLayer() later in this method.
            seekBar.setEnabled(false);
        }
    }

    /**
     * Utility function that returns the index in a string array with which the represented value is
     * the closest to a given float value.
     */
    public static int floatSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal - lastVal) * .5f)) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }

    /**
     * Persists the selected font size.
     */
    protected void commit() {
        PreferenceUtils.setFloatBallViewSize(mValues[mCurrentIndex]);
        if (mIsMiniMode) {
            MiniBallController.getController(getApplication()).updateViewSize((int) mValues[mCurrentIndex]);
        } else {
            FloatingBallController.getController(getApplication()).updateViewSize((int) mValues[mCurrentIndex]);
        }
    }

    private void setPreviewLayer(int index, boolean animate) {
        mCurrentIndex = index;
    }

    private class onPreviewSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private boolean mSeekByTouch;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setPreviewLayer(progress, true);
            if (!mSeekByTouch) {
                commit();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mSeekByTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPreviewPagerAdapter != null && mPreviewPagerAdapter.isAnimating()) {
                mPreviewPagerAdapter.setAnimationEndAction(new Runnable() {
                    @Override
                    public void run() {
                        commit();
                    }
                });
            } else {
                commit();
            }
            mSeekByTouch = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mIsMiniMode) {
            //mBorderLayout.setVisibility(View.GONE);

            mBorderLayoutAll.setBackgroundColor(Color.parseColor("#EDEDED"));
            mBorderLayoutAll.setEnabled(false);
            borderTv.setTextColor(Color.parseColor("#000000"));
            borderTv.setAlpha(0.3f);
            mBorderLayout.setEnabled(false);
            mBorderSwitch.setChecked(true);
            mBorderSwitch.setClickable(false);
            mBorderSwitch.setEnabled(false);

            VectorDrawable drawable = (VectorDrawable) getDrawable(R.drawable.icon_border);
            PorterDuffColorFilter pf = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            drawable.setColorFilter(pf);
            mImgBorder.setImageDrawable(drawable);
        }
        boolean borderOption = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_BORDER_OPTION, false);
        if (mBorderSwitch != null) {
            //add by yangzhong.gong for defect-4433958 begin
            try {
                //add by yangzhong.gong for defect-5233542 begin
                if(mIsMiniMode) {
                    mBorderSwitch.setChecked(true);
                } else {
                    mBorderSwitch.setChecked(borderOption);
                }
                //add by yangzhong.gong for defect-5233542 end
            } catch (Exception e) {
                e.printStackTrace();
            }
            //add by yangzhong.gong for defect-4433958 end
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean borderOption = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_BORDER_OPTION, false);
        if (mBorderSwitch != null) {
            //add by yangzhong.gong for defect-5233542 begin
            if(mIsMiniMode) {
                mBorderSwitch.setChecked(true);
            } else {
                mBorderSwitch.setChecked(borderOption);
            }
            //add by yangzhong.gong for defect-5233542 end
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}