package com.ytempest.framelibrary.net.https;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author ytempest
 *         Description：
 */
public class HttpsUtils {
    private static final String TAG = "HttpsUtils";

    public static SSLSocketFactory initSSLSocketFactory() {
        SSLContext sslContext = null;
        try {
            // 创建一个SSL的Context
            sslContext = SSLContext.getInstance("SSL");
            // 获取信任管理器集合
            X509TrustManager[] xTrustArray = new X509TrustManager[]{initTrustManager()};
            // 初始化这个SSL的Context，用于获取SocketFactory
            sslContext.init(null, xTrustArray, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "initSSLSocketFactory: 获取到的SSLFactory=" + sslContext.getSocketFactory());

        // 获取SocketFactory并返回
        return sslContext.getSocketFactory();
    }

    public static X509TrustManager initTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                Log.e(TAG, "getAcceptedIssuers: 返回一个 X509Certificate对象");
                return new X509Certificate[]{};
            }

        };
    }
}
