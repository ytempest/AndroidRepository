package com.ytempest.okhttpanalysis.my_okhttp.http;

/**
 * @author ytempest
 *         Description：
 */
public class Request {

    final String url;
    final String method;
    final  RequestBody requestBody;

    private Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.requestBody = builder.requestBody;
    }

    public static class Builder {
        String url;
        String method;
        RequestBody requestBody;

        public Builder() {
            method = "GET";
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder get() {
            this.method = "GET";
            return this;
        }

        public Builder post(RequestBody requestBody) {
            this.method = "POST";
            this.requestBody = requestBody;
            return this;
        }


        public Request build() {
            return new Request(this);
        }
    }
}
