package com.ytempest.architectureanalysis.sample4.proxy;

import com.ytempest.architectureanalysis.sample4.base.BaseView;

/**
 * @author ytempest
 *         Description：
 */
public class ActivityMvpProxyImpl<V extends BaseView> extends MvpProxyImpl<V>
        implements IActivityMvpProxy<V> {

    public ActivityMvpProxyImpl(V view) {
        super(view);
    }

    // 在这里就可以写一些 Activity的相关逻辑
}
