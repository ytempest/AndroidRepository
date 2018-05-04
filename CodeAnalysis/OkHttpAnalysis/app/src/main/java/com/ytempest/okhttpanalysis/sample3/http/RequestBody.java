package com.ytempest.okhttpanalysis.sample3.http;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * @author ytempest
 *         Description：
 */
public class RequestBody {

    // 表单格式，这个格式基本是固定的
    public static final String FORM = "multipart/form-data";

    private String type;

    private Map<String, Object> params;

    private static final String boundary = createBoundary();
    private static final String startBoundary = "--" + boundary;
    private static final String endBoundary = startBoundary + "--";

    private static String createBoundary() {
        return "OkHttp" + UUID.randomUUID();
    }

    public RequestBody() {
        this(new Builder());
    }

    private RequestBody(Builder builder) {
        this.type = builder.type;
        this.params = builder.params;
    }

    /**
     * 获取表单的类型，这个格式固定
     */
    public String getContentType() {
        // 这个表单的类型固定，这是规范
        return type + ";boundary=" + boundary;
    }

    /**
     * 获取表单长度（包含规范的字符，如Context-Type:text/plain），这个长度的单位
     * 是byte（startBoundary + 所有键值对的长度 + endBoundary）
     */
    public long getContentLength() {
        int length = 0;

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 添加上传表单的键值对的长度
            if (value instanceof String) {
                String postTextStr = getText(key, (String) value);
                length += postTextStr.getBytes().length;
            }

            // 添加上传文件的长度
            if (value instanceof Binary) {
                Binary binary = (Binary) value;
                String postStr = getGeneralText(key, binary);
                length += postStr.getBytes().length;
                length += binary.fileLength() + "\r\n".getBytes().length;
            }
        }

        if (params.size() != 0) {
            length += endBoundary.getBytes().length;
        }

        return length;
    }

    /**
     * 表单一个键值对的格式如下：
     * startBoundary + "\r\n"
     * Content-Disposition: form-data; name="file"; filename="test.png"
     * Context-Type: (文件的type)
     * <p>
     * 文件的内容流
     */
    private String getGeneralText(String key, Binary binary) {
        return startBoundary + "\r\n" +
                "Content-Disposition:form-data; " +
                "name=\"" + key + "\"; filename=\"" + binary.fileName() + "\"" + "\r\n" +
                "Context-Type:" + binary.mimeType() + "\r\n" +
                "\r\n";
    }

    /**
     * 表单一个键值对的格式如下：
     * startBoundary + "\r\n"
     * Content-Disposition: form-data; name="key"
     * Context-Type: text/plain
     * <p>
     * <p>
     * value
     */
    private String getText(String key, String value) {
        return startBoundary + "\r\n" +
                "Content-Disposition:form-data; name=\"" + key + "\"" + "\r\n" +
                "Context-Type:text/plain" + "\r\n" +
                "\r\n" +
                value + "\r\n";
    }


    /**
     * 一个表单的示例如下：
     * --OkHttp792e7332-a9ed-4519-b57d-3d7c73812cf0
     * Content-Disposition:form-data; name="platform"
     * Context-Type:text/plain
     * <p>
     * android
     * --OkHttp792e7332-a9ed-4519-b57d-3d7c73812cf0
     * Content-Disposition:form-data; name="file"; filename="test2.apk"
     * Context-Type:application/vnd.android.package-archive
     * <p>
     * buffer(com.android.okhttp.internal.http.RetryableSink@cc4d893).outputStream()
     * --OkHttp792e7332-a9ed-4519-b57d-3d7c73812cf0--
     */
    public void onWriteBody(OutputStream outputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String postTextStr = getText(key, (String) value);
                sb.append(postTextStr);
                outputStream.write(postTextStr.getBytes());
            }

            if (value instanceof Binary) {
                Binary binary = (Binary) value;
                String postTextStr = getGeneralText(key, binary);
                sb.append(postTextStr);
                outputStream.write(postTextStr.getBytes());
                sb.append(outputStream);
                binary.onWrite(outputStream);
                sb.append("\r\n");
                outputStream.write("\r\n".getBytes());
            }
        }

        if (params.size() != 0) {
            sb.append(endBoundary);
            outputStream.write(endBoundary.getBytes());
        }

        Log.e(TAG, "onWriteBody: sb --> \n" + sb.toString());
    }

    public static Binary createBinary(final File file) {
        return new Binary() {
            @Override
            public long fileLength() {
                return file.length();
            }

            @Override
            public String fileName() {
                return file.getName();
            }

            @Override
            public String mimeType() {
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String type = fileNameMap.getContentTypeFor(file.getAbsolutePath());
                if (TextUtils.isEmpty(type)) {
                    return "application/octet-stream";
                }
                return type;
            }

            @Override
            public void onWrite(OutputStream outputStream) throws IOException {
                InputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[2048];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                inputStream.close();
            }
        };
    }


    public static class Builder {
        String type;
        Map<String, Object> params;

        public Builder() {
            params = new HashMap<>();
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder addParam(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public Builder addParams(Map<String, Object> params) {
            this.params.putAll(params);
            return this;
        }

        public RequestBody build() {
            return new RequestBody(this);
        }
    }
}
