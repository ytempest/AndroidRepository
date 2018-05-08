package com.ytempest.okhttpanalysis.my_okhttp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author ytempest
 *         Description：这是网络请求的响应类，这个类会封装后台返回的数据
 */
public class Response {

    /**
     * 后台数据的输入流
     */
    private final InputStream inputStream;

    public Response(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * 获取后台数据的字符串
     */
    public String string() {
        if (inputStream != null) {
            return convertStreamToString(inputStream);
        }
        return null;
    }

    /**
     * 将 InputStream流的数据转换成String
     */
    private String convertStreamToString(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        String line;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();

    }
}
