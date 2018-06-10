package com.ytempest.architectureanalysis.sample3.proxy;

import com.ytempest.architectureanalysis.sample3.base.BasePresenter;
import com.ytempest.architectureanalysis.sample3.base.BaseView;
import com.ytempest.architectureanalysis.sample3.inject.InjectPresenter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：这个类实现统一的 View绑定 Presenter的逻辑，其他扩充逻辑在各自的 Proxy
 *         接口中定义，然后实现
 */
public class MvpProxyImpl<V extends BaseView> implements IMvpProxy {

    private V mView;
    private List<BasePresenter<V, ?>> mPresenters;

    public MvpProxyImpl(V view) {
        this.mView = view;
        mPresenters = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bindAndCreatePresenter() {
        Field[] declaredFields = mView.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            InjectPresenter annotation = field.getAnnotation(InjectPresenter.class);
            if (annotation != null) {
                Class<? extends BasePresenter> presenterClass = null;

                try {
                    // 最好做一下判断，如果@InjectPresenter注解标记的不是 Presenter类，而是一些
                    // 其他乱七八糟的类，那就会抛异常
                    presenterClass = (Class<? extends BasePresenter>) field.getType();
                } catch (Exception e) {
                    throw new RuntimeException("@InjectPresenter not support the type of " + field.getType());
                }

                try {
                    // 注入 Presenter
                    BasePresenter presenter = presenterClass.newInstance();
                    field.setAccessible(true);
                    field.set(mView, presenter);

                    // attach这个 Presenter
                    presenter.attach(mView);

                    // 将这个 Presenter保存起来，要用于 detach
                    mPresenters.add(presenter);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void unbind() {
        mView = null;
        for (BasePresenter<V, ?> presenter : mPresenters) {
            presenter.detach();
        }
    }
}
