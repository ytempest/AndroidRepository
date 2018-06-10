package com.ytempest.architectureanalysis.sample4.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ytempest.architectureanalysis.sample4.proxy.ActivityMvpProxyImpl;
import com.ytempest.architectureanalysis.sample4.proxy.IActivityMvpProxy;

/**
 * @author ytempest
 *         Description：
 */
public abstract class BaseMVPActivity extends AppCompatActivity implements BaseView {

    private static final String TAG = "BaseMVPActivity";

    private IActivityMvpProxy<BaseView> mActivityMvpProxy;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityMvpProxy = createActivityMvpProxy();
        mActivityMvpProxy.bindAndCreatePresenter();

    }

    private ActivityMvpProxyImpl<BaseView> createActivityMvpProxy() {
        return new ActivityMvpProxyImpl<BaseView>(this);
    }


    @Override
    protected void onDestroy() {
        mActivityMvpProxy.unbind();
        super.onDestroy();
    }


}
