package com.kuding.superball.applist;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.Utils.Utils;
import com.kuding.superball.floatview.NowKeyPanelModel;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 17-1-11.
 */

public class SortAdapter extends BaseAdapter implements SectionIndexer {
    private static final String TAG = "SortAdapter";
    private List<AppInfoModel> list = null;
    private Context mContext;
    private int mAction;
    private ArrayList<BaseItemInfo> mAddedInfos;
    private boolean mIsMiniMode = true;
    private int mPage;

    public SortAdapter(Application app, Context context, int action, List<AppInfoModel> list, int page) {
        this.mContext = context;

        mIsMiniMode = PreferenceUtils.isMiniMode(false);

        this.list = list;
        this.mPage = page;
        mAction = action;
        mAddedInfos = new ArrayList<>();

        if (mIsMiniMode) {
            ArrayList<BaseItemInfo> data = NowKeyPanelModel.getInstance().loadMiniData();
            mAddedInfos.addAll(data);
        } else {
            // 根据页数来判断 用户修改的这一页 的 数据
            if (this.mPage == 1) {
                ArrayList<BaseItemInfo> page1 = NowKeyPanelModel.getInstance().loadData(1);
                mAddedInfos.addAll(page1);
            } else if (mPage == 2) {
                ArrayList<BaseItemInfo> page2 = NowKeyPanelModel.getInstance().loadData(2);
                mAddedInfos.addAll(page2);
            } else {
                ArrayList<BaseItemInfo> page1 = NowKeyPanelModel.getInstance().loadData(1);
                ArrayList<BaseItemInfo> page2 = NowKeyPanelModel.getInstance().loadData(2);
                mAddedInfos.addAll(page1);
                mAddedInfos.addAll(page2);
            }
        }
    }


    public void updateListView(List<AppInfoModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.list.size();
    }

    public AppInfoModel getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final AppInfoModel mContent = list.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            viewHolder.imgIcon = (ImageView) view.findViewById(R.id.icon);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        //// 获取首字母的assii值
        int section = getSectionForPosition(position);
        //通过首字母的assii值来判断是否显示字母
        int positionForSelection = getPositionForSection(section);

        viewHolder.tvLetter.setOnClickListener(null);

        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.getSortLetters());
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }

        viewHolder.tvTitle.setText(this.list.get(position).getAppName());
        Drawable d = list.get(position).getAppIcon();
        d.setBounds(0, 0, Utils.dip2px(mContext, 48), Utils.dip2px(mContext, 48));
//        viewHolder.tvTitle.setCompoundDrawables(
//                list.get(position).getAppIcon(), null, null, null);
        viewHolder.imgIcon.setImageDrawable(d);
        viewHolder.imgIcon.clearColorFilter();
        viewHolder.tvTitle.setTextColor(mContext.getColor(R.color.setting_title_text_color));
        if (mAction == Constant.NOW_KEY_ACTION) {
            for (BaseItemInfo added : mAddedInfos) {
                if (added.getKey_word().equals(this.list.get(position).getPackageName())) {
                    ColorMatrix cm = new ColorMatrix();
                    cm.setSaturation(0f);
                    ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(cm);
                    viewHolder.imgIcon.setColorFilter(cmcf);
                    viewHolder.tvTitle.setTextColor(Color.LTGRAY);
                    list.get(position).setDuplicate(true);
                    break;
                }
            }
        }

        return view;
    }


    final static class ViewHolder {
        TextView tvLetter;
        TextView tvTitle;
        ImageView imgIcon;
    }


    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}
