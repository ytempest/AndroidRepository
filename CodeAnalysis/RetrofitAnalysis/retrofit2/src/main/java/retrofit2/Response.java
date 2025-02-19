/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2;

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static retrofit2.Utils.checkNotNull;

/**
 * An HTTP response.
 */
public final class Response<T> {
    // 这是 OkHttp的 Response
    private final okhttp3.Response rawResponse;
    // 这个是对数据解析成指定的 Result Bean的对象
    private final @Nullable
    T body;
    // 这个是发生异常的时候保存的 ResponseBody
    private final @Nullable
    ResponseBody errorBody;

    private Response(okhttp3.Response rawResponse, @Nullable T body,
                     @Nullable ResponseBody errorBody) {
        this.rawResponse = rawResponse;
        this.body = body;
        this.errorBody = errorBody;
    }

    /**
     * Create a synthetic successful response with {@code body} as the deserialized body.
     */
    public static <T> Response<T> success(@Nullable T body) {
        return success(body, new okhttp3.Response.Builder() //
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create a synthetic successful response with an HTTP status code of {@code code} and
     * {@code body} as the deserialized body.
     */
    public static <T> Response<T> success(int code, @Nullable T body) {
        if (code < 200 || code >= 300) {
            throw new IllegalArgumentException("code < 200 or >= 300: " + code);
        }
        return success(body, new okhttp3.Response.Builder() //
                .code(code)
                .message("Response.success()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create a synthetic successful response using {@code headers} with {@code body} as the
     * deserialized body.
     */
    public static <T> Response<T> success(@Nullable T body, Headers headers) {
        checkNotNull(headers, "headers == null");
        return success(body, new okhttp3.Response.Builder() //
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .headers(headers)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create a successful response from {@code rawResponse} with {@code body} as the deserialized
     * body.
     */
    public static <T> Response<T> success(@Nullable T body, okhttp3.Response rawResponse) {
        checkNotNull(rawResponse, "rawResponse == null");
        if (!rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse must be successful response");
        }
        return new Response<>(rawResponse, body, null);
    }

    /**
     * Create a synthetic error response with an HTTP status code of {@code code} and {@code body}
     * as the error body.
     */
    public static <T> Response<T> error(int code, ResponseBody body) {
        if (code < 400) throw new IllegalArgumentException("code < 400: " + code);
        return error(body, new okhttp3.Response.Builder() //
                .code(code)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * 创建一个错误的 Response，这个Response中只是没有解析好的 Result Bean
     */
    public static <T> Response<T> error(ResponseBody body, okhttp3.Response rawResponse) {
        checkNotNull(body, "body == null");
        checkNotNull(rawResponse, "rawResponse == null");
        // 再做一次检测，如果这个Response不是请求成功后返回的，那么rawResponse.isSuccessful()肯定为false
        if (rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse should not be successful response");
        }

        // 返回一个 Error Response
        return new Response<>(rawResponse, null, body);
    }

    /**
     * The raw response from the HTTP client.
     */
    public okhttp3.Response raw() {
        return rawResponse;
    }

    /**
     * HTTP status code.
     */
    public int code() {
        return rawResponse.code();
    }

    /**
     * HTTP status message or null if unknown.
     */
    public String message() {
        return rawResponse.message();
    }

    /**
     * HTTP headers.
     */
    public Headers headers() {
        return rawResponse.headers();
    }

    /**
     * Returns true if {@link #code()} is in the range [200..300).
     */
    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    /**
     * The deserialized response body of a {@linkplain #isSuccessful() successful} response.
     */
    public @Nullable
    T body() {
        return body;
    }

    /**
     * The raw response body of an {@linkplain #isSuccessful() unsuccessful} response.
     */
    public @Nullable
    ResponseBody errorBody() {
        return errorBody;
    }

    @Override
    public String toString() {
        return rawResponse.toString();
    }
}
