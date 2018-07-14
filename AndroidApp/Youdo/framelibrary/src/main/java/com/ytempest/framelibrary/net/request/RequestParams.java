package com.ytempest.framelibrary.net.request;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ytempest
 *         Description：
 */
public class RequestParams {
    // 用于存放普通的键值对
    public ConcurrentHashMap<String, String> urlParams = new ConcurrentHashMap<>();
    // 用于存放文件或者mp3之类的键值对
    public ConcurrentHashMap<String, Object> fileParams = new ConcurrentHashMap<>();

    /**
     * 默认构造方法
     */
    public RequestParams() {
        this(null);
    }

    public RequestParams(Map<String, String> map) {
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public RequestParams(String key, String value) {
        put(key, value);
    }


    public RequestParams put(String key, String value) {
        if (key != null) {
            urlParams.put(key, value);
        }
        return this;
    }


    public RequestParams put(String key, Object value) throws FileNotFoundException {
        if (key != null) {
            fileParams.put(key, value);
        }
        return this;
    }


    public boolean hasParams() {
        return urlParams.size() > 0 || fileParams.size() > 0;
    }
}
