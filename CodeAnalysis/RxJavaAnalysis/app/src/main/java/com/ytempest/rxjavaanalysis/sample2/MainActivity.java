package com.ytempest.rxjavaanalysis.sample2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.ytempest.rxjavaanalysis.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DeepAnalysisActivity";

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.iv_image);


        downloadByRxJava();


    }


    private void downloadByRxJava() {

        final String url1 = "http://pic60.nipic.com/file/20150207/11284670_083602732000_2.jpg";
        final String url2 = "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1211098591,1461282528&fm=27&gp=0.jpg";

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.e(TAG, "subscribe: ----> " + Thread.currentThread());
                emitter.onNext(url1);
                emitter.onComplete();
            }
        })
                .observeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
//                        Log.e(TAG, "第一个map: 当前线程 --> " + Thread.currentThread());
                        /*URL url = new URL(s);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        InputStream inputStream = urlConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);*/
                        return s;

                    }
                })
                /*.map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer bitmap) throws Exception {
                        Log.e(TAG, "第二个map: 当前线程 --> " + Thread.currentThread());

                        return bitmap + "";
                    }
                })*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(String bitmap) {
                        Log.e(TAG, "onNext: ");
                        Log.e(TAG, "onNext: 当前线程 --> " + Thread.currentThread());
//                        mImageView.setImageBitmap(bitmap);
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

}
