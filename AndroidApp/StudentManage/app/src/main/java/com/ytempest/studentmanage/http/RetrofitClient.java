package com.ytempest.studentmanage.http;

import android.util.Log;

import com.ytempest.studentmanage.http.converter.InputStreamConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author ytempest
 *         Description：
 */
public class RetrofitClient {
    public static final String URL = "http://192.168.5.104:8080/student_manage_war_exploded/";

//    public static final String URL = "http://192.168.31.68:8080/student_manage_war_exploded/";


//    public static final String URL = "http://192.168.0.160:8081/";


    private static RetrofitClient INSTANCE = null;
    private Retrofit retrofit;

    static {
        INSTANCE = new RetrofitClient();
    }

    private RetrofitClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(InputStreamConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RetrofitClient client() {
        return INSTANCE;
    }

    public <T> T create(Class<T> clazz) {
        return retrofit.create(clazz);
    }


}
