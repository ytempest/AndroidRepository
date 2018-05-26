package com.ytempest.retrofitanalysis.sample1.deal;

/**
 * @author ytempest
 *         Description：这个类可以匹配后台返回的所有数据，无论是成功还是失败
 *         泛型T：用于标识后台返回成功数据时，data应该要转换的类型
 */
public class Result<T> extends BaseResult {
    /**
     * 使用了 Object，所以无论后台返回什么样的格式（空字符串、对象）都可以匹配
     */
    private Object data;

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }
}
