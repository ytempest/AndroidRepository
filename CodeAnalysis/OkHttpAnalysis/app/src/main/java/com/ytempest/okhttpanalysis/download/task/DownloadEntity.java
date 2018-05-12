package com.ytempest.okhttpanalysis.download.task;

/**
 * @author ytempest
 *         Description：这是每一个线程下载任务的一个实体，它包含了一个子线程下载任务的一系列信息
 */
public class DownloadEntity {
    public String url;
    public long start;
    public long end;
    public int threadId;
    public long progress;

    public DownloadEntity() {
    }

    public DownloadEntity(String url, long start, long end, long progress, int threadId) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.progress = progress;
        this.threadId = threadId;
    }

    public String getUrl() {
        return url;
    }

    public DownloadEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getStart() {
        return start;
    }

    public DownloadEntity setStart(long start) {
        this.start = start;
        return this;
    }

    public long getEnd() {
        return end;
    }

    public DownloadEntity setEnd(long end) {
        this.end = end;
        return this;
    }

    public int getThreadId() {
        return threadId;
    }

    public DownloadEntity setThreadId(int threadId) {
        this.threadId = threadId;
        return this;
    }

    public long getProgress() {
        return progress;
    }

    public DownloadEntity setProgress(long progress) {
        this.progress = progress;
        return this;
    }
}
