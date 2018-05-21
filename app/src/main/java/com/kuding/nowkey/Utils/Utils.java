package com.kuding.nowkey.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kuding.nowkey.info.AppItemInfo;
import com.kuding.nowkey.info.BaseItemInfo;
import com.kuding.nowkey.info.FunctionItemInfo;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 17-1-11.
 */

public class Utils {
    private static final String TAG = "Utils";

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int dip2px(Context context, int dip) {
        final float SCALE = context.getResources().getDisplayMetrics().density;
        float valueDips = dip;
        int valuePixels = (int) (valueDips * SCALE + 0.5f);
        return valuePixels;
    }

//    public static ArrayList<BaseItemInfo> filterBaseItemInfos(Context context,
//                                                              ArrayList<BaseItemInfo> baseItemInfos) {
//        ArrayList<BaseItemInfo> filted = new ArrayList<>();
//        for (BaseItemInfo baseInfo : baseItemInfos) {
//            if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_APP) {
//                // for app info
//                AppItemInfo aInfo = setAppInfo(context, baseInfo);
//                if (aInfo != null) {
//                    filted.add(baseInfo);
//                }
//            } else if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
//                // for function info
//                FunctionItemInfo fInfo = FunctionUtils.functionFilter(context, baseInfo.getKey_word());
//                if (fInfo != null) {
//                    filted.add(baseInfo);
//                }
//            }
//        }
//        return filted;
//    }

    public static ArrayList<Integer> filterBaseItemInfos(Context context,
                                                         ArrayList<BaseItemInfo> baseItemInfos) {
        ArrayList<BaseItemInfo> filtered = new ArrayList<>();
        ArrayList<Integer> emptyIndex = new ArrayList<>();
        for (BaseItemInfo baseInfo : baseItemInfos) {
            if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_APP) {
                // for app info
                AppItemInfo aInfo = setAppInfo(context, baseInfo);
                if (aInfo != null) {
                    filtered.add(baseInfo);
                } else {
                    emptyIndex.add(baseInfo.getIndex());
                }
            } else if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
                // for function info
                FunctionItemInfo fInfo = FunctionUtils.functionFilter(context, baseInfo.getKey_word());
                if (fInfo != null) {
                    filtered.add(baseInfo);
                } else {
                    emptyIndex.add(baseInfo.getIndex());
                }
            }
        }
        baseItemInfos.clear();
        baseItemInfos.addAll(filtered);

        return emptyIndex;
    }

    public static ArrayList<BaseItemInfo> convertBaseItemInfos(Context context,
                                                               ArrayList<BaseItemInfo> baseItemInfos) {
        ArrayList<BaseItemInfo> converted = new ArrayList<>();
        for (BaseItemInfo baseInfo : baseItemInfos) {
            if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_APP) {
                // for app info
                AppItemInfo aInfo = setAppInfo(context, baseInfo);
                if (aInfo != null) {
                    converted.add(aInfo);
                }
            } else if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
                // for function info
                FunctionItemInfo fInfo = FunctionUtils.functionFilter(context, baseInfo.getKey_word());
                if (fInfo != null) {
                    fInfo.setIndex(baseInfo.getIndex());
                    fInfo.setType(baseInfo.getType());
                    fInfo.setKey_word(baseInfo.getKey_word());
                    converted.add(fInfo);
                }
            }
        }
        return converted;
    }

    @Nullable
    public static BaseItemInfo convertBaseItemInfo(Context context, BaseItemInfo baseInfo) {
        if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_APP) {
            // for app info
            AppItemInfo aInfo = setAppInfo(context, baseInfo);
            if (aInfo != null) {
                return aInfo;
            }
        } else if (baseInfo.getType() == Constant.NOW_KEY_ITEM_TYPE_FUNCTION) {
            // for function info
            FunctionItemInfo info = FunctionUtils.functionFilter(context, baseInfo.getKey_word());
            if (info != null) {
                info.setIndex(baseInfo.getIndex());
                info.setKey_word(baseInfo.getKey_word());
                info.setType(baseInfo.getType());
                return info;
            }
        }
        return null;
    }

    public static AppItemInfo setAppInfo(Context context, BaseItemInfo baseItemInfo) {
        final PackageManager packageManager = context.getApplicationContext().getPackageManager();
        String pkgName = baseItemInfo.getKey_word();
        String applicationName = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkgName, 0);
            applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
            AppItemInfo aInfo = new AppItemInfo();
            aInfo.setKey_word(pkgName);
            aInfo.setIndex(baseItemInfo.getIndex());
            aInfo.setType(baseItemInfo.getType());
            aInfo.setText(applicationName);
            aInfo.setPackageName(pkgName);
            aInfo.setIcon(getApplicationIcon(applicationInfo, packageManager));
            aInfo.setIntent(getApplicationIntent(pkgName, packageManager));
            return aInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取应用的名称
     */
    public static String getApplicationName(String packageName, PackageManager packageManager) {
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
    public static Drawable getApplicationIcon(ApplicationInfo applicationInfo, PackageManager packageManager) {
        Drawable applicationIcon = applicationInfo.loadIcon(packageManager);
        return applicationIcon;
    }

    public static Intent getApplicationIntent(String packageName, PackageManager packageManager) {
        return packageManager.getLaunchIntentForPackage(packageName);
    }

    public static boolean checkPackageExist(Context context, String pkg) {
        final PackageManager packageManager = context.getApplicationContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkIntentAvailable(Context context, Intent intent) {
        // catch event that there's no activity to handle intent
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {  //存在
            return true;
        } else {    //不存在
            return false;
        }
    }

    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public static void showInfo(String tag, ArrayList itemInfos) {
        for (int i = 0; i < itemInfos.size(); i++) {
            Log.d(tag, "showInfo " + tag + " : " + itemInfos.get(i));
        }
    }
}
