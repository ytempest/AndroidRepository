package com.ytempest.baselibrary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.ytempest.baselibrary.util.ActivityStackManager;

import butterknife.ButterKnife;

/**
 * @author ytempest
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = this.getComponentName().getShortClassName();

        // 添加 Activity 到 ActivityStackManager 中进行管理
        ActivityStackManager.getInstance().registerActivity(this);

        // 设置布局layout
        setContentView(getLayoutResId());

        // IOC注入
        ButterKnife.bind(this);

        // 初始化头部
        initTitle();

        // 初始化界面
        initView();

        // 初始化数据
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销 Activity，防止内存泄漏
        ActivityStackManager.getInstance().unregisterActivity(this);

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
     * 启动Activity，带结果
     */
    protected void startActivityForResult(Class<?> clazz,int requetCode) {
        Intent intent = new Intent(this, clazz);
        startActivityForResult(intent, requetCode);
    }

    /**
     * findViewById
     *
     * @return View
     */
    protected <T extends View> T viewById(int viewId) {
        return (T) findViewById(viewId);
    }


    protected void showToastShort(String tip) {
        Toast.makeText(BaseActivity.this, tip, Toast.LENGTH_SHORT).show();
    }


    protected void showToastShort(@StringRes int resId) {
        Toast.makeText(BaseActivity.this, resId, Toast.LENGTH_SHORT).show();
    }



    protected void showToastLong(String tip) {
        Toast.makeText(BaseActivity.this, tip, Toast.LENGTH_LONG).show();
    }

    protected void showToastLong(@StringRes int resId) {
        Toast.makeText(BaseActivity.this, resId, Toast.LENGTH_LONG).show();
    }

    // 只能放一些通用的方法，基本每个Activity都需要使用的方法，readDataBase最好不要放进来 ，
    // 如果是两个或两个以上的地方要使用,最好写一个工具类。
}
