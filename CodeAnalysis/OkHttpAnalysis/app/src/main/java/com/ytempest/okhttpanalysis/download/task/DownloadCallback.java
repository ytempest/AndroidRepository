package com.ytempest.okhttpanalysis.download.task;

import java.io.File;
import java.io.IOException;

/**
 * @author ytempest
 *         Description：
 */
public interface DownloadCallback {
    void onFailure(IOException e);

    void onProgress(long size, long currentProgress);

    void onSucceed(File file);
}
