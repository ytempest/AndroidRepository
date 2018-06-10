package com.ytempest.architectureanalysis.sample4.proxy;

/**
 * @author ytempest
 *         Description：这个接口定义不同的View的绑定 Presenter的规范
 */
public interface IMvpProxy {
    void bindAndCreatePresenter();

    void unbind();
}
