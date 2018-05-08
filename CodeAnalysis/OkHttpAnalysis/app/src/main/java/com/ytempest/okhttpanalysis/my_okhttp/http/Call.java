package com.ytempest.okhttpanalysis.my_okhttp.http;

/**
 * @author ytempest
 *         Description：进行网络请求的规范
 */
public interface Call {
    /**
     * 将 CallBack添加到线程池中执行
     *
     * @param callBack 实现了 Runnable的回调类，里面封装了实现网络请求的逻辑
     */
    void enqueue(CallBack callBack);

    /**
     * 开始网络连接，把请求结果封装成 Response
     */
    Response execute();

}
