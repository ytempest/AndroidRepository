package com.ytempest.okhttpanalysis.upload_analysis_1_2.listener;

/**
 * @author ytempest
 *         Description：文件上传进度监听器
 */
public interface OnUploadListener {
    /**
     * @param maxLength     上传文件的长度
     * @param currentLength 上传的进度
     */
    void onProgress(long maxLength, long currentLength);
}

