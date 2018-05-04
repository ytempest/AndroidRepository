package com.ytempest.okhttpanalysis.sample3.http;

/**
 * @author ytempest
 *         Description：
 */
public class OkHttpClient {

    private Dispatcher dispatcher;

    public OkHttpClient() {
        this(new Builder());
    }

    private OkHttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public Call newCall(Request request) {
        return RealCall.newRealCall(request, this);
    }

    public static class Builder {

        public  Builder() {
            dispatcher = new Dispatcher();
        }

        Dispatcher dispatcher;

        public Builder setDispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public OkHttpClient build() {
            return new OkHttpClient(this);
        }
    }

}
