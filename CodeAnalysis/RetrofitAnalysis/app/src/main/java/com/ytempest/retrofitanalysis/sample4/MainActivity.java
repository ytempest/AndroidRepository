package com.ytempest.retrofitanalysis.sample4;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ytempest.retrofitanalysis.R;
import com.ytempest.retrofitanalysis.sample4.deal.HttpCall;
import com.ytempest.retrofitanalysis.sample4.deal.Result;
import com.ytempest.retrofitanalysis.sample4.deal.UserInfoResult;

import java.lang.reflect.Field;
import java.net.URL;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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


        // <-----------------     旧BaseUrl     -------------------->
        URL before = retrofit.baseUrl().url();
        Log.e(TAG, "before: url --> " + before);


        Call<Result<UserInfoResult>> call = apiService.getUserInfo("dy", "123");
        call.enqueue(new HttpCall<UserInfoResult>() {
            @Override
            public void onSucceed(UserInfoResult result) {
                Log.e(TAG, "旧 baseUrl获取到的数据 --> " + result.toString());
            }

            @Override
            public void onError(String code, String msg) {
                Log.e(TAG, "onError: code:" + code + " , msg:" + msg);
            }
        });


        // <-----------------    改变 Retrofit的 BaseUrl   -------------------->
        changeRetrofitBaseUrl(retrofit, "http://v2.ffu365.com/");


        // <-----------------     新BaseUrl     -------------------->
        URL after = retrofit.baseUrl().url();
        Log.e(TAG, "after: url --> " + after);

        Call<DayDayResult> dayDayResult = apiService.getDayDayResult(
                "Api", "Index", "home", "1", "432");
        dayDayResult.enqueue(new Callback<DayDayResult>() {
            @Override
            public void onResponse(Call<DayDayResult> call, Response<DayDayResult> response) {
                Log.e(TAG, "新 baseUrl获取到的数据 --> " + response.body().getData().getNews_list());
            }

            @Override
            public void onFailure(Call<DayDayResult> call, Throwable t) {

            }
        });


    }


    /**
     * 切换 Retrofit的 BaseUrl
     *
     * @param url 目标url
     */
    private void changeRetrofitBaseUrl(Retrofit retrofit, String url) {
        try {
            Field baseUrl = retrofit.getClass().getDeclaredField("baseUrl");
            baseUrl.setAccessible(true);
            HttpUrl httpUrl = HttpUrl.parse(url);
            baseUrl.set(retrofit, httpUrl);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
