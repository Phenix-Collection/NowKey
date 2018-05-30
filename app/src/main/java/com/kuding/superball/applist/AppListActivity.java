package com.kuding.superball.applist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.floatview.NowKeyPanelModel;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 17-1-11.
 */

public class AppListActivity extends AppCompatActivity {
    private static final String TAG = "AppListActivity";

    private Context mContext;
    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private ClearEditText mClearEditText;
    private boolean mIsMiniMode = true;

    /**
     * 上次第一个可见元素，用于滚动时记录标识。
     */
    private int lastFirstVisibleItem = -1;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    /**
     * 分组的布局
     */
    private LinearLayout titleLayout;

    /**
     * 分组上显示的字母
     */
    private TextView title;

    private List<AppInfoModel> mAppList;

    private int mAction;
    private int mItemIndex;
    private int mPage;
    private boolean mExternal;
    private int mMenuOperate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mIsMiniMode = PreferenceUtils.isMiniMode(false);

        Intent intent = getIntent();
        if (intent != null) {
            mAction = intent.getIntExtra(Constant.OPERATE_BEHAVIOR, 1);
            mItemIndex = intent.getIntExtra(Constant.NOW_KEY_ITEM_INDEX, -1);
            mPage = intent.getIntExtra(Constant.NOW_KEY_ITEM_PAGE, -1);
            mExternal = intent.getBooleanExtra(Constant.NOW_KEY_ADD_EXTERNAL, false);
            mMenuOperate = intent.getIntExtra(Constant.NOW_KEY_MENU_OPERATE, -1);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.floating_menu_app_name);
        }
        setContentView(R.layout.app_list_activity);
        initViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Constant.GET_APP_RESULT_CANCEL);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.title);
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);//设置相应的字体背景样式
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.setSelection(position);
                }

            }
        });
        sortListView = (ListView) findViewById(R.id.country_lvcountry);
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mAction == Constant.GESTURE_DOUBLE_CLICK) {
                    if (mIsMiniMode) {
                        PreferenceUtils.setMiniGestureDoubleClick(adapter.getItem(position).getAppName());
                        PreferenceUtils.setMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_APP);
                        PreferenceUtils.setMiniGestureDoubleClickAction(adapter.getItem(position).getPackageName());
                    } else {
                        PreferenceUtils.setNormalGestureDoubleClick(adapter.getItem(position).getAppName());
                        PreferenceUtils.setNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_APP);
                        PreferenceUtils.setNormalGestureDoubleClickAction(adapter.getItem(position).getPackageName());
                    }
                } else if (mAction == Constant.GESTURE_LONG_CLICK) {
                    if (mIsMiniMode) {
                        PreferenceUtils.setMiniGestureLongPress(adapter.getItem(position).getAppName());
                        PreferenceUtils.setMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_APP);
                        PreferenceUtils.setMiniGestureLongPressAction(adapter.getItem(position).getPackageName());
                    } else {
                        PreferenceUtils.setNormalGestureLongPress(adapter.getItem(position).getAppName());
                        PreferenceUtils.setNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_APP);
                        PreferenceUtils.setNormalGestureLongPressAction(adapter.getItem(position).getPackageName());
                    }
                } else if (mAction == Constant.GESTURE_SHORT_DRAG) {
                    if (mIsMiniMode) {
                        PreferenceUtils.setMiniGestureShortDrag(adapter.getItem(position).getAppName());
                        PreferenceUtils.setMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_APP);
                        PreferenceUtils.setMiniGestureShortDragAction(adapter.getItem(position).getPackageName());
                    } else {
                        PreferenceUtils.setNormalGestureShortDrag(adapter.getItem(position).getAppName());
                        PreferenceUtils.setNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_APP);
                        PreferenceUtils.setNormalGestureShortDragAction(adapter.getItem(position).getPackageName());
                    }
                } else if (mAction == Constant.NOW_KEY_ACTION) {
                    if (adapter.getItem(position).isDuplicate()) {
                        return;
                    }
                    BaseItemInfo baseItemInfo = new BaseItemInfo();
                    baseItemInfo.setIndex(mItemIndex);
                    baseItemInfo.setType(Constant.NOW_KEY_ITEM_TYPE_APP);
                    baseItemInfo.setKey_word(adapter.getItem(position).getPackageName());

/*                    if (!mIsMiniMode) {
                        if (FloatingBallController.getController(getApplication()).getPage1Items()
                                .contains(baseItemInfo)
                                || FloatingBallController.getController(getApplication()).getPage2Items()
                                .contains(baseItemInfo)) {
                            return;
                        }
                    }*/

                    if (mMenuOperate == Constant.NOW_KEY_MENU_OPERATE_UPDATE) {
                        if (mExternal) {
                            NowKeyPanelModel.getInstance().replaceItem(baseItemInfo, mPage, mExternal);
                        } else {
                            NowKeyPanelModel.getInstance().replaceItem(baseItemInfo, mPage, mExternal);
                        }
                    } else {
                        if (mExternal) {
                            NowKeyPanelModel.getInstance().addItemExternal(baseItemInfo, mPage);
                        } else {
                            NowKeyPanelModel.getInstance().addItem(baseItemInfo, mPage);
                        }
                    }
                }
                setResult(Constant.GET_APP_RESULT_OK);
                finish();
            }
        });

        mAppList = getAllApplications();
        // 根据a-z进行排序源数据
        Collections.sort(mAppList, pinyinComparator);
        adapter = new SortAdapter(getApplication(), this, mAction, mAppList,mPage);
        sortListView.setAdapter(adapter);
        sortListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                //字母连续断层使不能置顶，例如  D （空） F使D到F阶段不存在置顶
                int section;
                try {
                    section = adapter.getSectionForPosition(firstVisibleItem);
                } catch (Exception e) {
                    return;
                }
                int nextSecPosition = adapter.getPositionForSection(section + 1);
                //解决断层置顶
                for (int i = 1; i < 30; i++) {
                    //26个英文字母充分循环
                    if (nextSecPosition == -1) {
                        //继续累加
                        int data = section + 1 + i;
                        nextSecPosition = adapter.getPositionForSection(data);
                    } else {
                        break;
                    }
                }


                if (firstVisibleItem != lastFirstVisibleItem) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                    params.topMargin = 0;
                    titleLayout.setLayoutParams(params);
                    title.setText(String.valueOf((char) section));

                }
                if (nextSecPosition == firstVisibleItem + 1) {
                    View childView = view.getChildAt(0);
                    if (childView != null) {
                        int titleHeight = titleLayout.getHeight();
                        int bottom = childView.getBottom();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout
                                .getLayoutParams();
                        if (bottom < titleHeight) {
                            float pushedDistance = bottom - titleHeight;
                            params.topMargin = (int) pushedDistance;
                            titleLayout.setLayoutParams(params);
                        } else {
                            if (params.topMargin != 0) {
                                params.topMargin = 0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });


        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private List<AppInfoModel> getAllApplications() {
        ArrayList<AppInfoModel> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final PackageManager packageManager = getApplicationContext().getPackageManager();
        List<ResolveInfo> resolveInfos = null;
        resolveInfos = packageManager.queryIntentActivities(intent, 0);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            Log.e(TAG, "queryIntentActivities got null or zero!");
            return null;
        }
        for (ResolveInfo resolveInfo : resolveInfos) {
            final String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
            String appName = getApplicationName(packageName, packageManager);
            AppInfoModel model = new AppInfoModel();
            model.setAppIcon(getApplicationIcon(resolveInfo.activityInfo.applicationInfo, packageManager));
            model.setComponentName(new ComponentName(packageName, resolveInfo.activityInfo.name));
            String pinyin = PinyinComparator.getPingYin(appName);
            String Fpinyin = pinyin.substring(0, 1).toUpperCase();
            model.setAppName(appName);
            model.setPackageName(packageName);
            // 正则表达式，判断首字母是否是英文字母
            if (Fpinyin.matches("[A-Z]")) {
                model.setSortLetters(Fpinyin);
            } else {
                model.setSortLetters("#");
            }
            if (list.contains(model)) continue;
            list.add(model);
        }
        return list;
    }


    /**
     * 获取应用的名称
     */
    private String getApplicationName(String packageName, PackageManager packageManager) {
        String applicationName = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {

        }
        return applicationName;
    }

    /**
     * 获取应用的Icon
     */
    private Drawable getApplicationIcon(ApplicationInfo applicationInfo, PackageManager packageManager) {
        Drawable applicationIcon = applicationInfo.loadIcon(packageManager);
        return applicationIcon;
    }

    private Intent getApplicationIntent(String packageName, PackageManager packageManager) {
        return packageManager.getLaunchIntentForPackage(packageName);
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<AppInfoModel> filterDateList = new ArrayList<AppInfoModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = mAppList;
            titleLayout.setVisibility(View.VISIBLE);
            title.setText("A");
        } else {
            titleLayout.setVisibility(View.GONE);
            filterDateList.clear();
            for (AppInfoModel sortModel : mAppList) {
                String name = sortModel.getAppName();
                if (name.toLowerCase().contains(filterStr.toLowerCase())
                        || PinyinComparator.getPingYin(name).startsWith(filterStr)) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }

}
