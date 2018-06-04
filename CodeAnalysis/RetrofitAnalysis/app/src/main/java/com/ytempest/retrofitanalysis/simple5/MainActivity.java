package com.ytempest.retrofitanalysis.simple5;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ytempest.retrofitanalysis.R;
import com.ytempest.retrofitanalysis.simple5.deal.UserInfoResult;
import com.ytempest.retrofitanalysis.simple5.retrofit.RetrofitClient;

import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OkHttp +RxJava + Retrofit 这样写代码行不行？ 1 ，2 ，
        RetrofitClient.getServiceApi()
                .userLogin("dy", "123")
                // .subscribeOn().observeOn().subscribe()
                // Subscriber 封装一下
                // 第二个坑 , 坑我们 返回值都是一个泛型，转换返回值泛型
                .compose(RetrofitClient.<UserInfoResult>transformer())
                // 注册完了要登录
                .subscribe(new BaseObserver<UserInfoResult>() {
                    @Override
                    protected void onError(String errorCode, String errorMessage) {
                        toast(errorMessage);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(UserInfoResult userInfo) {
                         toast(userInfo.toString());
                    }
                });
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void log(String text) {
        Log.e("TAG->Result", text);
    }
}
