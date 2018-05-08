package com.ytempest.okhttpanalysis.my_okhttp;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ytempest.okhttpanalysis.R;
import com.ytempest.okhttpanalysis.my_okhttp.http.Call;
import com.ytempest.okhttpanalysis.my_okhttp.http.CallBack;
import com.ytempest.okhttpanalysis.my_okhttp.http.OkHttpClient;
import com.ytempest.okhttpanalysis.my_okhttp.http.Request;
import com.ytempest.okhttpanalysis.my_okhttp.http.RequestBody;
import com.ytempest.okhttpanalysis.my_okhttp.http.Response;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpActivity extends AppCompatActivity {

    private static final String TAG = "HttpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test();

    }

    private void test() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();


        File file = new File(Environment.getExternalStorageDirectory(), "test2.zip");
        RequestBody requestBody = new RequestBody.Builder()
                .type(RequestBody.FORM)
//                .addParam("platform", "android")
//                .addParam("file", RequestBody.createBinary(file))
                .addParams(getParams())
                .build();

        Request request = new Request.Builder()
                .post(requestBody)
//                .url("https://api.saiwuquan.com/api/upload")
                .url("http://v2.ffu365.com/")
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new CallBack() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.string();
                Log.e(TAG, "onResponse: response --》" + string);
            }
        });
    }

    public Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("m", "Api");
        params.put("c", "Index");
        params.put("a", "home");
        params.put("appid", "1");
        params.put("uid", "432");

        return params;
    }
}