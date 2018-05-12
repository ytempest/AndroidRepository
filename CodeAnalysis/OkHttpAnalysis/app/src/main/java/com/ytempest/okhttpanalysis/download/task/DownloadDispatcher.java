package com.ytempest.okhttpanalysis.download.task;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：
 */
public class DownloadDispatcher {
    private static final String TAG = "DownloadDispatcher";

    private final static DownloadDispatcher sDispatcher = new DownloadDispatcher();

    private Deque<DownloadTask> mRunningTasks = new ArrayDeque<>();
    private Deque<DownloadTask> mStopTasks = new ArrayDeque<>();

    private DownloadDispatcher() {
    }

    public static DownloadDispatcher getDispatcher() {
        return sDispatcher;
    }

    public void startDownload(final String url, final DownloadCallback callback) {
        if (isTaskRunning(url)) {
            return;
        }
        Call call = OkHttpManager.getInstance().asyncCall(url);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long contentLength = response.body().contentLength();
                if (contentLength == -1) {
                    // 使用单线程下载，在这里也要处理下载进度
                    // TODO: 2018/5/10/010
                    return;
                }

                // 从暂停任务列表中获取该下载任务
                DownloadTask downloadTask = getDownloadTakFormStop(url);
                if (downloadTask == null) {
                    // 从对象池中获取，防止创建多余的对象
                    downloadTask = DownloadTask.obtainDownloadTask(url, contentLength, callback);
                }
                downloadTask.start();

                mRunningTasks.add(downloadTask);
            }
        });
    }

    private boolean isTaskRunning(String url) {
        if (mRunningTasks.size() > 0) {
            for (DownloadTask runningTask : mRunningTasks) {
                if (runningTask.getUrl().equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }


    private DownloadTask getDownloadTakFormStop(String url) {
        for (DownloadTask stopTask : mStopTasks) {
            if (stopTask.getUrl().equals(url)) {
                Log.e(TAG, "getDownloadTakFormStop: 该任务在暂停列表中");
                mStopTasks.remove(stopTask);
                return stopTask;
            }
        }
        return null;
    }

    public synchronized void stopDownload(String url) {
        for (DownloadTask runningTask : mRunningTasks) {
            if (runningTask.getUrl().equals(url)) {
                runningTask.stop();
                // 从运行任务列表中移除
                mRunningTasks.remove(runningTask);
                // 添加到停止任务列表中
                mStopTasks.add(runningTask);
            }
        }
    }

    public synchronized void cancelDownload(String url) {
        // 如果任务正在运行
        if (mRunningTasks.size() > 0) {
            for (DownloadTask runningTask : mRunningTasks) {
                if (runningTask.getUrl().equals(url)) {
                    runningTask.cancel();
                    mRunningTasks.remove(runningTask);
                    DownloadTask.recycle(runningTask);
                    return;
                }
            }
        }

        // 如果任务被暂停了
        if (mStopTasks.size() > 0) {
            for (DownloadTask stopTask : mStopTasks) {
                if (stopTask.getUrl().equals(url)) {
                    stopTask.cancel();
                    mStopTasks.remove(stopTask);
                    DownloadTask.recycle(stopTask);
                }
            }
        }

        // 如果任务被进度保存到了数据库，那么直接删除数据
        DownloadTask.deleteFile(url);
    }

    public synchronized void removeTaskFromRunning(DownloadTask downloadTask) {
        mRunningTasks.remove(downloadTask);
    }
}
