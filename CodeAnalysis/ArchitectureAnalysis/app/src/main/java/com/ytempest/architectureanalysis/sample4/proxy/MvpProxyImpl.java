package com.ytempest.architectureanalysis.sample4.proxy;

import com.ytempest.architectureanalysis.sample4.base.BasePresenter;
import com.ytempest.architectureanalysis.sample4.base.BaseView;
import com.ytempest.architectureanalysis.sample4.inject.InjectPresenter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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

                Class<? extends BasePresenter> presenterClass = (Class<? extends BasePresenter>) field.getType();

                // 最好做一下判断，如果@InjectPresenter注解标记的不是 Presenter类，而是一些
                // 其他乱七八糟的类，那就会抛异常
                if (!BasePresenter.class.isAssignableFrom(presenterClass)) {
                    throw new RuntimeException("@InjectPresenter not support the type of " + field.getType());
                }

                // 检测mView是否实现了该 presenter规定需要实现的 view接口
                checkViewImplement(presenterClass);

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

    private void checkViewImplement(Class<? extends BasePresenter> presenterClass) {

        // 获取 BasePresenter上的 BaseView的泛型
        ParameterizedType type = (ParameterizedType) presenterClass.getGenericSuperclass();
        Class<?> viewClass = (Class<?>) type.getActualTypeArguments()[0];

        // 获取Activity所有的接口
        Class<?>[] interfaces = mView.getClass().getInterfaces();

        // 检测是否Activity是否有 BaseView这个接口
        boolean isImplement = false;
        for (Class<?> anInterface : interfaces) {
            if (anInterface.isAssignableFrom(viewClass)) {
                isImplement = true;
            }
        }

        if (!isImplement) {
            throw new RuntimeException(mView.getClass().getName() + " class must implements the interface: " + viewClass.getName());
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
