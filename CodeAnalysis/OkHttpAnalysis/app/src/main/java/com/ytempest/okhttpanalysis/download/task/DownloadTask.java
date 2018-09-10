package com.ytempest.okhttpanalysis.download.task;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ytempest
 *         Description：一个DownloadTask对象对应一个下载任务，在这个对象中会分配成几个子任务，然后
 *         让每个子任务完成各自的下载任务
 */
class DownloadTask {
    private static final String TAG = "DownloadTask";

    private String mUrl;
    private long mContentLength;
    private DownloadCallback mCallback;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private List<DownloadRunning> mRunnableList;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int THREAD_COUNT = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private int mSucceedCount;
    private long mCurrentProgress;

    private static final int MAX_TASK_COUNT = 5;
    private static final List<DownloadTask> TASK_POOL = new ArrayList<>();
    /**
     * 该阈值用于和进度比较从而判断是否将下载进度保存到本地
     */
    private long threshold = 0;

    private DownloadTask(String url, long contentLength, DownloadCallback callback) {
        this.mUrl = url;
        this.mContentLength = contentLength;
        this.mCallback = callback;
        mRunnableList = new ArrayList<>();

        threshold = contentLength / THREAD_COUNT;
    }

    private synchronized ExecutorService executorService() {
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r, "DownloadTask");
                    thread.setDaemon(false);
                    return thread;
                }
            });
        }
        return mThreadPoolExecutor;
    }


    /**
     * 启动下载任务
     */
    public void start() {
        // 获取每一个子任务的下载实体
        List<DownloadEntity> entities = DaoManagerHelper.getManager().queryEntity(mUrl);

        if (entities == null) {
            entities = new ArrayList<>();
        }

        if (entities.size() == 0) {
            // 根据线程数量分配下载的子任务
            for (int i = 0; i < THREAD_COUNT; i++) {
                // 查找该下载任务是否保存在数据库中，如果有，那么就断点下载
                long threadSize = mContentLength / THREAD_COUNT;
                long start = i * threadSize;
                long end = (i + 1) * threadSize - 1;
                if (i == THREAD_COUNT - 1) {
                    end = mContentLength - 1;
                }

                // 如果数据库没有保存这个任务的下载进度，那么就开启一个任务下载
                DownloadEntity entity = new DownloadEntity(mUrl, start, end, 0, i);
                entities.add(entity);
            }
        }

        // 初始化总进度
        initCurrentProgress(entities);

        // 计算阈值
        threshold = calculateThreshold();

        // 判断该任务是否已经暂停了，如果已经是暂停了，那么就重启该任务
        if (mRunnableList.size() > 0) {
            Log.i(TAG, "重启每一个下载子任务");
            // 重启每一个子任务
            restartTask(entities);
            return;
        }

        // 如果该任务没有暂停过
        for (DownloadEntity entity : entities) {
            DownloadRunning downloadRunning = new DownloadRunning(entity, entity.progress, new DownloadCallback() {
                @Override
                public void onFailure(IOException e) {
                    // TODO: 2018/5/9/009
                    // 如果其中一个线程出现问题，就停止其他线程
                    mCallback.onFailure(e);
                }

                @Override
                public void onProgress(long size, long blockSize) {
                    synchronized (DownloadTask.class) {
                        mCurrentProgress += blockSize;
                        // 如果下载进度到达阈值就保存进度，并计算新的阈值
                        if (mCurrentProgress > threshold) {
                            Log.i(TAG, "onProgress: 自动保存下载进度");
                            threshold += mContentLength / THREAD_COUNT;
                            saveProgress();
                        }
                        mCallback.onProgress(mContentLength, mCurrentProgress);
                    }
                }

                @Override
                public void onSucceed(File file) {
                    synchronized (DownloadTask.class) {
                        mSucceedCount++;
                        Log.i(TAG, "onSucceed: 任务完成数量：" + mSucceedCount);
                        if (mSucceedCount == THREAD_COUNT) {
                            mCallback.onSucceed(file);
                            // 如果该下载任务保存在了数据库，那么就删除
                            DaoManagerHelper.getManager().deleteEntity(mUrl);
                            // 将下载任务从正在下载的列表中删除，并回收当前对象
                            DownloadDispatcher.getDispatcher().removeTaskFromRunning(DownloadTask.this);
                            // 当下载任务完成后，回收DownloadTask对象
                            DownloadTask.recycle(DownloadTask.this);
                        }
                    }
                }
            });

            mRunnableList.add(downloadRunning);
            executorService().execute(downloadRunning);
        }

    }

    /**
     * 根据已经下载的进度计算出下一个用于判断是否存储进度的阈值
     */
    private long calculateThreshold() {
        long threadSize = mContentLength / THREAD_COUNT;
        for (int i = 0; i < THREAD_COUNT; i++) {
            if ((i + 1) * threadSize > mCurrentProgress) {
                return (i + 1) * threadSize;
            }
        }
        return 0;
    }

    private void saveProgress() {
        for (DownloadRunning downloadRunning : mRunnableList) {
            downloadRunning.save();
        }
    }

    private void restartTask(List<DownloadEntity> entities) {
        for (int i = 0; i < mRunnableList.size(); i++) {
            DownloadEntity entity = entities.get(i);
            DownloadRunning downloadRunning = mRunnableList.get(i);
            downloadRunning.updateInfo(entity, entity.progress);
            executorService().execute(downloadRunning);
        }
    }

    private void initCurrentProgress(List<DownloadEntity> entities) {
        mCurrentProgress = 0;
        for (DownloadEntity entity : entities) {
            Log.i(TAG, "子任务[" + entity.threadId + "]的进度是：" + entity.progress);
            mCurrentProgress += entity.progress;
        }
    }


    public String getUrl() {
        return mUrl;
    }

    /**
     * 从DownloadTask对象池中获取DownloadTask对象，如果对象池中没有对象则创建一个新的对象
     */
    public static DownloadTask obtainDownloadTask(String url, long contentLength, DownloadCallback callback) {
        synchronized (TASK_POOL) {
            int size = TASK_POOL.size();
            if (size > 0) {
                DownloadTask downloadTask = TASK_POOL.get(0);
                downloadTask.mUrl = url;
                downloadTask.mContentLength = contentLength;
                downloadTask.mCallback = callback;
                return downloadTask;
            }
        }
        return new DownloadTask(url, contentLength, callback);
    }

    public static void recycle(DownloadTask downloadTask) {
        synchronized (TASK_POOL) {
            if (TASK_POOL.size() < MAX_TASK_COUNT) {
                downloadTask.clear();
                TASK_POOL.add(downloadTask);
            }
        }
    }

    public void clear() {
        mUrl = null;
        mContentLength = 0;
        mCallback = null;
        mThreadPoolExecutor = null;
        mRunnableList.clear();
        mSucceedCount = 0;
        mCurrentProgress = 0;
    }


    public void stop() {
        for (DownloadRunning downloadRunning : mRunnableList) {
            downloadRunning.stop();
        }
    }

    public void cancel() {
        for (DownloadRunning downloadRunning : mRunnableList) {
            downloadRunning.cancel();
        }
    }

    public static void deleteFile(String url) {
        DaoManagerHelper.getManager().deleteEntity(url);
        FileManager.getInstance().deleteFile(url);
    }

}
