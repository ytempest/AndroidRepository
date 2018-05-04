package com.ytempest.okhttpanalysis.sample3.http;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

/**
 * @author ytempest
 *         Description：
 */
public class RealCall implements Call {

    final Request request;
    final OkHttpClient client;

    private RealCall(Request request, OkHttpClient client) {
        this.request = request;
        this.client = client;
    }

    public static Call newRealCall(Request request, OkHttpClient client) {
        return new RealCall(request, client);
    }

    @Override
    public void enqueue(CallBack callBack) {

        AsyncCall asyncCall = new AsyncCall(callBack);

        client.dispatcher().enqueue(asyncCall);
    }

    @Override
    public Response execute() {
        return null;
    }

    final class AsyncCall extends NamedRunnable {

        final CallBack callBack;

        public AsyncCall(CallBack callBack) {
            this.callBack = callBack;
        }


        /**
         * 在这个方法中进行网络请求以及请求结果的回调
         */
        @Override
        public void execute() {
            // 来这里，开始访问网络 Request -> Response
            try {
                URL url = new URL(request.url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                if (urlConnection instanceof HttpsURLConnection) {
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
                    // 这里做一些关于 Https的操作
                    // httpsURLConnection.setHostnameVerifier();
                    // httpsURLConnection.setSSLSocketFactory();
                }

                // 设置请求的类型
                urlConnection.setRequestMethod(request.method);
                // 如果是 POST请求就设置获取服务器的输出流，利用这个输出流就可以向服务器进行写操作了
                if ("POST".equals(request.method)) {
                    // 设置获取服务器的输出流
                    urlConnection.setDoOutput(true);
                }

                // 获取url请求中的表单
                RequestBody requestBody = request.requestBody;

                // 为提交的表单设置一些请求头
                if (requestBody != null) {
                    // 设置表单的类型
                    urlConnection.setRequestProperty("Content-Type", requestBody.getContentType());
                    // 设置表单的长度，这个长度的单位是byte
                    urlConnection.setRequestProperty("Content-Length", Long.toString(requestBody.getContentLength()));
                }

                // 进行网络连接
                urlConnection.connect();

                // 如果表单不为空，那么就把服务器的输出流给它，让它把表单的信息（包含普通键值对、
                // 文件上传的字节流）写入到服务器的输出流
                if (requestBody != null) {
                    OutputStream outputStream = urlConnection.getOutputStream();
                    requestBody.onWriteBody(outputStream);
                    outputStream.flush();
                    outputStream.close();
                }

                int status = urlConnection.getResponseCode();
                Log.e(TAG, "execute: 请求结果码 -->" + status);

                if (status == 200) {
                    // 获取请求返回的数据流
                    InputStream inputStream = urlConnection.getInputStream();

                    // 把数据封装到 Response中
                    Response response = new Response(inputStream);

                    // 回调请求成功的接口
                    callBack.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                callBack.onFailure(RealCall.this, e);
            }
        }
    }
}
