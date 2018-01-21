package com.ytempest.baselibrary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ytempest.baselibrary.ioc.ViewUtils;

/**
 * @author ytempest
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置布局layout
        setContentView(getLayoutResId());

        ViewUtils.inject(this);

        // 初始化头部
        initTitle();

        // 初始化界面
        initView();

        // 初始化数据
        initData();
    }

    /**
     * 获取布局layout的Id
     *
     * @return 布局Id
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化头部
     */
    protected abstract void initTitle();

    /**
     * 初始化界面
     */
    protected abstract void initView();

    /**
     * 初始化数据
     */
    protected abstract void initData();


    /**
     * 启动Activity
     */
    protected void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    /**
     * findViewById
     *
     * @return View
     */
    protected <T extends View> T viewById(int viewId) {
        return (T) findViewById(viewId);
    }

    /**
     * 打印短时间的吐司
     *
     * @param tip
     */
    protected void showToastShort(String tip) {
        Toast.makeText(BaseActivity.this, tip, Toast.LENGTH_SHORT).show();
    }

    /**
     * 打印长时间的吐司
     *
     * @param tip
     */
    protected void showToastLong(String tip) {
        Toast.makeText(BaseActivity.this, tip, Toast.LENGTH_LONG).show();
    }

    // 只能放一些通用的方法，基本每个Activity都需要使用的方法，readDataBase最好不要放进来 ，
    // 如果是两个或两个以上的地方要使用,最好写一个工具类。
}
