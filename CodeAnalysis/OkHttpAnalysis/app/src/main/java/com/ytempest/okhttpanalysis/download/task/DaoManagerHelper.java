package com.ytempest.okhttpanalysis.download.task;

import com.ytempest.okhttpanalysis.download.task.db.DaoSupportFactory;
import com.ytempest.okhttpanalysis.download.task.db.IDaoSupport;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class DaoManagerHelper {
    private static DaoManagerHelper sInstance;
    private static IDaoSupport<DownloadEntity> mDaoSupport;

    private DaoManagerHelper() {
        mDaoSupport = DaoSupportFactory.getFactory().getDao(DownloadEntity.class);
    }

    public static DaoManagerHelper getManager() {
        if (sInstance == null) {
            synchronized (DaoManagerHelper.class) {
                if (sInstance == null) {
                    sInstance = new DaoManagerHelper();
                }
            }
        }
        return sInstance;
    }

    public List<DownloadEntity> queryEntity(String url) {
        return mDaoSupport.getQuerySupport().selection("url = ?").selectionArgs(url).query();
    }

    public void addEntity(DownloadEntity entity) {
        mDaoSupport.delete("url = ? and threadId = ?",
                entity.url, entity.threadId + "");
        mDaoSupport.insert(entity);
    }

    public void deleteEntity(String url) {
        mDaoSupport.delete("url = ?", url);
    }
}
