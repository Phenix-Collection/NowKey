package com.kuding.superball.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

import com.kuding.superball.R;
import com.kuding.superball.functions.FunctionAlexa;
import com.kuding.superball.functions.FunctionBluetooth;
import com.kuding.superball.functions.FunctionFlightMode;
import com.kuding.superball.functions.FunctionSpilitScreen;
import com.kuding.superball.functions.FunctionWifi;
import com.kuding.superball.functions.flashlight.FunctionLight;
import com.kuding.superball.functions.rotate.FunctionRotation;
import com.kuding.superball.functions.sound.FunctionSound;
import com.kuding.superball.info.FunctionItemInfo;

/**
 * Created by user on 17-1-22.
 */

public class FunctionUtils {

    public static FunctionItemInfo functionFilter(Context context, String key_word) {
        if (key_word == null) return null;
        String title = null;
        VectorDrawable vd = null;
        Drawable d = context.getDrawable(R.mipmap.ic_launcher);
        String item = key_word;
        FunctionItemInfo fInfo = null;
        if ("none".equals(item)) {
            title = context.getString(R.string.action_none);
            vd = (VectorDrawable) context.getDrawable(R.drawable.ic_none);
            fInfo = new FunctionItemInfo.FunctionNone(context);
        } else if ("apps".equals(item)) {
            title = context.getString(R.string.now_key_function_apps_item);
            vd = (VectorDrawable) context.getDrawable(R.drawable.apps);
            fInfo = new FunctionItemInfo(context);
        } else if ("home".equals(item)) {
            title = context.getString(R.string.now_key_function_operation_home);
            vd = (VectorDrawable) context.getDrawable(R.drawable.home);
            fInfo = new FunctionItemInfo.FunctionHome(context);
        } else if ("recent".equals(item)) {
            title = context.getString(R.string.now_key_function_operation_recent);
            vd = (VectorDrawable) context.getDrawable(R.drawable.recent);
            fInfo = new FunctionItemInfo.FunctionRecent(context);
        }  else if ("back".equals(item)) {
            title = context.getString(R.string.now_key_function_operation_back);
            vd = (VectorDrawable) context.getDrawable(R.drawable.ic_back);
            fInfo = new FunctionItemInfo.FunctionBack(context);
        }  else if ("splitscreen".equals(item)) {
            title = context.getString(R.string.now_key_function_operation_splitscreen);
            vd = (VectorDrawable) context.getDrawable(R.drawable.split_screen_gray);
            fInfo = new FunctionSpilitScreen(context);
        } else if ("bluetooth".equals(item)) {
            title = context.getString(R.string.now_key_function_function_bluetooth);
            vd = (VectorDrawable) context.getDrawable(R.drawable.bluetooth);
            fInfo = new FunctionBluetooth(context);
        } else if ("light".equals(item)) {
            title = context.getString(R.string.now_key_function_function_light);
            vd = (VectorDrawable) context.getDrawable(R.drawable.torch);
            fInfo = new FunctionLight(context);
        } else if ("network".equals(item)) {
            title = context.getString(R.string.now_key_function_function_network);
            d = context.getDrawable(R.drawable.ic_qs_mobile_white);
            fInfo = new FunctionItemInfo.FunctionNetwork(context);
        } else if ("wifi".equals(item)) {
            title = context.getString(R.string.now_key_function_function_wifi);
            vd = (VectorDrawable) context.getDrawable(R.drawable.wifi);
            fInfo = new FunctionWifi(context);
        } else if ("rotation".equals(item)) {
            title = context.getString(R.string.now_key_function_function_rotation);
            vd = (VectorDrawable) context.getDrawable(R.drawable.screen_rotation);
            fInfo = new FunctionRotation(context);
        } else if ("gps".equals(item)) {
            title = context.getString(R.string.now_key_function_function_gps);
            vd = (VectorDrawable) context.getDrawable(R.drawable.ic_signal_location_disable);
            fInfo = new FunctionItemInfo.FunctionGps(context);
        } else if ("flightmode".equals(item)) {
            title = context.getString(R.string.now_key_function_function_flightmode);
            vd = (VectorDrawable) context.getDrawable(R.drawable.airplanemode_1);
            fInfo = new FunctionFlightMode(context);
        } else if ("settings".equals(item)) { // system settings
            title = context.getString(R.string.now_key_function_function_setting);
            vd = (VectorDrawable) context.getDrawable(R.drawable.settings);
            fInfo = new FunctionItemInfo.FunctionSettings(context);
        } else if ("sound".equals(item)) {
            title = context.getString(R.string.now_key_function_function_sound);
            vd = (VectorDrawable) context.getDrawable(R.drawable.ic_volume_ringer);
            fInfo = new FunctionSound(context);
        } else if ("lock".equals(item)) {
            title = context.getString(R.string.now_key_function_function_lock);
            vd = (VectorDrawable) context.getDrawable(R.drawable.lock);
            fInfo = new FunctionItemInfo.FunctionLock(context);
        } else if ("notification".equals(item)) {
            title = context.getString(R.string.now_key_function_function_notification);
            vd = (VectorDrawable) context.getDrawable(R.drawable.noti);
            fInfo = new FunctionItemInfo.FunctionNotification(context);
        } else if ("display".equals(item)) {
            title = context.getString(R.string.now_key_function_function_display);
            vd = (VectorDrawable) context.getDrawable(R.drawable.display);
            fInfo = new FunctionItemInfo.FunctionDisplay(context);
        } else if ("sync".equals(item)) {
            title = context.getString(R.string.now_key_function_function_sync);
            vd = (VectorDrawable) context.getDrawable(R.drawable.sync);
            fInfo = new FunctionItemInfo.FunctionSync(context);
        } else if ("alarm".equals(item)) {
            title = context.getString(R.string.now_key_function_tool_alarm);
            vd = (VectorDrawable) context.getDrawable(R.drawable.alarm);
            fInfo = new FunctionItemInfo.FunctionAlarm(context);
        } else if ("camera".equals(item)) {
            title = context.getString(R.string.now_key_function_tool_camera);
            vd = (VectorDrawable) context.getDrawable(R.drawable.camera);
            fInfo = new FunctionItemInfo.FunctionCamera(context);
        } else if ("nsettings".equals(item)) { // now key settings
            title = context.getString(R.string.application_name);
            vd = (VectorDrawable) context.getDrawable(R.drawable.ic_super_ball);
            fInfo = new FunctionItemInfo.FunctionNSettings(context);
        } else if ("calculator".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.tct.calculator")
                    && !Utils.checkPackageExist(context, "com.android.calculator2")
                    && !Utils.checkPackageExist(context, "com.google.android.calculator")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_tool_calculator);
            vd = (VectorDrawable) context.getDrawable(R.drawable.calculator);
            fInfo = new FunctionItemInfo.FunctionCalculator(context);
        } else if ("calendar".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.tct.calendar")
                    && !Utils.checkPackageExist(context, "com.android.calendar")
                    && !Utils.checkPackageExist(context, "com.google.android.calendar")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_tool_calendar);
            vd = (VectorDrawable) context.getDrawable(R.drawable.calendar);
            fInfo = new FunctionItemInfo.FunctionCalendar(context);
        } else if ("call".equals(item)) {
            title = context.getString(R.string.now_key_function_phone_call);
            vd = (VectorDrawable) context.getDrawable(R.drawable.call);
            fInfo = new FunctionItemInfo.FunctionCall(context);
        } else if ("chat".equals(item)) {
            title = context.getString(R.string.now_key_function_phone_chat);
            vd = (VectorDrawable) context.getDrawable(R.drawable.message);
            fInfo = new FunctionItemInfo.FunctionChat(context);
        } else if ("recentcontact".equals(item)) {
            title = context.getString(R.string.now_key_function_phone_recentcontact);
            vd = (VectorDrawable) context.getDrawable(R.drawable.recent_contact);
            fInfo = new FunctionItemInfo.FunctionRecentContact(context);
        } else if ("contact".equals(item)) {
            title = context.getString(R.string.now_key_function_phone_contact);
            vd = (VectorDrawable) context.getDrawable(R.drawable.contact);
            fInfo = new FunctionItemInfo.FunctionContact(context);
        } else if ("selfie".equals(item)) {
            title = context.getString(R.string.now_key_function_alcatel_selfie);
            vd = (VectorDrawable) context.getDrawable(R.drawable.selfie);
            fInfo = new FunctionItemInfo.FunctionSelfie(context);
        } else if ("micorvideo".equals(item)) {
            title = context.getString(R.string.now_key_function_alcatel_microvideo);
            vd = (VectorDrawable) context.getDrawable(R.drawable.video);
            fInfo = new FunctionItemInfo.FunctionVideo(context);
        } else if ("googlevoice".equals(item)) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VOICE_ASSIST");
            if (!Utils.checkIntentAvailable(context, intent)) {
                return null;
            }
            title = context.getString(R.string.now_key_function_alcatel_googlevoice);
            vd = (VectorDrawable) context.getDrawable(R.drawable.google_search);
            fInfo = new FunctionItemInfo.FunctionGoogleVoice(context);
        } else if ("event".equals(item)) {
            title = context.getString(R.string.now_key_function_alcatel_event);
            vd = (VectorDrawable) context.getDrawable(R.drawable.event);
            fInfo = new FunctionItemInfo.FunctionEvent(context);
        } else if ("email".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.tct.email")
                    && !Utils.checkPackageExist(context, "com.google.android.gm")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_alcatel_email);
            vd = (VectorDrawable) context.getDrawable(R.drawable.email);
            fInfo = new FunctionItemInfo.FunctionEmail(context);
        } else if ("musicplaylist".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.alcatel.music5")
                    && !Utils.checkPackageExist(context, "com.google.android.music")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_alcatel_musicplaylist);
            vd = (VectorDrawable) context.getDrawable(R.drawable.music_playlist);
            fInfo = new FunctionItemInfo.FunctionPlayList(context);
        } else if ("recognisesongs".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.shazam.android")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_alcatel_recognisesongs);
            vd = (VectorDrawable) context.getDrawable(R.drawable.recognizesongs);
            fInfo = new FunctionItemInfo.FunctionRecogniseSongs(context);
        } else if ("navigatehome".equals(item)) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    "com.android.settings.func.NavigateHomeSettingsActivity");
            if (!Utils.checkIntentAvailable(context, intent)) {
                return null;
            }
            title = context.getString(R.string.now_key_function_alcatel_navigationhome);
            vd = (VectorDrawable) context.getDrawable(R.drawable.navigation_home);
            fInfo = new FunctionItemInfo.FunctionNavigationHome(context);
        } else if ("timer".equals(item)) {
            title = context.getString(R.string.now_key_function_alcatel_timer);
            vd = (VectorDrawable) context.getDrawable(R.drawable.timer);
            fInfo = new FunctionItemInfo.FunctionTimer(context);
        } else if ("soundrecording".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.tct.soundrecorder")
                    && !Utils.checkPackageExist(context, "com.android.speechrecorder")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_alcatel_soundrecording);
            vd = (VectorDrawable) context.getDrawable(R.drawable.recorder);
            fInfo = new FunctionItemInfo.FunctionRecoder(context);
        } else if ("callacontact".equals(item)) {
            String s = PreferenceUtils.getString(PreferenceUtils.NOW_KEY_CALL_A_CONTACT_ITEM, "");
            if (!"".equals(s)) {
                title = s.split(",")[0];
            } else {
                title = context.getString(R.string.now_key_function_alcatel_callacontact);
            }
            vd = (VectorDrawable) context.getDrawable(R.drawable.call_a_contact);
            fInfo = new FunctionItemInfo.FunctionCallAContact(context);
        } else if ("screenshot".equals(item)) {
            title = context.getString(R.string.now_key_function_function_screenshot);
            vd = (VectorDrawable) context.getDrawable(R.drawable.screenshot);
            fInfo = new FunctionItemInfo.FunctionScreenshot(context);
        } else if ("gallery".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.android.gallery")
                    && !Utils.checkPackageExist(context, "com.android.gallery3d")
                    && !Utils.checkPackageExist(context, "com.tct.gallery3d")
                    && !Utils.checkPackageExist(context, "com.google.android.apps.photos")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_function_gallery);
            vd = (VectorDrawable) context.getDrawable(R.drawable.gallery_icon);
            fInfo = new FunctionItemInfo.FunctionGallery(context);
        } else if ("addacontact".equals(item)) {
            title = context.getString(R.string.now_key_function_function_addacontact);
            vd = (VectorDrawable) context.getDrawable(R.drawable.add_contacts_icon);
            fInfo = new FunctionItemInfo.FunctionAddAContact(context);
        } else if ("colorcatcher".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.tct.colorcatcher")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_function_colorcatcher);
            vd = (VectorDrawable) context.getDrawable(R.drawable.colorcatcher_icon);
            fInfo = new FunctionItemInfo.FunctionColorCatcher(context);
        } else if ("booster".equals(item)) {
            String boosterPkg = "com.tct.onetouchbooster";
            if (!Utils.checkPackageExist(context, boosterPkg)) {
                return null;
            }
            //modify by yanghzong.gong for defect-4887291 begin
            //title = context.getString(R.string.now_key_function_function_booster);
            vd = (VectorDrawable) context.getDrawable(R.drawable.booster_icon);
            try {
                PackageManager packageManager = context.getApplicationContext().getPackageManager();
                title = Utils.getApplicationName(boosterPkg, packageManager);
            } catch (Exception e){
                e.printStackTrace();
            }
            //modify by yanghzong.gong for defect-4887291 end
            fInfo = new FunctionItemInfo.FunctionBooster(context);
        } else if ("alexa".equals(item)) {
            if (!Utils.checkPackageExist(context, "com.amazon.alexa.avs.companion")) {
                return null;
            }
            title = context.getString(R.string.now_key_function_function_alexa);
            vd = (VectorDrawable) context.getDrawable(R.drawable.alexa);
            fInfo = new FunctionAlexa(context);
        }
        if (fInfo != null) {
            if (title != null) {
                fInfo.setText(title);
            }
            if (vd != null) {
                vd.setTint(Color.WHITE);
                fInfo.setIcon(vd);
            } else {
                // add by junye.li for defect 4270855 begin
                try {
                    d.setTint(Color.WHITE);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                // add by junye.li for defect 4270855 end
                fInfo.setIcon(d);
            }
        }
        return fInfo;
    }

    /**
     * @param type 0 for double click, 1 for long click , 2 for shortdrag
     * @return
     */
    public static FunctionItemInfo getCallAContactFromGesture(Context context, int type) {
        String title;
        String s = "";
        Drawable d = context.getDrawable(R.mipmap.ic_launcher);
        if (type == 0) {
            if (PreferenceUtils.isMiniMode(false)) {
                s = PreferenceUtils.getMiniGestureDoubleClickCall("");
            } else {
                s = PreferenceUtils.getNormalGestureDoubleClickCall("");
            }
        } else if (type == 1) {
            if (PreferenceUtils.isMiniMode(false)) {
                s = PreferenceUtils.getMiniGestureLongPressCall("");
            } else {
                s = PreferenceUtils.getNormalGestureLongPressCall("");
            }
        }else if (type == 2) {
            if (PreferenceUtils.isMiniMode(false)) {
                s = PreferenceUtils.getMiniGestureShortDragCall("");
            } else {
                s = PreferenceUtils.getNormalGestureShortDragCall("");
            }
        }
        if (!"".equals(s)) {
            title = s.split(",")[0];
        } else {
            title = context.getString(R.string.now_key_function_alcatel_callacontact);
        }
        VectorDrawable vd = (VectorDrawable) context.getDrawable(R.drawable.call_a_contact);
        FunctionItemInfo.FunctionCallAContact fInfo = new FunctionItemInfo.FunctionCallAContact(context);
        fInfo.setActionType(type);
        if (title != null) {
            fInfo.setText(title);
        }
        if (vd != null) {
            vd.setTint(Color.WHITE);
            fInfo.setIcon(vd);
        } else {
            fInfo.setIcon(d);
        }
        return fInfo;
    }

}
