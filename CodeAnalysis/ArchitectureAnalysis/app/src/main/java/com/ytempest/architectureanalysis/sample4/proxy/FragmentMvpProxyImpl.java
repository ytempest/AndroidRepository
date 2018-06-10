package com.ytempest.architectureanalysis.sample4.proxy;

import com.ytempest.architectureanalysis.sample4.base.BaseView;

/**
 * @author ytempest
 *         Description：
 */
public class FragmentMvpProxyImpl<V extends BaseView> extends MvpProxyImpl<V>
        implements IFragmentMvpProxy<V> {

    public FragmentMvpProxyImpl(V view) {
        super(view);
    }
}
