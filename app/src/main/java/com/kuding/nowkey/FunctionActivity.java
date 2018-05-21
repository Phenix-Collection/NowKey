package com.kuding.nowkey;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kuding.nowkey.Utils.Constant;
import com.kuding.nowkey.Utils.FunctionUtils;
import com.kuding.nowkey.Utils.NowKeyParser2;
import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.info.BaseItemInfo;
import com.kuding.nowkey.info.FunctionItemInfo;
import com.kuding.nowkey.R;
import com.kuding.nowkey.applist.AppListActivity;
import com.kuding.nowkey.floatview.GestureController;
import com.kuding.nowkey.floatview.NowKeyPanelModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * 功能选择界面
 */

public class FunctionActivity extends AppCompatActivity {
    private static final String TAG = "FunctionActivity";

    private static final int PERMISSION_READ_CONTACT_REQUEST_CODE = 111;

    public static final int GROUP_APP_ID = 0;
    public static final int GROUP_OPERATION_ID = 1;
    public static final int GROUP_FUNCTION_ID = 2;
    public static final int GROUP_TOOL_ID = 3;
    //    public static final int GROUP_PHONE_ID = 4;
    public static final int GROUP_ALCATEL_ID = 4;

    private int mAction;
    private boolean mIsGesture = false;         // 当前是否是选择 手势 的功能
    private int mItemIndex;
    private int mPage;
    private boolean mExternal;
    private String mKeyWord;                    // 传入的keyword
    private int mMenuOperate = -1;              // 本次菜单执行的操作  更新0、增加1、删除2 之类的

/*    private final int[] mGroup = {
            R.string.now_key_function_header_apps, R.string.now_key_function_header_operation,
            R.string.now_key_function_header_function, R.string.now_key_function_header_tool,
//            R.string.now_key_function_header_phone,
            R.string.now_key_function_header_alcatel
    };*/

    private ArrayList<String> mDataLists;
    private ArrayList<BaseItemInfo> mAddedInfos;

    private Context mContext;
    //private ExpandableListView mFunctionsList;
    private ListView mFunctionsList;

    private boolean mIsMiniMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsMiniMode = PreferenceUtils.isMiniMode(false);

        Intent intent = getIntent();
        if (intent != null) {
            mAction = intent.getIntExtra(Constant.OPERATE_BEHAVIOR, 1);
            mItemIndex = intent.getIntExtra(Constant.NOW_KEY_ITEM_INDEX, -1);
            mPage = intent.getIntExtra(Constant.NOW_KEY_ITEM_PAGE, -1);
            mExternal = intent.getBooleanExtra(Constant.NOW_KEY_ADD_EXTERNAL, false);
            mMenuOperate = intent.getIntExtra(Constant.NOW_KEY_MENU_OPERATE, -1);
            mKeyWord = intent.getStringExtra(Constant.NOW_KEY_KEY_WORD);
        }

