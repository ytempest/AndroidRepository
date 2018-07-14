package com.ytempest.framelibrary.net.request;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author ytempest
 *         Description：根据请求地址和请求参数创建出指定的Request
 */
public class RequestCreator {
    public static Request createPostRequest(String url, RequestParams requestParams) {
        return createPostRequest(url, requestParams, null);
    }

    public static Request createPostRequest(String url, RequestParams params, RequestParams headersParams) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Headers.Builder headersBuilder = new Headers.Builder();
        if (headersParams != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        FormBody formBody = formBodyBuilder.build();
        Headers headers = headersBuilder.build();

        return new Request.Builder()
                .url(url)
                .post(formBody)
                .headers(headers)
                .build();

    }

    public static Request createGetRequest(String url, RequestParams params) {
        return createGetRequest(url, params, null);
    }

    public static Request createGetRequest(String url, RequestParams params, RequestParams headersParams) {
        StringBuilder urlBuilder = new StringBuilder(url + "?");
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }

        Headers.Builder headersBuilder = new Headers.Builder();
        if (headersParams != null) {
            for (Map.Entry<String, String> entry : headersParams.urlParams.entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        String finalUrl = urlBuilder.substring(0, urlBuilder.length() - 1);
        Headers headers = headersBuilder.build();

        return new Request.Builder()
                .get()
                .url(finalUrl)
                .headers(headers)
                .build();
    }

    public static Request createMonitorRequest(String url, RequestParams params) {
        StringBuilder urlBuilder = new StringBuilder(url).append("&");
        if (params != null && params.hasParams()) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        String finalUrl = urlBuilder.substring(0, urlBuilder.length() - 1);
        return new Request.Builder()
                .get()
                .url(finalUrl)
                .build();
    }

    private static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");

    public static Request createMultiPostRequest(String url, RequestParams params) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.fileParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof File) {
                    multipartBodyBuilder.addPart(
                            Headers.of("Content-Disposition", "form-data;name=\"" + key + "\""),
                            RequestBody.create(FILE_TYPE, (File) value));

                } else if (value instanceof String) {
                    multipartBodyBuilder.addPart(
                            Headers.of("Content-Disposition", "form-data;name=\"" + key + "\""),
                            RequestBody.create(null, (String) value));
                }
            }
        }

        return new Request.Builder()
                .post(multipartBodyBuilder.build())
                .url(url)
                .build();
    }
}
