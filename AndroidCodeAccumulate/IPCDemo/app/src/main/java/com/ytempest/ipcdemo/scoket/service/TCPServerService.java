package com.ytempest.ipcdemo.scoket.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TCPServerService extends Service {

    private static final String TAG = "TCPServerService";

    private boolean mIsServiceDestroy = false;
    private String[] mDefineMessages = new String[]{
            "你好，哈哈",
            "请问你叫什么名字呀？",
            "今天天气不错",
            "据说爱笑的人都不会太差，不知道真假",
            "你是不是有点困"
    };

    public TCPServerService() {
    }

    @Override
    public void onCreate() {
        // 启动服务端，监听Socket的8688端口
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroy = true;
        super.onDestroy();
    }

    private class TcpServer implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                // 监听本地的8688端口
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                Log.e(TAG, "failed to establish port:8688 ");
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestroy) {
                try {
                    // accept()：这个方法会不断监听是否有客户端连接过来，如果有就返回客户端的
                    // Socket对象；这个监听过程是耗时的，只有当客户端连接过来，这个方法才会有
                    // 返回值，否则会一直阻塞
                    final Socket client = serverSocket.accept();
                    Log.e(TAG, "accept the connection of client");

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                // 回复客户端
                                responseClient(client);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void responseClient(Socket client) throws IOException {
            // 获取客户端的输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));

            // 获取客户端的输出流，设置 PrintWriter第二参数为true：表示当向这个流写入东西后会自动刷新
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(client.getOutputStream())), true);

            out.println("欢迎来到聊天室！");

            while (!mIsServiceDestroy) {
                // 获取客户端发送过来的数据
                String str = in.readLine();
                Log.e(TAG, "msg form client:" + str);
                if (str == null) {
                    break;
                }

                int i = new Random().nextInt(mDefineMessages.length);
                String msg = mDefineMessages[i];
                // 把回复信息写入客户端的输出流进而回复客户端
                out.println(msg);
                Log.e(TAG, "send: " + msg);
            }

            Log.e(TAG, "client quit.");

            // 回复结束后关闭流
            out.close();
            in.close();
            client.close();
        }

    }
}
