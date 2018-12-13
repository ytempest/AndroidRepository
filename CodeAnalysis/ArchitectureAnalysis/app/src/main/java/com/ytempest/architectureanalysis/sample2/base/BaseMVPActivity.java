package com.ytempest.architectureanalysis.sample2.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ytempest.architectureanalysis.sample2.inject.InjectPresenter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public abstract class BaseMVPActivity extends AppCompatActivity implements BaseView {

    private static final String TAG = "BaseMVPActivity";

    private List<BasePresenter<?, ?>> mPresenters;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenters = new ArrayList<>();

        injectPresenter();
    }

    /**
     * 动态注入 Presenter
     */
    @SuppressWarnings("unchecked")
    private void injectPresenter() {
        Field[] declaredFields = this.getClass().getDeclaredFields();
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
                    field.set(this, presenter);

                    // 将Viewattach到这个Presenter
                    presenter.attach(this);

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
    protected void onDestroy() {
        detachPresenter();
        super.onDestroy();
    }

    private void detachPresenter() {
        for (BasePresenter<?, ?> presenter : mPresenters) {
            presenter.detach();
        }
    }
}
