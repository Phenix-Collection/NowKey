package com.kuding.nowkey.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kuding.nowkey.Utils.PreferenceUtils;
import com.kuding.nowkey.floatview.FloatingBallController;
import com.kuding.nowkey.floatview.MiniBallController;
import com.kuding.nowkey.INowkeyRemoteService;


/**
 * Nowkey的远程服务
 */
public class NowkeyRemoteService extends Service {
    private static final String TAG = "NowkeyRemoteService";

    private final INowkeyRemoteService.Stub mBinder = new INowkeyRemoteService.Stub() {

        @Override
        public void hideNowkeyFloatBall() throws RemoteException {
            if (PreferenceUtils.isShowNowKey(false)) {
                Intent intent = new Intent(NowkeyRemoteService.this, NowKeyService.class);
                intent.setAction(NowKeyService.HIDE_NOWEY_FLOATBALL);
                startService(intent);
            }
        }

        @Override
        public void showNowkeyFloatBall() throws RemoteException {
            if (PreferenceUtils.isShowNowKey(false)) {
                Intent intent = new Intent(NowkeyRemoteService.this, NowKeyService.class);
                intent.setAction(NowKeyService.SHOW_NOWEY_FLOATBALL);
                startService(intent);
            }
        }

        @Override
        public Rect getFloatBallRect() throws RemoteException {
            if (PreferenceUtils.isShowNowKey(false)) {
                if (PreferenceUtils.isMiniMode(true)) {
                    return MiniBallController.getController(getApplication()).getFloatBallPosition();
                } else {
                    return FloatingBallController.getController(getApplication()).getFloatBallPosition();
                }
            } else {
                return null;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "anxi NowkeyRemoteService onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "anxi NowkeyRemoteService onUnbind()");
        return super.onUnbind(intent);
    }
}
