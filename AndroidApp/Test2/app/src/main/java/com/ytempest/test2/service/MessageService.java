package com.ytempest.test2.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ytempest.test2.ProcessConnection;
import com.ytempest.test2.UserAidl;

import java.util.List;

/**
 * @author ytempest
 *         Description:
 */
public class MessageService extends Service {

    private static final String TAG = "MessageService";
    private final int MESSAGEID = 2;
    private final ProcessConnection.Stub mBinder = new ProcessConnection.Stub() {
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "MessageService 连接了 GuardService ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(MessageService.this, GuardService.class));

            bindService(new Intent(MessageService.this, GuardService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(MESSAGEID, new Notification());

        Log.e(TAG, "GuardService.class.getName() -->  " + GuardService.class.getName());

        if (!serviceAlive(GuardService.class.getName())) {
            Log.e(TAG, "MessageService start GuardService");
            startService(new Intent(MessageService.this, GuardService.class));
        }
        bindService(new Intent(MessageService.this, GuardService.class), mServiceConnection, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private boolean serviceAlive(String serviceName) {

        boolean isWork = false;

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(100);

        if (infos.size() <= 0) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo serviceInfo : infos) {
            String name = serviceInfo.service.getClassName().toString();
//            Log.e(TAG, "MessageService : serviceAlive: serviceName --> " + name);
            if (name.equals(serviceName)) {
                isWork = true;
            }
        }

        return isWork;
    }
}
