package com.ytempest.rxjavaanalysis.sample1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.ytempest.rxjavaanalysis.R;
import com.ytempest.rxjavaanalysis.rxjava.Observable;
import com.ytempest.rxjavaanalysis.rxjava.Observer;
import com.ytempest.rxjavaanalysis.rxjava.map.Function;
import com.ytempest.rxjavaanalysis.rxjava.scheduler.Scheduler;
import com.ytempest.rxjavaanalysis.rxjava.scheduler.Schedulers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int SHOW_IMAGE = 100;
    private ImageView mImageView;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == SHOW_IMAGE) {
                Bitmap bitmap = (Bitmap) msg.obj;
                mImageView.setImageBitmap(bitmap);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.iv_image);

//        downloadInBackground();

//        downloadByRxJava();

        myselfRxJava();

    }

    private void myselfRxJava() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Observable.just("123456789")
                        .observeOn(Schedulers.io())
                        .map(new Function<String, Integer>() {
                            @Override
                            public Integer call(String s) {
                                Log.e(TAG, "第一个map: 当前线程 --> " + Thread.currentThread());
                                return s.length();
                            }
                        })
                        .observeOn(Schedulers.mainThread())
                        .map(new Function<Integer, String>() {
                            @Override
                            public String call(Integer integer) {
                                Log.e(TAG, "第二个map: 当前线程 --> " + Thread.currentThread());
                                return "I had know the length of string is " + integer;
                            }
                        })
                        .observeOn(Schedulers.io())
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onSubscribe() {
                                Log.e(TAG, "onSubscribe: ");
                            }

                            @Override
                            public void onNext(String s) {
                                Log.e(TAG, "第三个map: 当前线程 --> " + Thread.currentThread());
                                Log.e(TAG, "onNext: the value --> " + s);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: ");
                            }

                            @Override
                            public void onComplete() {
                                Log.e(TAG, "onComplete: ");
                            }
                        });

            }
        }).start();
    }


    private void downloadInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1211098591,1461282528&fm=27&gp=0.jpg");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Message message = Message.obtain();
                    message.arg1 = SHOW_IMAGE;
                    message.obj = bitmap;
                    mHandler.sendMessage(message);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
