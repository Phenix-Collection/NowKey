package com.kuding.superball;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.R;

public class ThemeSettingsActivity extends AppCompatActivity {

    private String[] mThemeTitles;
    private String[] mThemeColors;

    private int mSelectId;

    private Context mContext;
    private LayoutInflater mInflater;
    private ListView mThemeList;
    private View[] mSelectedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mThemeTitles = mContext.getResources().getStringArray(R.array.theme_colors_title);
        mThemeColors = mContext.getResources().getStringArray(R.array.theme_colors);

        setContentView(R.layout.settings_theme_list);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.now_key_option_theme_title);
        }
        mSelectedView = new View[mThemeColors.length];
        mInflater = LayoutInflater.from(this);
        mThemeList = (ListView) findViewById(R.id.theme_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mThemeList.setAdapter(new ThemeAdapter());
        mSelectId = PreferenceUtils.getInt(PreferenceUtils.NOW_KEY_THEME_INDEX, 0);
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

    class ThemeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mThemeColors.length;
        }

        @Override
        public Object getItem(int i) {
            return mThemeColors[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int id = i;
            final ViewHolderItem itemHolder;
            if (view == null) {
                view = mInflater.inflate(R.layout.settings_theme_color_item, null);
                itemHolder = new ViewHolderItem();
                itemHolder.imgItem = (ImageView) view.findViewById(R.id.theme_color_icon);
                itemHolder.tvItem = (TextView) view.findViewById(R.id.theme_item_title);
                itemHolder.selectedItem = (ImageView) view.findViewById(R.id.theme_select_icon);
                view.setTag(itemHolder);
                mSelectedView[id] = itemHolder.selectedItem;
            } else {
                itemHolder = (ViewHolderItem) view.getTag();
            }
            if(i == 0){
                itemHolder.imgItem.setBackgroundColor(Color.parseColor("#4a4a4a"));
            }else{
                itemHolder.imgItem.setBackgroundColor(Color.parseColor(mThemeColors[i]));
            }
            if (i == mSelectId) {
                itemHolder.selectedItem.setVisibility(View.VISIBLE);
            } else {
                itemHolder.selectedItem.setVisibility(View.GONE);
            }
            itemHolder.tvItem.setText(mThemeTitles[i]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelectId != id) {
                        View last = mSelectedView[mSelectId];
                        if (last != null) {
                            last.setVisibility(View.GONE);
                        }
                        itemHolder.selectedItem.setVisibility(View.VISIBLE);
                        mSelectId = id;
                        PreferenceUtils.setInt(PreferenceUtils.NOW_KEY_THEME, Color.parseColor(mThemeColors[id]));
                        PreferenceUtils.setInt(PreferenceUtils.NOW_KEY_THEME_INDEX, id);
                        Intent intent = new Intent();
                        intent.setAction(Constant.BROADCAST_THEME_CHANGE);
                        mContext.sendBroadcast(intent);
                        }
                }
            });
            return view;
        }
    }

    private static class ViewHolderItem {
        private TextView tvItem;
        private ImageView imgItem;
        private ImageView selectedItem;
    }
}