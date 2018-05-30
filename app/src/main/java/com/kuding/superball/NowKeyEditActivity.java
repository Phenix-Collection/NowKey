package com.kuding.superball;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import com.kuding.superball.Utils.BaseModelComparator;
import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.drag.DragAdapter;
import com.kuding.superball.drag.DragSortListView;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.R;
import com.kuding.superball.floatview.CustomDialog;
import com.kuding.superball.floatview.FloatingBallController;
import com.kuding.superball.floatview.NowKeyPanelModel;
import com.kuding.superball.service.NowKeyService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 17-3-15.
 */

public class NowKeyEditActivity extends AppCompatActivity implements NowKeyPanelModel.NowKeyModelCallback {
    private int ADD_ITEM_REQ_CODE = 100;

    private Context mContext;
    private ActionBar mActionBar;

    private DragSortListView mDragSortListView1;
    private DragSortListView mDragSortListView2;

    private CustomDialog mWarningDialog;

    private ScrollView func_scrollview;

    private DragAdapter dragAdapter1;
    private DragAdapter dragAdapter2;

    private Toast showToast;

    private List<BaseItemInfo> choosedLists1;
    private List<BaseItemInfo> choosedLists2;

    private Handler mHandler = new Handler();

    private int mRemoveWitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.funcsettings);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.now_key_option_edit_title);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        initView();
        initData();
        setAdapterAndListeners();
        //NowKeyPanelModel.getInstance(getApplicationContext()).setNowkeyModelCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        func_scrollview = (ScrollView) findViewById(R.id.func_scrollview);
        mDragSortListView1 = (DragSortListView) findViewById(R.id.drag_sort_list_1);
        mDragSortListView2 = (DragSortListView) findViewById(R.id.drag_sort_list_2);
    }

    private void initData() {
        choosedLists1 =
                FloatingBallController.getController(getApplication()).getPage1DragItem();
        choosedLists2 =
                FloatingBallController.getController(getApplication()).getPage2DragItem();
    }

    private void setAdapterAndListeners() {
        dragAdapter1 = new DragAdapter(mContext, choosedLists1);
        mDragSortListView1.setAdapter(dragAdapter1);
        mDragSortListView1.setDropListener(onDrop);
        mDragSortListView1.setRemoveListener(onRemove);
        dragAdapter1.setFuncEditItemListener(funcEditItemListener);

        dragAdapter2 = new DragAdapter(mContext, choosedLists2);
        mDragSortListView2.setAdapter(dragAdapter2);
        mDragSortListView2.setDropListener(onDrop2);
        mDragSortListView2.setRemoveListener(onRemove2);
        dragAdapter2.setFuncEditItemListener(funcEditItemListener2);

    }


    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            if (from != to) {
                BaseItemInfo item = dragAdapter1.getItem(from);
                choosedLists1.remove(from);
                choosedLists1.add(to, item);
                for (int i = 0; i < choosedLists1.size(); i++) {
                    BaseItemInfo info = choosedLists1.get(i);
                    if (info != null) {
                        info.setIndex(i);
                    }
                }
                Collections.sort(choosedLists1, new BaseModelComparator());
                dragAdapter1.notifyDataSetChanged();
                mDragSortListView1.moveCheckState(from, to);
                ArrayList<BaseItemInfo> updated =
                        new ArrayList<>(Arrays.asList(new BaseItemInfo[choosedLists1.size()]));
                Collections.copy(updated, choosedLists1);
                ArrayList<BaseItemInfo> empty = new ArrayList<>();
                for (int i = 0; i < updated.size(); i++) {
                    BaseItemInfo info = updated.get(i);
                    if (info == null || info.getType() == 0 || "".equals(info.getKey_word())) {
                        empty.add(info);
                    }
                }
                for (BaseItemInfo info : empty) {
                    updated.remove(info);
                }
                FloatingBallController.getController(getApplication()).updatePage1Position(updated);
            }
        }
    };

    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which, boolean flag) {
            BaseItemInfo item = dragAdapter1.getItem(which);
            if (item != null) {
                FloatingBallController.getController(getApplication()).deleteItemExternal(item);
                item.setType(0);
                item.setKey_word("");
                dragAdapter1.notifyDataSetChanged();
            }
        }

        @Override
        public boolean isRemoveLast(int which) {
            int count = 0;
            for (BaseItemInfo info : choosedLists1) {
                if (info != null && !"".equals(info.getKey_word())) {
                    count++;
                }
            }
            for (BaseItemInfo info : choosedLists2) {
                if (info != null && !"".equals(info.getKey_word())) {
                    count++;
                }
            }
            if (count == 1) {
                mRemoveWitch = which;
                showWarningDialog();
                return true;
            } else {
                return false;
            }
        }
    };

    private DragSortListView.DropListener onDrop2 = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            if (from != to) {
                BaseItemInfo item = dragAdapter2.getItem(from);
                choosedLists2.remove(from);
                choosedLists2.add(to, item);
                for (int i = 0; i < choosedLists2.size(); i++) {
                    BaseItemInfo info = choosedLists2.get(i);
                    if (info != null) {
                        info.setIndex(i);
                    }
                }
                Collections.sort(choosedLists2, new BaseModelComparator());
                dragAdapter2.notifyDataSetChanged();
                mDragSortListView2.moveCheckState(from, to);
                ArrayList<BaseItemInfo> updated =
                        new ArrayList<>(Arrays.asList(new BaseItemInfo[choosedLists2.size()]));
                Collections.copy(updated, choosedLists2);
                ArrayList<BaseItemInfo> empty = new ArrayList<>();
                for (int i = 0; i < updated.size(); i++) {
                    BaseItemInfo info = updated.get(i);
                    if (info == null || info.getType() == 0 || "".equals(info.getKey_word())) {
                        empty.add(info);
                    }
                }
                for (BaseItemInfo info : empty) {
                    updated.remove(info);
                }
                FloatingBallController.getController(getApplication()).updatePage2Position(updated);
            }
        }
    };

    private DragSortListView.RemoveListener onRemove2 = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which, boolean flag) {
            BaseItemInfo item = dragAdapter2.getItem(which);
            if (item != null) {
                FloatingBallController.getController(getApplication()).deleteItemExternal(item);
                item.setType(0);
                item.setKey_word("");
                dragAdapter2.notifyDataSetChanged();
            }
        }

        @Override
        public boolean isRemoveLast(int which) {
            int count = 0;
            for (BaseItemInfo info : choosedLists1) {
                if (info != null && !"".equals(info.getKey_word())) {
                    count++;
                }
            }
            for (BaseItemInfo info : choosedLists2) {
                if (info != null && !"".equals(info.getKey_word())) {
                    count++;
                }
            }
            if (count == 1) {
                mRemoveWitch = which;
                showWarningDialog();
                return true;
            } else {
                return false;
            }
        }
    };

    private DragAdapter.FuncEditItemListener funcEditItemListener = new DragAdapter.FuncEditItemListener() {

        @Override
        public void editFuncItem(int goType) {
//            if (goType == FuncUtil.START_MUSIC_PLAYLIST_ID) {
//                Intent mIntent = new Intent();
//                mIntent.setAction("com.tct.mix.action.FUNCLOCKSCREEN");
//                mIntent.putExtra("funcSettings", true);
//                mContext.sendBroadcast(mIntent);
//            } else if (goType == FuncUtil.NAVIGATE_HOME_ID) {
//                Intent mIntent = new Intent(
//                        "android.settings.NAVIGATE_HOME_SETTINGS");
//                startActivity(mIntent);
//            }
//            else if (goType == FuncUtil.CALL_A_CONTACT_ID) {
//                Intent intent = new Intent("com.android.contacts.action.GET_MULTI_PHONES");
//                intent.addCategory("android.intent.category.DEFAULT");
//                intent.setType(Phone.CONTENT_TYPE);
//                intent.putExtra("funcParams", 0);
//                try {
//                    startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
//                } catch (ActivityNotFoundException e) {
//                }
//            }
        }

        @Override
        public void addFuncItem(int position) {
            Intent i = new Intent();
            i.setClass(mContext, FunctionActivity.class);
            i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.NOW_KEY_ACTION);
            i.putExtra(Constant.NOW_KEY_ITEM_INDEX, position);
            i.putExtra(Constant.NOW_KEY_ITEM_PAGE, 1);
            i.putExtra(Constant.NOW_KEY_ADD_EXTERNAL, true);
            startActivityForResult(i, ADD_ITEM_REQ_CODE);
        }
    };


    private DragAdapter.FuncEditItemListener funcEditItemListener2 = new DragAdapter.FuncEditItemListener() {

        @Override
        public void editFuncItem(int goType) {
//            if (goType == FuncUtil.START_MUSIC_PLAYLIST_ID) {
//                Intent mIntent = new Intent();
//                mIntent.setAction("com.tct.mix.action.FUNCLOCKSCREEN");
//                mIntent.putExtra("funcSettings", true);
//                mContext.sendBroadcast(mIntent);
//            } else if (goType == FuncUtil.NAVIGATE_HOME_ID) {
//                Intent mIntent = new Intent(
//                        "android.settings.NAVIGATE_HOME_SETTINGS");
//                startActivity(mIntent);
//            }
//            else if (goType == FuncUtil.CALL_A_CONTACT_ID) {
//                Intent intent = new Intent("com.android.contacts.action.GET_MULTI_PHONES");
//                intent.addCategory("android.intent.category.DEFAULT");
//                intent.setType(Phone.CONTENT_TYPE);
//                intent.putExtra("funcParams", 0);
//                try {
//                    startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
//                } catch (ActivityNotFoundException e) {
//                }
//            }
        }

        @Override
        public void addFuncItem(int position) {
            Intent i = new Intent();
            i.setClass(mContext, FunctionActivity.class);
            i.putExtra(Constant.OPERATE_BEHAVIOR, Constant.NOW_KEY_ACTION);
            i.putExtra(Constant.NOW_KEY_ITEM_INDEX, position);
            i.putExtra(Constant.NOW_KEY_ITEM_PAGE, 2);
            i.putExtra(Constant.NOW_KEY_ADD_EXTERNAL, true);
            startActivityForResult(i, ADD_ITEM_REQ_CODE);
        }
    };


    private void showWarningDialog() {
        if (mWarningDialog == null) {
            CustomDialog.Builder dialog = new CustomDialog.Builder(this);
            mWarningDialog =
                    dialog.setTitle(R.string.application_name)
                            .setMessage(R.string.now_key_item_delete_warning)
                            .setPositiveButton(R.string.now_key_item_delete_warning_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    BaseItemInfo item = dragAdapter1.getItem(mRemoveWitch);
                                    if (item != null && !"".equals(item.getKey_word())) {
                                        FloatingBallController.getController(getApplication()).deleteItemExternal(item);
                                        item.setType(0);
                                        item.setKey_word("");
                                        dragAdapter1.notifyDataSetChanged();
                                    }

                                    item = dragAdapter2.getItem(mRemoveWitch);
                                    if (item != null && !"".equals(item.getKey_word())) {
                                        FloatingBallController.getController(getApplication()).deleteItemExternal(item);
                                        item.setType(0);
                                        item.setKey_word("");
                                        dragAdapter2.notifyDataSetChanged();
                                    }
                                    PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
                                    Intent intent = new Intent(mContext, NowKeyService.class);
                                    intent.setAction(NowKeyService.STOP_NOW_KEY);
                                    startService(intent);
                                    dialogInterface.dismiss();
                                }
                            }).setNegativeButton(R.string.now_key_item_delete_warning_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            mWarningDialog.setCanceledOnTouchOutside(false);
        }
        if (!mWarningDialog.isShowing()) {
            mWarningDialog.show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NowKeyPanelModel.getInstance().removeNowKeyModelCallback(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_REQ_CODE) {
            initData();
            setAdapterAndListeners();
        }
    }

    @Override
    public void onNowKeyItemDelete(BaseItemInfo item, int page) {
        // add by junye.li for defect 4532093 begin
        if (item == null) {
            if (page == 1) {
                for (BaseItemInfo info : choosedLists1) {
                    if (info != null && !"".equals(info.getKey_word()) && info.getType() != 0) {
                        info.setType(0);
                        info.setKey_word("");
                        dragAdapter1.notifyDataSetChanged();
                    }
                }
            } else if (page == 2) {
                for (BaseItemInfo info : choosedLists2) {
                    if (info != null && !"".equals(info.getKey_word()) && info.getType() != 0) {
                        info.setType(0);
                        info.setKey_word("");
                        dragAdapter2.notifyDataSetChanged();
                    }
                }
            }
            return;
        }
        // add by junye.li for defect 4532093 end

        int index = choosedLists1.indexOf(item);
        if (index != -1) {
            item = dragAdapter1.getItem(index);
            if (item != null && !"".equals(item.getKey_word()) && item.getType() != 0) {
//                FloatingBallController.getController(getApplication()).deleteItemExternal(item);
                item.setType(0);
                item.setKey_word("");
                dragAdapter1.notifyDataSetChanged();
            }
        }

        index = choosedLists2.indexOf(item);
        if (index != -1) {
            item = dragAdapter2.getItem(index);
            if (item != null && !"".equals(item.getKey_word()) && item.getType() != 0) {
//                FloatingBallController.getController(getApplication()).deleteItemExternal(item);
                item.setType(0);
                item.setKey_word("");
                dragAdapter2.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onNowKeyItemAdd(BaseItemInfo item, int page, boolean external) {

    }

    @Override
    public void onNowKeyItemReplace(BaseItemInfo item, int page, boolean external) {

    }

    @Override
    public void onNowKeyItemUpdate(BaseItemInfo item, int page) {

    }

    @Override
    public void onNowKeyItemUpdatePosition(ArrayList<BaseItemInfo> items, int page) {

    }
}
