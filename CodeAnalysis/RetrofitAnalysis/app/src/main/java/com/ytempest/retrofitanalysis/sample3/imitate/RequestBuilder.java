package com.ytempest.retrofitanalysis.sample3.imitate;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * @author ytempest
 *         Description：
 */
class RequestBuilder {

    private final String baseUrl;
    private final String httpMethod;
    private final String relativeUrl;
    private FormBody.Builder formBodyBuilder;

    public RequestBuilder(String httpMethod, String baseUrl, String relativeUrl) {
        this.httpMethod = httpMethod;
        this.baseUrl = baseUrl;
        this.relativeUrl = relativeUrl;
        this.formBodyBuilder = new FormBody.Builder();
    }

    void addFormField(String name, String value) {
        formBodyBuilder.add(name, value);
    }

    Request build() {
        String url = baseUrl + relativeUrl;
        Request request = new Request.Builder()
                .url(url)
                .method(httpMethod, formBodyBuilder.build())
                .build();

        return request;
    }
}
