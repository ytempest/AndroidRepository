package com.ytempest.okhttpanalysis.interceptor_analysis.test;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：
 */
public class CacheRequestInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response response = chain.proceed(chain.request());

        return response;
    }
}
