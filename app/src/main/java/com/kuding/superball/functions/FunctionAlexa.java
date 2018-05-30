package com.kuding.superball.functions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.amazon.alexa.avs.tctvoiceact.ITctAlexaVoiceService;
import com.kuding.superball.info.FunctionItemInfo;

/**
 * Created by user on 17-2-5.
 */

public class FunctionAlexa extends FunctionItemInfo {
    private Context mContext;


    public FunctionAlexa(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onAdd(Context context) {
        super.onAdd(context);
    }

    @Override
    public void onDelete(Context context) {
        super.onDelete(context);
    }

    @Override
    public boolean doAction(Context context) {
        startAlexa();
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private ITctAlexaVoiceService mITctAlexaVoiceService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mITctAlexaVoiceService = ITctAlexaVoiceService.Stub.asInterface(service);
            try {
                mITctAlexaVoiceService.startSession();
            } catch (Exception e) {
                Log.d("AlexaVoiceService", "bindTctAlexaVoiceService e =" + e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mITctAlexaVoiceService = null;
        }
    };

    private void startAlexa() {
        if (mITctAlexaVoiceService == null) {
            Intent intent = new Intent(ITctAlexaVoiceService.class.getName());
            intent.setPackage("com.amazon.alexa.avs.companion");
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            try {
                mITctAlexaVoiceService.startSession();
            } catch (Exception e) {
                Log.d("AlexaVoiceService", "bindTctAlexaVoiceService e =" + e);
            }
        }
    }

}
