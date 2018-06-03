package com.ytempest.retrofitanalysis.sample3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ytempest.retrofitanalysis.R;
import com.ytempest.retrofitanalysis.sample3.deal.Result;
import com.ytempest.retrofitanalysis.sample3.deal.UserInfoResult;
import com.ytempest.retrofitanalysis.sample3.imitate.Call;
import com.ytempest.retrofitanalysis.sample3.imitate.Callback;
import com.ytempest.retrofitanalysis.sample3.imitate.Response;
import com.ytempest.retrofitanalysis.sample3.imitate.Retrofit;
import com.ytempest.retrofitanalysis.sample3.imitate.converter.gson.GsonConverterFactory;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.200.100/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Result<UserInfoResult>> call = apiService.getUserInfo("dy", "123");
        call.enqueue(new HttpCall<UserInfoResult>() {
            @Override
            public void onSucceed(UserInfoResult result) {
                Log.e(TAG, "onSucceed: result --> " + result.toString());
            }

            @Override
            public void onError(String code, String msg) {
                Log.e(TAG, "onError: code:" + code + " , msg:" + msg);
            }
        });*/
        onSelfRetrofit();
    }

    public void onSelfRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.200.100/")
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Result<UserInfoResult>> call = apiService.getUserInfo("dy", "123");
        call.enqueue(new Callback<Result<UserInfoResult>>() {
            @Override
            public void onResponse(Call<Result<UserInfoResult>> call, Response<Result<UserInfoResult>> response) {
                Result<UserInfoResult> body = response.body();
                Log.e(TAG, "onResponse: body --> " + body.getData());
            }

            @Override
            public void onFailure(Call<Result<UserInfoResult>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

}
