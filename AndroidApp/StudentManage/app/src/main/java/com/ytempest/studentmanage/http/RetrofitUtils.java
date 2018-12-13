package com.ytempest.studentmanage.http;

import android.support.annotation.NonNull;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author ytempest
 *         Description：
 */
public class RetrofitUtils {


    private RetrofitUtils() {
    }

    /**
     * 将字符串类型的数据封装成表单的Body部分
     */
    public static RequestBody createBodyFromString(String value) {
        if (value == null) {
            value = "";
        }
        return RequestBody.create(
                MediaType.parse("multipart/form-data"), value);
    }

    /**
     * 将文件封装成MultipartBody.Part类型
     */
    public static MultipartBody.Part createPartFromFile(String name, @NonNull File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(file.getAbsolutePath())), file);
        return MultipartBody.Part.createFormData(name, file.getName(), fileBody);
    }


    /**
     * 猜测文件类型
     */
    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }
}
