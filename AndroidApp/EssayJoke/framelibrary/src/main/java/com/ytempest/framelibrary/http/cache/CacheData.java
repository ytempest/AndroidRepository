package com.ytempest.framelibrary.http.cache;

/**
 * @author ytempest
 * Description:缓存数据的类
 */
public class CacheData {
    /** 经过MD5处理过的请求链接 */
    private String mUrlKey;

    /** 后台返回的Json */
    private String mResultJson;

    public CacheData() {
    }

    public CacheData(String urlKey, String resultJson) {
        this.mUrlKey = urlKey;
        this.mResultJson = resultJson;
    }

    /**
     * 获取缓存的JSON
     * @return JSON字符串
     */
    public String getResultJson() {
        return mResultJson;
    }
}
