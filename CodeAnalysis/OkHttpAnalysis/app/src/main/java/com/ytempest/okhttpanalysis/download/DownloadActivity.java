package com.ytempest.okhttpanalysis.download;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ytempest.okhttpanalysis.R;
import com.ytempest.okhttpanalysis.download.task.DownloadCallback;
import com.ytempest.okhttpanalysis.download.task.DownloadDispatcher;
import com.ytempest.okhttpanalysis.download.task.FileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.login.LoginException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "HttpActivity";
    private TextView mTextView;
    private File mFile = new File(Environment.getExternalStorageDirectory(), "zhifubao.apk");
    private String mUrl = "http://gdown.baidu.com/data/wisegame/b913a2b8f661b52c/zhihu_663.apk";
    private String mUrlTwo = "http://gdown.baidu.com/data/wisegame/a2cd8828b227b9f9/neihanduanzi_692.apk";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int size = msg.arg1;
            int progress = msg.arg2;
            mTextView.setText(size + "\n" + progress);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mTextView = findViewById(R.id.text);

        FileManager.getInstance().init(this);
        FileManager.getInstance().setRootFile(Environment.getExternalStorageDirectory());

    }

    public void download(View view) {
        downloadByMultiThread();
    }

    public void pause(View view) {
        DownloadDispatcher.getDispatcher().stopDownload(mUrl);
    }

    public void cancel(View view) {
        DownloadDispatcher.getDispatcher().cancelDownload(mUrl);
    }

    public void broken(View view) {
        int a = 10 / 0;
    }

    public void single(View view) {
        downloadByThread();
    }

    /**
     * 使用多线程断点下载
     */
    private void downloadByMultiThread() {
        final long startTime = System.currentTimeMillis();
        DownloadDispatcher.getDispatcher().startDownload(mUrl, new DownloadCallback() {
            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onProgress(final long size, final long currentProgress) {

                Message message = Message.obtain(mHandler, 23, (int) size, (int) currentProgress);
                mHandler.sendMessage(message);

             /*   Log.e(TAG, "onProgress: ---------------------");
                Log.i(TAG, "onProgress: size     --> " + progress);
                Log.e(TAG, "onProgress: progress --> " + currentProgress);*/
            }

            @Override
            public void onSucceed(File file) {
                Log.e(TAG, "onSucceed: 多线程耗时：" + (System.currentTimeMillis() - startTime) / 1000);
                Log.e(TAG, "onSucceed: file --> " + file);
                installFile(file);
            }
        });
    }


    /**
     * 使用原生的OkHttp下载文件
     */
    private void downloadByThread() {
        final long startTime = System.currentTimeMillis();
        String url = mUrl;
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .eventListenerFactory(new EventListener.Factory() {
                    @Override
                    public EventListener create(Call call) {
                        return new EventListener() {
                            @Override
                            public void callStart(Call call) {
                                super.callStart(call);
                            }
                        };
                    }
                }).build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                Log.e(TAG, "onResponse: thread --> " + Thread.currentThread());
                writeToFile(inputStream, mFile);
                Log.e(TAG, "onSucceed: 单线程耗时：" + (System.currentTimeMillis() - startTime) / 1000);
            }
        });
    }


    /**
     * 将输入流的数据写到指定的file文件中
     */
    private void writeToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[2048];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        Log.e(TAG, "写入完成");
        inputStream.close();
        outputStream.close();
    }

    private void installFile(File file) {
        // 核心是下面几句代码
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

}
