package com.kuding.nowkey;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.kuding.nowkey.Utils.Utils;
import com.kuding.nowkey.R;
import com.kuding.nowkey.functions.call.CallActivity;
import com.kuding.nowkey.functions.lock.LockActivity;
import com.kuding.nowkey.functions.recent.RecentActivity;
import com.kuding.nowkey.functions.rotate.RotateActivity;
import com.kuding.nowkey.functions.screenshot.ScreenshotActivity;

/**
 * Created by user on 17-2-4.
 */

public class ActionActivity extends Activity {

    public static final String ACTION_EXTRA = "action_extra";
    public static final String ACTION_EXTRA_PAGE = "action_extra_page";
    public static final String ACTION_EXTRA_INDEX = "action_extra_index";
    public static final String ACTION_EXTRA_ACTION = "action_extra_action";
    public static final int ACTION_RECENT = 1;
    public static final int ACTION_NETWORK = 2;
    public static final int ACTION_GPS = 3;
    public static final int ACTION_AIR_PLANE = 4;
    public static final int ACTION_SETTINGS = 5;
    public static final int ACTION_SOUND = 6;
    public static final int ACTION_LOCK = 7;
    public static final int ACTION_DISPLAY = 8;
    public static final int ACTION_SYNC = 9;
    public static final int ACTION_SCREEN_SHOT = 10;
    public static final int ACTION_ALARM = 11;
    public static final int ACTION_CAMERA = 12;
    public static final int ACTION_N_SETTINGS = 13;
    public static final int ACTION_CALCULATOR = 14;
    public static final int ACTION_CALENDAR = 15;
    public static final int ACTION_CALL = 16;
    public static final int ACTION_CHAT = 17;
    public static final int ACTION_RECENT_CONTACT = 18;
    public static final int ACTION_CONTACT = 19;
    public static final int ACTION_SELFIE = 20;
    public static final int ACTION_VIDEO = 21;
    public static final int ACTION_GOOGLE_VOICE = 22;
    public static final int ACTION_EVENT = 23;
    public static final int ACTION_EMAIL = 24;
    public static final int ACTION_PLAY_LIST = 25;
    public static final int ACTION_RECOGNISE_SONGS = 26;
    public static final int ACTION_NAVIGATION_HOME = 27;
    public static final int ACTION_TIMER = 28;
    public static final int ACTION_SOUND_RECORDER = 29;
    public static final int ACTION_CALL_A_CONTACT = 30;
    public static final int ACTION_GALLERY = 31;
    public static final int ACTION_ADD_A_CONTACT = 32;
    public static final int ACTION_BOOSTER = 33;
    public static final int ACTION_COLORCATCHER = 34;
    public static final int ACTION_ROTATION = 35;
    public static final int ACTION_ALEXA = 36;
    public static final int ACTION_BACK = 37;
    public static final int ACTION_SPLIT_SCREEN = 38;

