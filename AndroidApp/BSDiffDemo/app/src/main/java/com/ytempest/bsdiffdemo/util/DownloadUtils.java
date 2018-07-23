package com.ytempest.bsdiffdemo.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author ytempest
 *         Description：
 */
public class DownloadUtils {

    private static final String TAG = "DownloadUtils";

    /**
     * 从指定网络位置下载文件到指定存储位置中
     */
    public static void download(String url, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        InputStream is = null;
        FileOutputStream os = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoInput(true);
            is = urlConnection.getInputStream();
            os = new FileOutputStream(file);

            byte[] buffer = new byte[50];
            int len = 0;
            long apkSize = 0;
            while ((len = is.read(buffer)) != -1) {
                apkSize += len;
                Log.d(TAG, "download: " + apkSize);
                os.write(buffer, 0, len);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
