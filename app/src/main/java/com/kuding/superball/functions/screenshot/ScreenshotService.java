package com.kuding.superball.functions.screenshot;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.kuding.superball.NowKeyApplication;
import com.kuding.superball.Utils.PreferenceUtils;
import com.kuding.superball.floatview.FloatingBallController;
import com.kuding.superball.floatview.MiniBallController;

import java.nio.ByteBuffer;

/**
 * Created by user on 17-1-24.
 */

public class ScreenshotService extends Service {

    private static Intent mResultData = null;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private ImageReader mImageReader;
    private WindowManager mWindowManager;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setResultData(Intent intent) {
        mResultData = intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startScreenShot();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startScreenShot() {
        if (PreferenceUtils.isMiniMode(false)) {
            MiniBallController.getController(getApplication()).removeAllViews();
        } else {
            FloatingBallController.getController(getApplication()).removeAllViews();
        }
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        createImageReader();

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                startVirtual();
            }
        }, 50);

        handler1.postDelayed(new Runnable() {
            public void run() {
                //capture the screen
                startCapture();

            }
        }, 200);
    }


    private void createImageReader() {

        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            mMediaProjection =
                    getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void startCapture() {
        try {
            Image image = mImageReader.acquireLatestImage();

            if (image == null) {
                startScreenShot();
            } else {
                SaveTask mSaveTask = new SaveTask();
                AsyncTaskCompat.executeParallel(mSaveTask, image);
            }
        } catch (Exception e) {

        }
    }


    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();

            return bitmap;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //预览图片
            if (bitmap != null) {
                ((NowKeyApplication) getApplication()).setmScreenCaptureBitmap(bitmap);

                new GlobalScreenshot(getApplicationContext()).takeScreenshot(
                        new Runnable() {
                            @Override
                            public void run() {
                                stopSelf();
                                if (PreferenceUtils.isMiniMode(false)) {
                                    MiniBallController.getController(getApplication()).showFloatBall();
                                } else {
                                    FloatingBallController.getController(getApplication()).showFloatBall();
                                }
                            }
                        }, true, true);
            }
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVirtual();
        tearDownMediaProjection();
    }
}