    public static final int NAVIGATE_TYPE_CAR = 1;
    public static final int NAVIGATE_TYPE_TRANSIT = 2;
    public static final int NAVIGATE_TYPE_WALK = 3;

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
        handleIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void handleIntent() {
        Intent action = getIntent();
        String warning = null;
        ComponentName cn = null;
        if (action != null) {
            int extra = action.getIntExtra(ACTION_EXTRA, -1);
            Intent intent = new Intent();
            switch (extra) {
                case ACTION_RECENT:
                    intent.putExtra("recent_extra",1);
                    intent.setClass(this, RecentActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_BACK:
                    intent.putExtra("recent_extra",2);
                    intent.setClass(this, RecentActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SPLIT_SCREEN:
                    intent.putExtra("recent_extra",3);
                    intent.setClass(this, RecentActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_NETWORK:
                    cn = new ComponentName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity");
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_GPS:
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_AIR_PLANE:
                    intent.setAction(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SETTINGS:
                    intent.setAction(Settings.ACTION_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SOUND:
                    intent.setAction(Settings.ACTION_SOUND_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_LOCK:
                    intent.setClass(this, LockActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_DISPLAY:
                    intent.setAction(Settings.ACTION_DISPLAY_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SYNC:
                    intent.setAction(Settings.ACTION_SYNC_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SCREEN_SHOT:
                    intent.setClass(this, ScreenshotActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_ALARM:
                    intent.setAction(AlarmClock.ACTION_SHOW_ALARMS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_CAMERA:
                    intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_N_SETTINGS:
                    intent.setClass(this, NowKeyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_CALCULATOR:
                    if (Utils.checkPackageExist(this, "com.tct.calculator")) {
                        cn = new ComponentName("com.tct.calculator",
                                "com.tct.calculator.Calculator");
                    } else if (Utils.checkPackageExist(this, "com.android.calculator2")) {
                        cn = new ComponentName("com.android.calculator2",
                                "com.android.calculator2.Calculator");
                    } else if (Utils.checkPackageExist(this, "com.google.android.calculator")) {
                        cn = new ComponentName("com.google.android.calculator",
                                "com.android.calculator2.Calculator");
                    }
                    intent.setComponent(cn);
                    warning = getString(R.string.calculator_not_installed);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_CALENDAR:
                    startCalendar();
                    return;
                case ACTION_CALL:
                    intent.setAction(Intent.ACTION_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_CHAT:
                    Uri sms_uri = Uri.parse("smsto:");//设置号码
                    intent = new Intent(Intent.ACTION_SENDTO, sms_uri);//调用发短信Action
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_RECENT_CONTACT:
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setType("vnd.android.cursor.dir/calls");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_CONTACT:
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setType("vnd.android.cursor.dir/contact");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SELFIE:
                    startSelfie();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    return;
                case ACTION_VIDEO:
                    intent.setAction("android.media.action.VIDEO_CAPTURE");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_GOOGLE_VOICE:
                    intent.setAction("android.intent.action.VOICE_ASSIST");
                    warning = getString(R.string.google_voicd_not_installed);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_EVENT:
                    intent.setAction(Intent.ACTION_INSERT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_EMAIL:
                    Uri uri = Uri.parse("mailto:");
                    intent = new Intent(Intent.ACTION_SENDTO, uri);
                    if (Utils.checkPackageExist(this, "com.tct.email")) {
                        intent.setPackage("com.tct.email");
                    } else if (Utils.checkPackageExist(this, "com.google.android.gm")) {
                        intent.setPackage("com.google.android.gm");
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_PLAY_LIST:
                    if (Utils.checkPackageExist(this, "com.alcatel.music5")) {
                        cn = new ComponentName("com.alcatel.music5",
                                "com.alcatel.music5.activities.Music5WelcomeActivity");
                    } else if (Utils.checkPackageExist(this, "com.google.android.music")) {
                        cn = new ComponentName("com.google.android.music",
                                "com.android.music.activitymanagement.TopLevelActivity");
                    }
                    intent.setComponent(cn);
                    warning = getString(R.string.music_not_installed);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_RECOGNISE_SONGS:
                    intent.setAction("com.shazam.android.intent.actions.START_TAGGING");
                    warning = getString(R.string.shazam_not_installed);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_NAVIGATION_HOME:
                    int navigateType = Settings.System.getInt(getContentResolver(), "navigateType", NAVIGATE_TYPE_CAR);
                    String homeAddress = Settings.System.getString(getContentResolver(), "home_address");
                    String method = null;
                    if (navigateType == NAVIGATE_TYPE_CAR) {
                        method = "&mode=d";
                    } else if (navigateType == NAVIGATE_TYPE_TRANSIT) {
                        method = "&mode=transit";
                    } else {
                        method = "&mode=w";
                    }
                    if (homeAddress == null || homeAddress.equals("")) {
                        intent.setClassName("com.android.settings",
                                "com.android.settings.func.NavigateHomeSettingsActivity");
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=" + homeAddress + method));
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_TIMER:
                    intent.setAction(AlarmClock.ACTION_SET_TIMER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_SOUND_RECORDER:
                    startSoundRecorder();
                    return;
                case ACTION_CALL_A_CONTACT:
                    int index = action.getIntExtra(ACTION_EXTRA_INDEX, -1);
                    if (index != -1) {
                        intent.putExtra(CallActivity.ACTION_EXTRA_INDEX, index);
                    }
                    int page = action.getIntExtra(ACTION_EXTRA_PAGE, -1);
                    if (page != -1) {
                        intent.putExtra(CallActivity.ACTION_EXTRA_PAGE, page);
                    }
                    int actionType = action.getIntExtra(ACTION_EXTRA_ACTION, -1);
                    if (actionType != -1) {
                        intent.putExtra(CallActivity.ACTION_EXTRA_ACTION, actionType);
                    }
                    intent.setClass(this, CallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_GALLERY:
                    startGallery();
                    return;
                case ACTION_ADD_A_CONTACT:
                    startAddAContact();
                    return;
                case ACTION_BOOSTER:
                    try {
                        intent = Utils.getApplicationIntent("com.tct.onetouchbooster",
                                getPackageManager());
                        if (intent == null) {
                            intent = new Intent().setClassName("com.tct.onetouchbooster", "com.tct.onetouchbooster.ui.MainActivity");
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    } catch (Exception e){
                        Toast.makeText(this, getString(R.string.now_key_app_not_exist), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ACTION_COLORCATCHER:
                    // modify by junye.li for defect 4234415 begin
//                    cn = new ComponentName("com.tct.colorcatcher",
//                            "com.tct.colorcatcher.EnterThemeSelectActivity");
//                    intent.setComponent(cn);
                    intent = Utils.getApplicationIntent("com.tct.colorcatcher", getPackageManager());
                    // modify by junye.li for defect 4234415 end
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_ROTATION:
                    intent.setClass(this, RotateActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTION_ALEXA:
                    intent = Utils.getApplicationIntent("com.amazon.alexa.avs.companion",
                            getPackageManager());
                    if (intent != null) {
                    	//add by yangzhong.gong for defect-4398104 begin
                    	try {
                    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    	} catch (Exception e) {
                    		warning = getString(R.string.alexa_app_error);
                    	}
                    	//add by yangzhong.gong for defect-4398104 end
                    } else {
                        warning = getString(R.string.alexa_app_error);
                    }
                    break;
                default:
                    intent.setClass(this, NowKeyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;

            }

            if (intent != null) {
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (warning != null) {
                        Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (warning != null) {
                    Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
                }
            }

        }
        finish();
    }

    private void startCalendar() {
        if (Utils.checkPackageExist(this, "com.tct.calendar")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.tct.calendar",
                    "com.tct.calendar.AllInOneActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (Utils.checkPackageExist(this, "com.android.calendar")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.calendar",
                    "com.android.calendar.AllInOneActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (Utils.checkPackageExist(this, "com.google.android.calendar")) {
            //modify by yangzhong.gong for defect-4383202 begin
            try {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.google.android.calendar",
                        "com.android.calendar.AllInOneActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, R.string.calendar_not_installed, Toast.LENGTH_SHORT).show();
            }
            //modify by yangzhong.gong for defect-4383202 end
        } else {
            Toast.makeText(this, R.string.calendar_not_installed, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void startSelfie() {
        try {
            Intent intent = new Intent("com.tct.camera.STARTFRONTCAMERA");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent("android.media.action.STILL_IMAGE_CAMERA");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void startSoundRecorder() {
        if (Utils.checkPackageExist(this, "com.tct.soundrecorder")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.tct.soundrecorder",
                    "com.tct.soundrecorder.SoundRecorder");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (Utils.checkPackageExist(this, "com.android.speechrecorder")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.speechrecorder",
                    "com.android.speechrecorder.SpeechRecorderActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
//        Intent intent = new Intent();
//        intent.setClassName("com.tct.soundrecorder",
//                "com.tct.soundrecorder.SoundRecorderService");
//        intent.setAction("serviceaction");
//        intent.putExtra("cmd", "play");
//        startService(intent);
        finish();
    }

    private void startGallery() {
        if (Utils.checkPackageExist(this, "com.tct.gallery3d")) {
            Intent intent = Utils.getApplicationIntent("com.tct.gallery3d", getPackageManager());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (Utils.checkPackageExist(this, "com.google.android.apps.photos")) {
            try {
                Intent intent = Utils.getApplicationIntent("com.tct.gallery3d", getPackageManager());
                ComponentName cn = new ComponentName("com.google.android.apps.photos",
                        "com.google.android.apps.photos.gallery.GalleryActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.google.android.apps.photos",
                        "com.google.android.apps.photos.home.HomeActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } else if (Utils.checkPackageExist(this, "com.android.gallery")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.gallery",
                    "com.android.camera.GalleryPicker");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (Utils.checkPackageExist(this, "com.android.gallery3d")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.gallery3d",
                    "com.android.gallery3d.app.GalleryActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (Utils.checkPackageExist(this, "com.google.android.apps.photos")) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.google.android.apps.photos",
                    "com.google.android.apps.photos.gallery.GalleryActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void startAddAContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.dir/person");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.setType("vnd.android.cursor.dir/raw_contact");
        startActivity(intent);
        finish();
    }
}
