package com.thb.messengerserver;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MessengerService extends Service {
    private static final String TAG = "MessengerService";

    private static final int MSG_TYPE_REGISTER = 1;
    private static final int MSG_TYPE_MESSAGE = 2;
    private static final int MSG_TYPE_SEND_MSG = 3;
    private static final String KEY = "data_key";

    private Messenger mClientMessenger = null;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() : return messenger");
        return mMessenger.getBinder();
    }

    private Messenger mMessenger = new Messenger(new IncomingHandler());

    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TYPE_REGISTER:
                mClientMessenger = msg.replyTo;
                Log.d(TAG, "handleMessage() : register service");
                break;
            case MSG_TYPE_MESSAGE:
                Bundle bundle = msg.getData();
                if (null == bundle || null == bundle.getString(KEY)) {
                    Log.d(TAG, "handleMessage() : client send null");
                    String content = "who are you?";
                    sendMsgToClient(content);
                } else {
                    // bundle.getString(KEY) not null!!
                    Log.d(TAG, "handleMessage() : client send the msg of --- " + bundle.getString(KEY));
                    String content = "the msg is : " + bundle.getString(KEY);
                    sendMsgToClient(content);
                }
                break;

            default:
                super.handleMessage(msg);
                break;
            }

        }
    }

    private void sendMsgToClient(String content) {
        if (null != mClientMessenger) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY, content);
            Message msg = Message.obtain(null, MSG_TYPE_SEND_MSG);
            msg.setData(bundle);
            try {
                mClientMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "sendMsgToClient() : err is " + e.toString());
            }
        } else {
            Log.w(TAG, "sendMsgToClient() : mClientMessager is null !!!");
        }
    }
}
