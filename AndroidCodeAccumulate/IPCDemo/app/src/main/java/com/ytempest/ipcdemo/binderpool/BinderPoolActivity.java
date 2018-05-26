package com.ytempest.ipcdemo.binderpool;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ytempest.ipcdemo.R;
import com.ytempest.ipcdemo.binderpool.service.BinderPool;

public class BinderPoolActivity extends AppCompatActivity {

    private static final String TAG = "BinderPoolActivity";

    private TextView mTextView;
    private ISecurityCenter mSecurityCenter;
    private ICompute mCompute;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_pool);

        mTextView = findViewById(R.id.tv_context);
    }

    public void click(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取Binder连接池
                BinderPool binderPool = BinderPool.getInstance(BinderPoolActivity.this);
                // 通过Binder连接池获取指定的Binder对象
                IBinder securityBinder = binderPool.queryBinder(BinderPool.BINDER_SECURITY_CENTER);
                // 转型
                mSecurityCenter = ISecurityCenter.Stub.asInterface(securityBinder);

                String msg = "hello world - 安卓";

                try {
                    String encryptMsg = mSecurityCenter.encrypt(msg);
                    String decryptMsg = mSecurityCenter.decrypt(encryptMsg);
                    final String show = "文本：" + msg + "\n" +
                            "加密：" + encryptMsg + "\n" +
                            "解密：" + decryptMsg + "\n";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText(mTextView.getText().toString() + "\n" + show);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


                IBinder computerBinder = binderPool.queryBinder(BinderPool.BINDER_COMPUTE);
                mCompute = ICompute.Stub.asInterface(computerBinder);
                try{
                    final String show = "23 + 7 = " + mCompute.add(23, 7);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText(mTextView.getText().toString() + "\n\n" + show);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

