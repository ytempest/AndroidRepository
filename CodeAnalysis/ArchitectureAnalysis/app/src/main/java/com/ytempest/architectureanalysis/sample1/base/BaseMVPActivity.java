package com.ytempest.architectureanalysis.sample1.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author ytempest
 *         Description：通过模板模式，将 Presenter的创建交给子类，然后将Presenter的解绑统一在父类
 *         中进行实现
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
