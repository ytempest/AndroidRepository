package com.ytempest.ipcdemo.binderpool.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ytempest.ipcdemo.binderpool.IBinderPool;

import java.util.concurrent.CountDownLatch;


/**
 * @author ytempest
 *         Description：BinderPool 对所有的远程服务提供的Binder进行管理，通过BinderPool才能从
 *         远程服务中获取到相应的 Binder
 */
public class BinderPool {
    private static final String TAG = "BinderPool";

    private static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 1;
    public static final int BINDER_SECURITY_CENTER = 2;

    private Context mContext;
    /**
     * 远程服务的Binder，这个Binder充当连接池的作用
     */
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInstance;
    /**
     * 通过这个对象实现从Binder连接池中获取Binder这个一个操作变成一个同步操作
     */
    private CountDownLatch mConnectBinderPoolCountDownLatch;
    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: start");
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                // 为连接到远程服务的Binder设置死亡代理
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // 将锁存器的数量减一
            mConnectBinderPoolCountDownLatch.countDown();
            Log.e(TAG, "onServiceConnected: end");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // ignore
        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        /**
         * 当Binder绑定断开的时候会调用该方法
         */
        @Override
        public void binderDied() {
            Log.e(TAG, "binder dead.");
            // 移除Binder死亡代理
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);

            // 下面重新连接远程服务
            mBinderPool = null;
            connectBinderPoolService();
        }
    };


    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context);
                }
            }
        }

        return sInstance;
    }


    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;

        try {
            // 如果成功连接到了远程服务
            if (mBinderPool != null) {
                // 调用远程服务的方法获取相应的Binder
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return binder;
    }

    private synchronized void connectBinderPoolService() {
        // 创建一个数目为1的锁存器
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);

        Intent service = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(service, mBinderPoolConnection, Context.BIND_AUTO_CREATE);

        try {
            // 使当前线程在锁存器倒计数至零之前一直等待，除非线程被中断。
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Description：BinderPoolImpl 才是远程服务返回的Binder
     */

    public static class BinderPoolImpl extends IBinderPool.Stub {

        public BinderPoolImpl() {
            super();
        }

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode) {
                case BINDER_SECURITY_CENTER: {
                    binder = new SecurityCenterImpl();
                    break;
                }

                case BINDER_COMPUTE: {
                    binder = new ComputeImpl();
                    break;
                }

                default:
                    break;
            }

            return binder;
        }
    }

}
