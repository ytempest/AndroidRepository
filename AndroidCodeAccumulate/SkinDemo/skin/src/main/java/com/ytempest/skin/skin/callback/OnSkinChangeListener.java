package com.ytempest.skin.skin.callback;

/**
 * @author ytempest
 *         Description：换肤状态的监听器，这个接口的三个方法都会运行在主线程中
 */
public interface OnSkinChangeListener {
    /**
     * 当开始换肤就会调用该方法
     */
    void onStart();

    /**
     * 换肤出现异常就会调用该方法
     *
     * @param errorCode 错误号，{@link com.ytempest.skin.skin.config.SkinConfig}
     */
    void onError(int errorCode);

    /**
     * 当换肤成功会调用该方法
     */
    void onSucceed();
}
