package com.ytempest.ipcdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ytempest.ipcdemo.UserAidl;

public class MyService extends Service {

    private static final String TAG = "MyService";

    private final Messenger mMessenger = new Messenger(new MessengerHandler());
    

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();

    }

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: 收到信息");
            switch (msg.what) {
                case 100:
                    Messenger client = msg.replyTo;
                    // 给客户端发送消息
                    Message message = getClientMessage();
                    try {
                        client.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Bundle data = msg.getData();
                    String string = data.getString("msg", "");
                    Log.e(TAG, "handleMessage: 服务端收到消息 --> " + string);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        public Message getClientMessage() {
            Message message = Message.obtain(null, 200);
            Bundle bundle = new Bundle();
            bundle.putString("msg", "服务端已经收到你的消息了，你好");
            message.setData(bundle);
            return message;
        }
    }

}
