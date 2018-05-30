package com.kuding.superball;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.help.GifView;
import com.kuding.superball.R;

/**
 * Created by yeah017 on 2017/3/14.
 */

public class GestureSettingsActivity extends AppCompatActivity {
    private static final int CLICK_TYPE_DOUBLE = 1;
    private static final int CLICK_TYPE_LONG = 2;
    private TextView mDoubleClickActionTv;
    private TextView mLongPressActionTv;

    private LinearLayout mDoubleClickLayout;
    private LinearLayout mLongClickLayout;

    private FrameLayout mTipsDialog;
    private GifView mGifView;
    private Button mTipsButton;

    private boolean mShowingTips = false;
    private int mClickType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_gesture_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.now_key_option_gesture_title);
        }
        mTipsDialog = (FrameLayout) findViewById(R.id.tips_layout);
        mGifView = (GifView) findViewById(R.id.tip_dialog_gif);
        mTipsButton = (Button) findViewById(R.id.tip_dialog_button);
        mTipsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShowingTips) {
                    if (mClickType == CLICK_TYPE_LONG) {
                        PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_FIRST_SET_LONG_CLICK, false);
                    } else if (mClickType == CLICK_TYPE_DOUBLE) {
                        PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_FIRST_SET_DOUBLE_CLICK, false);
                    }
                    mClickType = 0;
                    mTipsDialog.setVisibility(View.GONE);
                    mShowingTips = false;
                }
            }
        });
        mDoubleClickActionTv = (TextView) findViewById(R.id.now_key_gesture_double_action);
        mLongPressActionTv = (TextView) findViewById(R.id.now_key_gesture_long_action);
        mDoubleClickLayout = (LinearLayout) findViewById(R.id.double_click_layout);
        mDoubleClickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mShowingTips) {
                    boolean first = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_FIRST_SET_DOUBLE_CLICK, true);
                    mClickType = CLICK_TYPE_DOUBLE;
                    if (first && mTipsDialog != null && mGifView != null && !mShowingTips) {
                        mShowingTips = true;
                        mTipsDialog.setVisibility(View.VISIBLE);
                        //mGifView.setMovieResource(R.raw.help_gesture_double_click);
                    } else {
                        Intent i = new Intent();
                        i.setClass(GestureSettingsActivity.this, FunctionActivity.class);
                        i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.GESTURE_DOUBLE_CLICK);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }
            }
        });
        mLongClickLayout = (LinearLayout) findViewById(R.id.long_click_layout);
        mLongClickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mShowingTips) {
                    boolean first = PreferenceUtils.getBoolean(PreferenceUtils.NOW_KEY_FIRST_SET_LONG_CLICK, true);
                    mClickType = CLICK_TYPE_LONG;
                    if (first && mTipsDialog != null && mGifView != null && !mShowingTips) {
                        mShowingTips = true;
                        mTipsDialog.setVisibility(View.VISIBLE);
                        //mGifView.setMovieResource(R.raw.help_gesture_long_click);
                    } else {
                        Intent i = new Intent();
                        i.setClass(GestureSettingsActivity.this, FunctionActivity.class);
                        i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.GESTURE_LONG_CLICK);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String doubleClickAction = PreferenceUtils.getNormalGestureDoubleClickAction(getString(R.string.action_none));
        if (mDoubleClickActionTv != null) {
            mDoubleClickActionTv.setText(doubleClickAction);
        }
        String longPressAction = PreferenceUtils.getNormalGestureLongPressAction(getString(R.string.action_none));
        if (mLongPressActionTv != null) {
            mLongPressActionTv.setText(longPressAction);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mShowingTips && mTipsDialog != null) {
                    mTipsDialog.setVisibility(View.GONE);
                    mShowingTips = false;
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mShowingTips && mTipsDialog != null) {
            mTipsDialog.setVisibility(View.GONE);
            mShowingTips = false;
        } else {
            finish();
        }
    }
}
