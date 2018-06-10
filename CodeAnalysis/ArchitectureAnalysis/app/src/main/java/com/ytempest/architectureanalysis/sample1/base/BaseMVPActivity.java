package com.ytempest.architectureanalysis.sample1.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author ytempest
 *         Description：
 */
public abstract class BaseMVPActivity<P extends BasePresenter>
        extends AppCompatActivity implements BaseView {

    protected P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = createPresenter();
        mPresenter.attach(this);
    }


    @Override
    protected void onDestroy() {
        mPresenter.detach();
        super.onDestroy();
    }

    // 用于创建指定的 Presenter
    protected abstract P createPresenter();
}
