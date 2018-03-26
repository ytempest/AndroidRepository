package com.ytempest.framelibrary.http.cache;

import com.ytempest.baselibrary.security.MD5Utils;
import com.ytempest.framelibrary.db.DaoSupportFactory;
import com.ytempest.framelibrary.db.IDaoSupport;

import java.util.List;


/**
 * create by ytempest at 2017-11-17
 * Description: 对后台返回的JSON字符串进行缓存，以及获取的的辅助类
 */
public class CacheDataUtils {

    /**
     * 获取数据
     */
    public static String getCacheResultJson(String requestUrl) {
        final IDaoSupport<CacheData> dataDaoSupport = DaoSupportFactory.getFactory().getDao(CacheData.class);
        // 从数据库拿缓存
        List cacheDataList = dataDaoSupport.getQuerySupport()
                .selection("mUrlKey = ?").selectionArgs(MD5Utils.stringToMD5(requestUrl))
                .query();
        if (cacheDataList.size() != 0) {
            // 有缓存的数据
            CacheData cacheData = (CacheData) cacheDataList.get(0);
            return cacheData.getResultJson();
        }
        return null;
    }

    /**
     * 缓存数据
     */
    public static long setCacheResultJson(String finalUrl, String resultJson) {
        final IDaoSupport<CacheData> dataDaoSupport = DaoSupportFactory.getFactory().
                getDao(CacheData.class);
        // requestUrl中有http:www，有"："会报错，所以将requestUrl用MD5处理
        String md5Url = MD5Utils.stringToMD5(finalUrl);
        dataDaoSupport.delete("mUrlKey=?", md5Url);
        // 返回插入的行号
        return dataDaoSupport.insert(new CacheData(md5Url, resultJson));
    }
}
