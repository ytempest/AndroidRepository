package com.ytempest.ipcdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ytempest.ipcdemo.service.MyService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Messenger mRemoteMessenger;
    private Messenger mClientMessenger = new Messenger(new MessageHandler());


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteMessenger = new Messenger(service);
            Message message = Message.obtain(null, 100);
            Bundle bundle = new Bundle();
            bundle.putString("msg", "这条消息在客户端被创建");
            message.setData(bundle);
            message.replyTo = mClientMessenger;
            try {
                mRemoteMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }


    private static class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 200:
                    Bundle data = msg.getData();
                    String string = data.getString("msg", "");
                    Log.e(TAG, "客户端: 收到服务端的消息 --> " + string);
                    break;

                default:
                    break;

            }
        }
    }
}

