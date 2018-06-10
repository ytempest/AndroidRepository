package com.ytempest.architectureanalysis.sample4.proxy;

import com.ytempest.architectureanalysis.sample4.base.BaseView;

/**
 * @author ytempest
 *         Description：
 */
public class GroupViewMvpProxyImpl<V extends BaseView> extends MvpProxyImpl<V>
        implements IGroupViewMvpProxy<V> {

    public GroupViewMvpProxyImpl(V view) {
        super(view);
    }
}
