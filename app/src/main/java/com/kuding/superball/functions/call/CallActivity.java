package com.kuding.superball.functions.call;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.kuding.superball.Utils.Constant;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.info.BaseItemInfo;
import com.kuding.superball.R;
import com.kuding.superball.floatview.NowKeyPanelModel;

import android.telephony.PhoneNumberUtils;//add by yangzhong.gong for defect-5064609

/**
 * Created by user on 17-1-25.
 */

public class CallActivity extends Activity {
    public static final String ACTION_EXTRA_INDEX = "action_extra_index";
    public static final String ACTION_EXTRA_PAGE = "action_extra_page";
    public static final String ACTION_EXTRA_ACTION = "action_extra_action";
    private static final int CALL_PHONE_REQUEST_CODE = 111;
    private static final int CONTACT_REQUEST_CODE = 112;
    private String mContactInfos;
    private int mIndex;
    private int mPage;
    private int mActionType;

    private boolean hasRun = false;
    private boolean mIsMiniMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.empty_layout);
        mIsMiniMode = PreferenceUtils.isMiniMode(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasRun) {
            hasRun = true;
            initContactInfo();
            if (!"".equals(mContactInfos)) {
                checkCallPermission();
            } else {
                checkContactPermission();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!hasRun) {
            hasRun = true;
            initContactInfo();
            if (!"".equals(mContactInfos)) {
                checkCallPermission();
            } else {
                checkContactPermission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasRun = false;
    }

    private void checkCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            // 申请授权。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_REQUEST_CODE);
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_SETTINGS)) {
//                // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
//            } else {
//                // 申请授权。
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        WRITE_STORAGE_PERMISSION_REQUEST_CODE);
//            }
        } else {
            call();
        }
    }

    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {// 没有权限。
            // 申请授权。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACT_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, Constant.GET_CONTACT_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CALL_PHONE_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {// 没有权限。
                finish();
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {
                call();
            }
        } else if (requestCode == CONTACT_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED) {// 没有权限。
                finish();
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, Constant.GET_CONTACT_REQUEST);
            }
        }
    }

    private void call() {
        if (!"".equals(mContactInfos)) {
            String[] contact = mContactInfos.split(",");
            if (contact.length <= 1) {
                String warning = contact[0] + " " + getString(R.string.now_key_call_a_contact_no_number);
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String[] numbers = contact[1].split("/");
                if (numbers != null) {
                    if (numbers.length > 1) {
                        showCommonListDialog(numbers);
                    } else {
                        //add by yangzhong.gong for defect-5064609 begin
                        try {
                            Intent phoneIntent;
                            if (!PhoneNumberUtils.isEmergencyNumber(numbers[0])) {
                                phoneIntent = new Intent("android.intent.action.CALL",
                                        Uri.parse("tel:" + numbers[0]));
                            } else {
                                phoneIntent = new Intent("android.intent.action.CALL_EMERGENCY",
                                        Uri.parse("tel:" + numbers[0]));
                            }
                            phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(phoneIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //add by yangzhong.gong for defect-5064609 end
                        finish();
                    }
                }
            }

        }
    }

    private void initContactInfo() {
        Intent intent = getIntent();
        if (intent != null) {
            mIndex = intent.getIntExtra(ACTION_EXTRA_INDEX, -1);
            mPage = intent.getIntExtra(ACTION_EXTRA_PAGE, -1);
            mActionType = intent.getIntExtra(ACTION_EXTRA_ACTION, -1);
        }
        if (mActionType == 0) {
            if (mIsMiniMode) {
                mContactInfos = PreferenceUtils.getMiniGestureDoubleClickCall("");
            } else {
                mContactInfos = PreferenceUtils.getNormalGestureDoubleClickCall("");
            }
        } else if (mActionType == 1) {
            if (mIsMiniMode) {
                mContactInfos = PreferenceUtils.getMiniGestureLongPressCall("");
            } else {
                mContactInfos = PreferenceUtils.getNormalGestureLongPressCall("");
            }
        } else if (mActionType == 2) {
            if (mIsMiniMode) {
                mContactInfos = PreferenceUtils.getMiniGestureShortDragCall("");
            } else {
                mContactInfos = PreferenceUtils.getNormalGestureShortDragCall("");
            }
        } else {
            if(mIsMiniMode){
                mContactInfos = PreferenceUtils.getCallContactItemMini("");
            }else{
                mContactInfos = PreferenceUtils.getCallContactItem("");
            }
        }
    }

    private void showCommonListDialog(final String[] numbers) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setItems(numbers, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String number = numbers[which];
                Intent phoneIntent = new Intent("android.intent.action.CALL",
                        Uri.parse("tel:" + number));
                phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(phoneIntent);
                finish();
            }
        }).create();
        alert.setCancelable(false);
        alert.show();
    }

    private static final String[] PHONE_COLUMNS = {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
    };

    private static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 0;
    private static final int PHONE_PHOTO_ID_COLUMN_INDEX = 1;
    private static final int PHONE_NUMBER_COLUMN_INDEX = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.GET_CONTACT_REQUEST) {
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
//
//                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.
//                            Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.
//                            Phone.CONTACT_ID + "=" + "?", new String[]{contactId}, null);
//                    if (phoneCursor.moveToFirst()) {
//                        do {
//                            String s = phoneCursor.getString(phoneCursor.getColumnIndex(
//                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
//                            phoneNumber.append(s);
//                            phoneNumber.append("/");
//                        } while (phoneCursor.moveToNext());
//                    }
//                    //  关闭查询手机号码的cursor
//                    phoneCursor.close();

                    name = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
                    mPhoneNumber = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
                    phoneNumber.append(mPhoneNumber);
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

    private void onFinishGetContact(String name, String phoneNumber) {
        if (mIndex == -1 || mPage == -1) return;
        if ("".equals(name)) name = phoneNumber;
        BaseItemInfo baseItemInfo = new BaseItemInfo();
        baseItemInfo.setIndex(mIndex);
        baseItemInfo.setType(Constant.NOW_KEY_ITEM_TYPE_FUNCTION);
        baseItemInfo.setKey_word("callacontact");
        if(mIsMiniMode){
            PreferenceUtils.setCallContactItemMini(name + "," + phoneNumber);
        }else{
            PreferenceUtils.setCallContactItem(name + "," + phoneNumber);
        }
        NowKeyPanelModel.getInstance().updateItem(baseItemInfo, mPage);
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
}
