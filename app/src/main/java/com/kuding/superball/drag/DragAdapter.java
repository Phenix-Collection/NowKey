package com.kuding.superball.drag;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.FunctionUtils;
import com.kuding.superball.Utils.Utils;
import com.kuding.superball.info.AppItemInfo;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.info.FunctionItemInfo;
import com.kuding.superball.R;

import java.util.List;


public class DragAdapter extends BaseAdapter {
    private final static String TAG = "DragAdapter";
    private boolean isItemShow = false;
    private Context context;
    private Resources res;
    private int holdPosition;
    private boolean isChanged = false;
    boolean isVisible = true;

    public List<BaseItemInfo> listData;

    private TextView item_text;
    public int remove_position = -1;
    private FuncEditItemListener funcEditItemListener;
    private final PackageManager pm;

    public DragAdapter(Context context,
                       List<BaseItemInfo> listData) {
        this.context = context;
        this.listData = listData;
        res = context.getResources();
        pm = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return listData == null ? 0 : listData.size();
    }

    @Override
    public BaseItemInfo getItem(int position) {
        if (listData != null && listData.size() != 0
                && position < listData.size()) {
            return listData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderNormal holderNormal = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.drag_listview_item, null);
            holderNormal = new ViewHolderNormal();
            holderNormal.funcIcon = (ImageView) convertView
                    .findViewById(R.id.funcicon);
            holderNormal.appIcon = (ImageView) convertView
                    .findViewById(R.id.appicon);
            holderNormal.funcText = (TextView) convertView
                    .findViewById(R.id.functext);
            holderNormal.fun_editIcon = (ImageView) convertView
                    .findViewById(R.id.fun_edit);
            holderNormal.func_removeIcon = (ImageView) convertView
                    .findViewById(R.id.click_remove);
            holderNormal.func_addIcon = (ImageView) convertView
                    .findViewById(R.id.click_add);
            convertView.setTag(holderNormal);
        } else {
            holderNormal = (ViewHolderNormal) convertView.getTag();
        }

        setChildView(position, holderNormal.funcText, holderNormal.funcIcon, holderNormal.appIcon, holderNormal.func_removeIcon, holderNormal.func_addIcon);

        return convertView;
    }

    private void setChildView(final int id, TextView tv, ImageView img, ImageView appIcon, ImageView remove, ImageView add) {
        String item = listData.get(id).getKey_word();
        int type = listData.get(id).getType();
        if (type == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
            FunctionItemInfo info = FunctionUtils.functionFilter(context, item);
            appIcon.setVisibility(View.GONE);
            img.setVisibility(View.VISIBLE);
            if (info != null) {
                Drawable d = info.getIcon();
                d.setTint(context.getColor(R.color.nowkey_color));
                tv.setTextColor(context.getColor(R.color.setting_title_text_color));
                img.setImageDrawable(d);
                tv.setText(info.getText());
                remove.setVisibility(View.VISIBLE);
                add.setVisibility(View.GONE);
                add.setOnClickListener(null);
            } else {
                img.setImageDrawable(null);
                appIcon.setImageDrawable(null);
                tv.setText("");
                remove.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (funcEditItemListener != null) {
                            funcEditItemListener.addFuncItem(id);
                        }
                    }
                });
            }
        } else if (type == Constant.NOW_KEY_ITEM_TYPE_APP) {
            AppItemInfo info = (AppItemInfo) Utils.convertBaseItemInfo(context, listData.get(id));
            appIcon.setVisibility(View.VISIBLE);
            img.setVisibility(View.GONE);
            if (info != null) {
                Drawable d = info.getIcon();
                tv.setTextColor(context.getColor(R.color.setting_title_text_color));
                appIcon.setImageDrawable(d);
                tv.setText(info.getText());
                remove.setVisibility(View.VISIBLE);
                add.setVisibility(View.GONE);
                add.setOnClickListener(null);
            } else {
                img.setImageDrawable(null);
                appIcon.setImageDrawable(null);
                tv.setText("");
                remove.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (funcEditItemListener != null) {
                            funcEditItemListener.addFuncItem(id);
                        }
                    }
                });
            }
        } else {
            appIcon.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
            img.setImageDrawable(null);
            appIcon.setImageDrawable(null);
            tv.setText("");
            remove.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (funcEditItemListener != null) {
                        funcEditItemListener.addFuncItem(id);
                    }
                }
            });
        }

    }

    public void setFuncEditItemListener(
            FuncEditItemListener funcEditItemListener) {
        this.funcEditItemListener = funcEditItemListener;
    }

    static class ViewHolderNormal {
        ImageView funcIcon;
        ImageView appIcon;
        TextView funcText;
        ImageView fun_editIcon;
        ImageView func_removeIcon;
        ImageView func_addIcon;
    }

    public interface FuncEditItemListener {
        void editFuncItem(int type);
        void addFuncItem(int position);
    }
}