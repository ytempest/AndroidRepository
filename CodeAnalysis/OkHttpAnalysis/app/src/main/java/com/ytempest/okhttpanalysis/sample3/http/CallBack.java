package com.ytempest.okhttpanalysis.sample3.http;

import java.io.IOException;

/**
 * @author ytempest
 *         Description：网络请求的回调接口
 */
public interface CallBack {

    void onFailure(Call call, IOException e);

    void onResponse(Call call, Response response) throws IOException;

}
