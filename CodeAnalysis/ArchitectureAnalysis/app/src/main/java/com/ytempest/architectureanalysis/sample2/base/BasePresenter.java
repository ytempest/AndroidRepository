package com.ytempest.architectureanalysis.sample2.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;

/**
 * @author ytempest
 *         Description：这个类主要用于解决 attach() 和 detach()方法要在每一个Activity中设置的问题；
 *         这个问题的解决方法是：Base + 泛型
 */
public class BasePresenter<V extends BaseView, M extends BaseModel> {

    private V mView;
    /**
     * 通过代理 View实现一种类似 AOP的思想，将判断mView！=null 的代码抽取出来统一处理
     */
    private V mProxyView;
    private M mModel;

    @SuppressWarnings("unchecked")
    public void attach(V view) {
        mView = view;
        // 通过创建 V 的代理对象，来监听V的各个方法（即：onLoading()、onError()、onSucceed()）的执行
        mProxyView = (V) Proxy.newProxyInstance(view.getClass().getClassLoader(), view.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (mView != null) {
                            return method.invoke(mView, args);
                        }
                        return null;
                    }
                });

        mModel = createModel();
    }

    @SuppressWarnings("unchecked")
    private M createModel() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        Class<M> targetClass = (Class<M>) type.getActualTypeArguments()[1];
        M model = null;
        try {
            model = targetClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return model;
    }


    public void detach() {
        mView = null;
        mProxyView = null;
    }

    protected V getView() {
        return mProxyView;
    }

    protected M getModel() {
        return mModel;
    }
}