        //modify by yangzhong.gong for defect-4451135 begin
        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(R.string.now_key_option_gesture_and_function_title);
            }
        } catch (Exception e) {

        }
        //modify by yangzhong.gong for defect-4451135 end
        setContentView(R.layout.now_key_functions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /*if (mAction == Constant.NOW_KEY_ACTION) {
                    if (mExternal) {
                        NowKeyPanelModel.getInstance(mContext).addItemExternal(null, mPage);
                    } else {
                        NowKeyPanelModel.getInstance(mContext).addItem(null, mPage);
                    }
                }*/
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final String[] PHONE_COLUMNS = {
            Phone.DISPLAY_NAME,
            Phone.PHOTO_ID,
            Phone.NUMBER,
            Phone.TYPE,
            Phone.LABEL,
            Phone.LOOKUP_KEY,
    };

    private static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 0;
    private static final int PHONE_PHOTO_ID_COLUMN_INDEX = 1;
    private static final int PHONE_NUMBER_COLUMN_INDEX = 2;
    private static final int PHONE_TYPE_COLUMN_INDEX = 3;
    private static final int PHONE_LABEL_COLUMN_INDEX = 4;
    private static final int PHONE_LOOKUP_KEY_COLUMN_INDEX = 5;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.GET_APP_REQUEST) {
            if (resultCode == Constant.GET_APP_RESULT_OK) {
                finish();
            }
        } else if (requestCode == Constant.GET_CONTACT_REQUEST) {
            if (data == null) {
                finish();
                return;
            }
            //  获取返回的联系人的Uri信息
            Uri contactDataUri = data.getData();
            Cursor cursor = getContentResolver().query(contactDataUri, PHONE_COLUMNS, null, null, null);
            StringBuilder phoneNumber = new StringBuilder();
            String name = "";
            String mPhoneNumber;
            if (cursor.moveToFirst()) {
                do {
//                    //   获得联系人记录的ID
//                    String contactId = cursor.getString(cursor.getColumnIndex(
//                            ContactsContract.Contacts._ID));
//                    //  获得联系人的名字
//                    name = cursor.getString(cursor.getColumnIndex(
//                            ContactsContract.Contacts.DISPLAY_NAME));

                    name = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
                    mPhoneNumber = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);

//                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.
//                            Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.
//                            Phone.CONTACT_ID + "=" + "?", new String[]{contactId}, null);
//                    if (phoneCursor.moveToFirst()) {
//                        do {
//                            String s = phoneCursor.getString(phoneCursor.getColumnIndex(
//                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneNumber.append(mPhoneNumber);
//                            phoneNumber.append("/");
//                        } while (phoneCursor.moveToNext());
//                    }
//                    //  关闭查询手机号码的cursor
//                    phoneCursor.close();

                } while (cursor.moveToNext());
            }
            //  关闭查询联系人信息的cursor
            cursor.close();
            String[] numbers = phoneNumber.toString().split("/");
            if (numbers.length == 0) {
                Toast.makeText(this, R.string.now_key_call_a_contact_no_number, Toast.LENGTH_SHORT).show();
                finish();
            } else if (numbers.length == 1) {
                onFinishGetContact(name, phoneNumber.toString());
                finish();
            } else {
                showCommonListDialog(name, numbers);
            }
        }
    }

    private void showCommonListDialog(final String name, final String[] numbers) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(name);
        alert.setItems(numbers, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String displayName = name;
                if ("".equals(displayName)) displayName = numbers[which];
                onFinishGetContact(displayName, numbers[which]);
                finish();
            }
        }).create();
        alert.setCancelable(false);
        alert.show();
    }

    private void onFinishGetContact(String name, String phoneNumber) {
        if ("".equals(name)) name = phoneNumber;
        if (mAction == Constant.GESTURE_DOUBLE_CLICK) {
            if (mIsMiniMode) {
                PreferenceUtils.setMiniGestureDoubleClick(name);
                PreferenceUtils.setMiniGestureDoubleClickAction("callacontact");
                PreferenceUtils.setMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setMiniGestureDoubleClickCall(name + "," + phoneNumber);
            } else {
                PreferenceUtils.setNormalGestureDoubleClick(name);
                PreferenceUtils.setNormalGestureDoubleClickAction("callacontact");
                PreferenceUtils.setNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setNormalGestureDoubleClickCall(name + "," + phoneNumber);
            }
        } else if (mAction == Constant.GESTURE_LONG_CLICK) {
            if (mIsMiniMode) {
                PreferenceUtils.setMiniGestureLongPress(name);
                PreferenceUtils.setMiniGestureLongPressAction("callacontact");
                PreferenceUtils.setMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setMiniGestureLongPressCall(name + "," + phoneNumber);
            } else {
                PreferenceUtils.setNormalGestureLongPress(name);
                PreferenceUtils.setNormalGestureLongPressAction("callacontact");
                PreferenceUtils.setNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setNormalGestureLongPressCall(name + "," + phoneNumber);
            }
        } else if (mAction == Constant.GESTURE_SHORT_DRAG) {
            if (mIsMiniMode) {
                PreferenceUtils.setMiniGestureShortDrag(name);
                PreferenceUtils.setMiniGestureShortDragAction("callacontact");
                PreferenceUtils.setMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setMiniGestureShortDragCall(name + "," + phoneNumber);
            } else {
                PreferenceUtils.setNormalGestureShortDrag(name);
                PreferenceUtils.setNormalGestureShortDragAction("callacontact");
                PreferenceUtils.setNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                PreferenceUtils.setNormalGestureShortDragCall(name + "," + phoneNumber);
            }
        } else if (mAction == Constant.NOW_KEY_ACTION) {
            BaseItemInfo baseItemInfo = new BaseItemInfo();
            baseItemInfo.setIndex(mItemIndex);
            baseItemInfo.setType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
            baseItemInfo.setKey_word("callacontact");
            if (mIsMiniMode) {
                PreferenceUtils.setCallContactItemMini(name + "," + phoneNumber);
            } else {
                PreferenceUtils.setCallContactItem(name + "," + phoneNumber);
            }
            if (mMenuOperate == Constant.NOW_KEY_MENU_OPERATE_UPDATE) {
                NowKeyPanelModel.getInstance().replaceItem(baseItemInfo, mPage, false);
            }
        }
    }

    private void initView() {
        mContext = this;
        mFunctionsList = (ListView) findViewById(R.id.functions_list);
        mDataLists = new ArrayList<String>();

        // 只有手势操作才会有none这一项 ， 第一项数据为空，用户选择之后，可以关闭此项操作
        if (mAction == Constant.GESTURE_DOUBLE_CLICK
                || mAction == Constant.GESTURE_SHORT_DRAG
                || mAction == Constant.GESTURE_LONG_CLICK) {
            mDataLists.add("none");
            mIsGesture = true;
        }
        try {
            mDataLists.addAll(NowKeyParser2.parseDefaultNowKey(this, R.xml.default_nowkey));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FunctionsAdapter adapter = new FunctionsAdapter();
        mFunctionsList.setAdapter(adapter);
        mFunctionsList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        int Id = (int) id;
                        String item = mDataLists.get(Id);
                        // 先处理特殊的事件 none apps callacontact
                        if ("none".equals(item) && mIsGesture) {
                            if (mIsMiniMode) {
                                if (mAction == Constant.GESTURE_DOUBLE_CLICK) {
                                    GestureController.getInstance(mContext).cleanMiniModeGesture(Constant.GESTURE_DOUBLE_CLICK);
                                } else if (mAction == Constant.GESTURE_LONG_CLICK) {
                                    GestureController.getInstance(mContext).cleanMiniModeGesture(Constant.GESTURE_LONG_CLICK);
                                } else if (mAction == Constant.GESTURE_SHORT_DRAG) {
                                    GestureController.getInstance(mContext).cleanMiniModeGesture(Constant.GESTURE_SHORT_DRAG);
                                }
                            } else {
                                if (mAction == Constant.GESTURE_DOUBLE_CLICK) {
                                    GestureController.getInstance(mContext).cleanNormalModeGesture(Constant.GESTURE_DOUBLE_CLICK);
                                } else if (mAction == Constant.GESTURE_LONG_CLICK) {
                                    GestureController.getInstance(mContext).cleanNormalModeGesture(Constant.GESTURE_LONG_CLICK);
                                } else if (mAction == Constant.GESTURE_SHORT_DRAG) {
                                    GestureController.getInstance(mContext).cleanNormalModeGesture(Constant.GESTURE_SHORT_DRAG);
                                }
                            }
                            finish();
                            return;
                        } else if ("apps".equals(item)) {
                            Intent intent = new Intent();
                            intent.setClass(FunctionActivity.this, AppListActivity.class);
                            intent.putExtra(Constant.OPERATE_BEHAVIOR, mAction);
                            intent.putExtra(Constant.NOW_KEY_ITEM_INDEX, mItemIndex);
                            intent.putExtra(Constant.NOW_KEY_ITEM_PAGE, mPage);
                            intent.putExtra(Constant.NOW_KEY_ADD_EXTERNAL, mExternal);
                            intent.putExtra(Constant.NOW_KEY_MENU_OPERATE, mMenuOperate);
                            startActivityForResult(intent, Constant.GET_APP_REQUEST);
                            return;
                        } else if ("callacontact".equals(item)) {
                            BaseItemInfo baseItemInfo = new BaseItemInfo();
                            baseItemInfo.setIndex(mItemIndex);
                            baseItemInfo.setType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            baseItemInfo.setKey_word(mDataLists.get(Id));

                            // 先判断是否重复,如果重复就不处理
                            if (!checkDuplicate(baseItemInfo) || isCallaContact()) {
                                checkPermissionAndSaveNumber();
                            }
                            return;
                        }

                        // 处理普通的事件
                        TextView tv = (TextView) view.findViewById(R.id.functions_item);

                        if (mAction == Constant.GESTURE_DOUBLE_CLICK) {
                            // 处理 双击手势事件
                            if (mIsMiniMode) {
                                PreferenceUtils.setMiniGestureDoubleClick(tv.getText().toString());
                                PreferenceUtils.setMiniGestureDoubleClickAction(mDataLists.get(Id));
                                PreferenceUtils.setMiniGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            } else {
                                PreferenceUtils.setNormalGestureDoubleClick(tv.getText().toString());
                                PreferenceUtils.setNormalGestureDoubleClickAction(mDataLists.get(Id));
                                PreferenceUtils.setNormalGestureDoubleClickType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            }
                        } else if (mAction == Constant.GESTURE_LONG_CLICK) {
                            // 处理 长按手势 事件
                            if (mIsMiniMode) {
                                PreferenceUtils.setMiniGestureLongPress(tv.getText().toString());
                                PreferenceUtils.setMiniGestureLongPressAction(mDataLists.get(Id));
                                PreferenceUtils.setMiniGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            } else {
                                PreferenceUtils.setNormalGestureLongPress(tv.getText().toString());
                                PreferenceUtils.setNormalGestureLongPressAction(mDataLists.get(Id));
                                PreferenceUtils.setNormalGestureLongPressType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            }
                        } else if (mAction == Constant.GESTURE_SHORT_DRAG) {
                            // 处理 短拉手势 事件
                            if (mIsMiniMode) {
                                PreferenceUtils.setMiniGestureShortDrag(tv.getText().toString());
                                PreferenceUtils.setMiniGestureShortDragAction(mDataLists.get(Id));
                                PreferenceUtils.setMiniGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            } else {
                                PreferenceUtils.setNormalGestureShortDrag(tv.getText().toString());
                                PreferenceUtils.setNormalGestureShortDragAction(mDataLists.get(Id));
                                PreferenceUtils.setNormalGestureShortDragType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            }
                        } else if (mAction == Constant.NOW_KEY_ACTION) {
                            // 处理 普通事件
                            BaseItemInfo baseItemInfo = new BaseItemInfo();
                            baseItemInfo.setIndex(mItemIndex);
                            baseItemInfo.setType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
                            baseItemInfo.setKey_word(mDataLists.get(Id));

                            // 先判断是否已经存在 call a contact,如果存在了，就不处理。
                            if (checkDuplicate(baseItemInfo)) {
                                return;
                            }
                            if (mMenuOperate == Constant.NOW_KEY_MENU_OPERATE_UPDATE) {
                                // 更新转盘的菜单项
                                NowKeyPanelModel.getInstance().replaceItem(baseItemInfo, mPage, false);
                            } else {
                                // 默认处理方式
                                if (mExternal) {
                                    NowKeyPanelModel.getInstance().addItemExternal(baseItemInfo, mPage);
                                } else {
                                    NowKeyPanelModel.getInstance().addItem(baseItemInfo, mPage);
                                }
                            }
                        }
                        finish();
                    }
                }
        );
    }

    /**
     * 检查是否已经存在
     *
     * @param baseItemInfo
     * @return
     */
    private boolean checkDuplicate(BaseItemInfo baseItemInfo) {
        // 如果是手势的话，就不判断重复了
        if (mAction != Constant.NOW_KEY_ACTION) {
            return false;
        }
        try {
            if (mIsMiniMode) {
                ArrayList<BaseItemInfo> infos = NowKeyPanelModel.getInstance().loadMiniData();
                if (infos == null || infos.contains(baseItemInfo)) {
                    return true;
                }
            } else if (!mIsMiniMode) {
                ArrayList<BaseItemInfo> infos1 = NowKeyPanelModel.getInstance().loadData(1);
                ArrayList<BaseItemInfo> infos2 = NowKeyPanelModel.getInstance().loadData(2);
                if (infos1 == null || infos2 == null || infos1.contains(baseItemInfo) || infos2.contains(baseItemInfo)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        /*if (mAction == Constant.NOW_KEY_ACTION) {
            if (mExternal) {
                NowKeyPanelModel.getInstance(mContext).addItemExternal(null, mPage);
            } else {
                NowKeyPanelModel.getInstance(mContext).addItem(null, mPage);
            }
        }*/
        finish();
    }

    /**
     * 检查用户是否是选择了 callacontact 菜单，从而进入 FunctionActivity的
     *
     * @return
     */
    private boolean isCallaContact() {
        if(mKeyWord != null && "callacontact".equals(mKeyWord)){
            Log.d(TAG,"用户选择了 callacontact");
            return true;
        }else{
            Log.d(TAG,"用户不是选择了 callacontact");
            return false;
        }
    }

    /**
     * 适配器
     */
    class FunctionsAdapter extends BaseAdapter {

        FunctionsAdapter() {
            if (mIsMiniMode) {
                ArrayList<BaseItemInfo> items = NowKeyPanelModel.getInstance().loadMiniData();
                mAddedInfos = new ArrayList<>();
                if (items != null) {
                    mAddedInfos.addAll(items);
                }
            } else {
                ArrayList<BaseItemInfo> page1 = NowKeyPanelModel.getInstance().loadData(1);
                ArrayList<BaseItemInfo> page2 = NowKeyPanelModel.getInstance().loadData(2);
                mAddedInfos = new ArrayList<>();
                if (page1 != null) {
                    mAddedInfos.addAll(page1);
                }
                if (page2 != null) {
                    mAddedInfos.addAll(page2);
                }
            }
        }

        @Override
        public int getCount() {
            if (mDataLists != null) {
                return mDataLists.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return mDataLists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem itemHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.now_key_functions_list_item, null, false);
                itemHolder = new ViewHolderItem();
                itemHolder.tvItem = (TextView) convertView.findViewById(R.id.functions_item);
                itemHolder.imgItem = (ImageView) convertView.findViewById(R.id.functions_item_icon);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ViewHolderItem) convertView.getTag();
            }

            String item = mDataLists.get(position);
            FunctionItemInfo info = FunctionUtils.functionFilter(mContext, item);
            if (info != null) {
                Drawable d = info.getIcon();
                d.setTint(getColor(R.color.nowkey_color));
                itemHolder.tvItem.setTextColor(getColor(R.color.setting_title_text_color));
                itemHolder.imgItem.setImageDrawable(d);
                if (mAction == Constant.NOW_KEY_ACTION) {
                    for (BaseItemInfo added : mAddedInfos) {
                        // 把用户已经选择过的条目置灰
                        if (added.getKey_word().equals(item)) {
                            // 特殊情况：如果用户选择的是 callacontact 则不置灰，用户再次点击，可以进入选择电话号码的界面。
                            if("callacontact".equals(item) && isCallaContact()){
                                continue;
                            }
                            d.setTint(Color.LTGRAY);
                            itemHolder.imgItem.setImageDrawable(d);
                            itemHolder.tvItem.setTextColor(Color.LTGRAY);
                            break;
                        }
                    }
                }
                if ("callacontact".equals(item)) {
                    itemHolder.tvItem.setText(R.string.now_key_function_alcatel_callacontact);
                } else {
                    itemHolder.tvItem.setText(info.getText());
                }
            }
            return convertView;
        }
    }

    private static class ViewHolderItem {
        private TextView tvItem;
        private ImageView imgItem;
    }

    /**
     * 检查添加电话号码的权限，然后保存电话号码
     */
    private void checkPermissionAndSaveNumber() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            // 申请授权。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_READ_CONTACT_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, Constant.GET_CONTACT_REQUEST);
            //startActivityForResult(contact, 0);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_READ_CONTACT_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED) {
                finish();
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, Constant.GET_CONTACT_REQUEST);
            }
        }
    }
}
