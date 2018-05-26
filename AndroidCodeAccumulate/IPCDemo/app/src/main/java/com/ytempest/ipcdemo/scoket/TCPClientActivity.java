package com.ytempest.ipcdemo.scoket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ytempest.ipcdemo.R;
import com.ytempest.ipcdemo.scoket.service.TCPServerService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPClientActivity extends AppCompatActivity {

    private static final String TAG = "BinderPoolActivity";

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;

    /**
     * 服务端的输出流
     */
    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 客户端接受到服务端回复的信息，现在将信息显示出来
                case MESSAGE_RECEIVE_NEW_MSG: {
                    mMessageTextView.setText(mMessageTextView.getText()
                            + (String) msg.obj);
                    break;
                }

                // 成功连接到服务端，设置发送按钮可用
                case MESSAGE_SOCKET_CONNECTED: {
                    mSendButton.setEnabled(true);
                    break;
                }

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_client);

        mSendButton = findViewById(R.id.bt_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mMessageEditText.getText().toString();
                if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {

                    mPrintWriter.println(msg);
                    mMessageEditText.setText("");
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "self " + time + ":" + msg + "\n";
                    mMessageTextView.setText(mMessageTextView.getText() + showedMsg);
                }
            }
        });

        mMessageTextView = findViewById(R.id.tv_context);
        mMessageEditText = findViewById(R.id.et_edit);

        // 启动服务端
        Intent service = new Intent(this, TCPServerService.class);
        startService(service);

        new Thread() {
            @Override
            public void run() {
                // 开始连接到服务端
                connectTCPServer();
            }

        }.start();
    }

    private String formatDateTime(long l) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(l));
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                // 关闭客户端的socket的输出流
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    /**
     * 该方法会连接到服务端
     */
    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                // 创建一个 Socket连接到 8688端口
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;

                // 获取服务端的输出流，设置true表示让 PrintWriter自动刷新
                mPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                // 连接成功后做一些设置
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                Log.e(TAG, "connect server success ");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // 连接失败就睡眠1秒然后重试
                SystemClock.sleep(1000);
                Log.e(TAG, "connect tcp server failed, retry...");
            }
        }

        try {
            // 获取服务端的输出流
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            // 如果TCPClientActivity还没有finish，即客户端还没有关闭
            while (!TCPClientActivity.this.isFinishing()) {
                // 获取服务端回复的信息
                String msg = br.readLine();
                Log.e(TAG, "receive : " + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "server " + time + ":" + msg + "\n";
                    // 发送给 Handler，交给它进行显示
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg)
                            .sendToTarget();
                }
            }
            Log.e(TAG, "quit...");
            mPrintWriter.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

