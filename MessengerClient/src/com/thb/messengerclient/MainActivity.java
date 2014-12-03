package com.thb.messengerclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    private static final String KEY = "data_key";
    private static final int MSG_TYPE_REGISTER = 1;
    private static final int MSG_TYPE_MESSAGE = 2;
    private static final int MSG_TYPE_RECEIVE_MSG = 3;

    private Button mSend;
    private EditText mEditContent;
    private TextView mReceiveMsg;

    private Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mServer = null;

    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mEditContent = (EditText) findViewById(R.id.edit_content);
        mReceiveMsg = (TextView) findViewById(R.id.txt_receive);
        mSend = (Button) findViewById(R.id.btn_send);
        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    sendMsgToServer();
            }
        });
    }

    private void sendMsgToServer() {
        Message message = Message.obtain(null, MSG_TYPE_MESSAGE);
        String content = mEditContent.getText().toString();
        if (null != content && content.trim().length() > 0) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY, mEditContent.getText().toString());
            message.setData(bundle);
        }
        try {
            mServer.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, "handleMessage() : err is " + e.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent("com.thb.messengerserver.messenger.service");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @SuppressLint("HandlerLeak")
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TYPE_RECEIVE_MSG:
                Bundle bundle = msg.getData();
                if (null != bundle) {
                    String content = getString(R.string.msg_receive) + bundle.getString(KEY);
                    mReceiveMsg.setText(content);
                }
                break;

            default:
                super.handleMessage(msg);
                break;
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mServer = null;
            Log.d(TAG, "onServiceDisconnected() : disconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() : connected");
            mBound = true;
            mServer = new Messenger(service);

            Message msg = Message.obtain(null, MSG_TYPE_REGISTER);
            msg.replyTo = mMessenger;
            try {
                mServer.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "onServiceConnected() : err is " + e.toString());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            mBound = false;
            unbindService(mConnection);
        }
    }

}
