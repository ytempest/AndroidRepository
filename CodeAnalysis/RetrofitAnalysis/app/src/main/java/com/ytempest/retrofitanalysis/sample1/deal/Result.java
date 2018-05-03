package com.ytempest.retrofitanalysis.sample1.deal;

/**
 * @author ytempest
 *         Description：这个类可以匹配后台返回的所有数据，无论是成功还是失败
 */
public class Result<T> extends BaseResult {
    private Object data;

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }


}
