package com.ytempest.retrofitanalysis.sample2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ytempest.retrofitanalysis.R;
import com.ytempest.retrofitanalysis.sample2.deal.HttpCall;
import com.ytempest.retrofitanalysis.sample2.deal.Result;
import com.ytempest.retrofitanalysis.sample2.deal.UserInfoResult;


import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onlyRetrofit();
    }


    private void onlyRetrofit() {

        new Thread() {
            @Override
            public void run() {

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .client(okHttpClient)
                        .baseUrl("http://192.168.200.100/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                ApiService apiService = retrofit.create(ApiService.class);
                Call<Result<UserInfoResult>> call = apiService.getUserInfoByCall("dy", "123");
                call.enqueue(new HttpCall<UserInfoResult>() {
                    @Override
                    public void onSucceed(UserInfoResult result) {
                        Log.e(TAG, "onSucceed: current thread --> " + Thread.currentThread());
                        Log.e(TAG, "onSucceed: result --> " + result.toString());
                    }

                    @Override
                    public void onError(String code, String msg) {
                        Log.e(TAG, "onError: code:" + code + " , msg:" + msg);
                    }
                });
            }
        }.start();

    }

    private void retrofitAndRxJava() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl("http://192.168.200.100/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getUserInfo(new String[]{"dy"}, "123")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result<UserInfoResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Result<UserInfoResult> result) {
                        Log.e(TAG, "onNext:  result --> " + result.getData().toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
