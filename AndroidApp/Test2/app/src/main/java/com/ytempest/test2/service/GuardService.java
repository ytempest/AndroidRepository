package com.ytempest.test2.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.ytempest.test2.ProcessConnection;

import java.security.Guard;
import java.util.List;

public class GuardService extends Service {


    private static final String TAG = "GuardService";
    private final int GUARDID = 1;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "GuardService 连接了 MessageService ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(GuardService.this, MessageService.class));
            bindService(new Intent(GuardService.this, MessageService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    private ProcessConnection.Stub mProcessConnection = new ProcessConnection.Stub() {
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(GUARDID, new Notification());

        if (!serviceAlive(MessageService.class.getName())) {
            Log.e(TAG, "GuardService start MessageService ");
            startService(new Intent(GuardService.this, MessageService.class));
        }
        bindService(new Intent(GuardService.this, MessageService.class), mServiceConnection, Context.BIND_IMPORTANT);

        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mProcessConnection;
    }


    /**
     *
     * @param serviceName
     * @return
     */
    private boolean serviceAlive(String serviceName) {

        boolean isWork = false;

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(100);

        if (infos.size() <= 0) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo serviceInfo : infos) {
            String name = serviceInfo.service.getClassName().toString();
//            Log.e(TAG, "GuardService : serviceAlive: serviceName --> " + name);
            if (name.equals(serviceName)) {
                isWork = true;
            }
        }

        return isWork;
    }
}
