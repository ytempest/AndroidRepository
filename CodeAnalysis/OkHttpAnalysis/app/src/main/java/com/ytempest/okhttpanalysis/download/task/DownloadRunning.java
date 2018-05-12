package com.ytempest.okhttpanalysis.download.task;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Response;

/**
 * @author ytempest
 *         Description：这个类是每一个线程的任务类，负责完成下载任务的一个子任务
 */
class DownloadRunning implements Runnable {

    private static final String TAG = "DownloadRunning";

    private DownloadEntity mEntity;
    private DownloadCallback mCallback;
    private long mProgress;

    private boolean isStop = false;
    private boolean isCancel = false;


    public DownloadRunning(DownloadEntity entity, long progress, DownloadCallback callback) {
        mProgress = progress;
        mEntity = entity;
        this.mCallback = callback;
    }

    @Override
    public void run() {
        // 重置下载状态
        resetDownloadStatus();
        InputStream inputStream = null;
        RandomAccessFile accessFile = null;
        try {
            Response response = OkHttpManager.getInstance().asyncCall(
                    mEntity.url, mEntity.start + mEntity.progress, mEntity.end);

            inputStream = response.body().byteStream();
            File file = FileManager.getInstance().getFile(mEntity.url);
            accessFile = new RandomAccessFile(file, "rwd");
            // 将文件流的指针移动到断点的位置
            accessFile.seek(mEntity.start + mProgress);

            byte[] buffer = new byte[1024 * 5];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                if (!isCancel) {
                    // 如果用户停止了下载任务
                    if (isStop) {
                        // 保存进度到数据库，然后退出
                        mEntity.progress = mProgress;
                        DaoManagerHelper.getManager().addEntity(mEntity);
                        Log.e(TAG, "run: 任务(" + mEntity.threadId + ")已经暂停");
                        return;
                    }

                    mProgress += len;
                    mCallback.onProgress(mEntity.end - mEntity.start, len);
                    accessFile.write(buffer, 0, len);
                } else {
                    // 删除进度数据
                    DaoManagerHelper.getManager().deleteEntity(mEntity.url);
                    // 删除文件
                    FileManager.getInstance().deleteFile(mEntity.url);
                    return;
                }
            }
            // 下载成功回调接口
            mCallback.onSucceed(file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(inputStream);
            Utils.close(accessFile);
        }
    }

    private void resetDownloadStatus() {
        isStop = false;
        isCancel = false;
    }


    public void stop() {
        isStop = true;
    }

    public void cancel() {
        isCancel = true;
    }

    public void updateInfo(DownloadEntity entity, long progress) {
        this.mEntity = entity;
        this.mProgress = progress;
        resetDownloadStatus();
    }
}
