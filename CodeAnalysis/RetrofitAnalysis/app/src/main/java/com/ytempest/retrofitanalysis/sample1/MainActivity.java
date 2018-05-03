package com.ytempest.retrofitanalysis.sample1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ytempest.retrofitanalysis.R;
import com.ytempest.retrofitanalysis.sample1.deal.HttpCall;
import com.ytempest.retrofitanalysis.sample1.deal.Result;
import com.ytempest.retrofitanalysis.sample1.deal.UserInfoResult;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        });
    }
}
