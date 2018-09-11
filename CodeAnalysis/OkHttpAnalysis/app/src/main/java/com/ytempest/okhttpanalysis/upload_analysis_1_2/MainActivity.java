package com.ytempest.okhttpanalysis.upload_analysis_1_2;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ytempest.okhttpanalysis.R;
import com.ytempest.okhttpanalysis.upload_analysis_1_2.listener.OnUploadListener;
import com.ytempest.okhttpanalysis.upload_analysis_1_2.solution_2.MultipartBodyDelegate;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HttpActivity";
    private File mFile = new File(Environment.getExternalStorageDirectory(), "test2.apk");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private String guessMimeType(File file) {
        String filePath = file.getAbsolutePath();

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(filePath);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }


    public void upload(View view) {
        String url = "https://api.saiwuquan.com/api/upload";

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                // 添加日志打印
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        Log.e(TAG, "upload: mFile.length --> " + mFile.length());
        RequestBody fileRequestBody = RequestBody.create(MediaType.parse(guessMimeType(mFile)), mFile);
        // RequestBodyDelegate requestBodyDelegate = new RequestBodyDelegate(fileRequestBody);
        builder.addFormDataPart("platform", "android");
        builder.addFormDataPart("file", mFile.getName(), fileRequestBody);

        MultipartBodyDelegate multipartBodyDelegate = new MultipartBodyDelegate(builder.build(), new OnUploadListener() {
            @Override
            public void onProgress(long maxLength, long currentLength) {
                Log.e(TAG, "onProgress: maxLength --> " + maxLength + "  ||  currentLength --> " + currentLength);
            }
        });

        Request request = new Request.Builder()
                .url(url)
                .post(multipartBodyDelegate)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(TAG, "onFailure: 上传失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "onResponse: 上传成功");
            }
        });
    }

}
