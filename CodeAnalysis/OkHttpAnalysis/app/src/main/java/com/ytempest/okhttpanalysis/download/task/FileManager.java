package com.ytempest.okhttpanalysis.download.task;

import android.content.Context;

import java.io.File;

/**
 * @author ytempest
 *         Description：
 */
public class FileManager {
    private static FileManager mInstance;
    private Context mContext;
    private File mRootFile;

    private FileManager() {

    }

    public static FileManager getInstance() {
        if (mInstance == null) {
            synchronized (FileManager.class) {
                if (mInstance == null) {
                    mInstance = new FileManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void setRootFile(File rootFile) {
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }

        if (rootFile.exists() && rootFile.isDirectory()) {
            mRootFile = rootFile;
        }
    }

    public File getFile(String url) {
        String fileName = Utils.md5Url(url);

        if (mRootFile == null) {
            mRootFile = mContext.getCacheDir();
        }

        return new File(mRootFile, fileName);
    }

    public void deleteFile(String url) {
        File file = getFile(url);
        if (file.exists()) {
            file.delete();
        }
    }
}
