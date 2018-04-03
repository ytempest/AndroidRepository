package com.ytempest.baselibrary.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ytempest.baselibrary.ioc.ViewUtils;


/**
 * @author ytempest
 */
public abstract class BaseFragment extends Fragment {

    protected View rootView;
    protected Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        rootView = View.inflate(mContext, getLayoutId(), null);

        // 加入注解
        ViewUtils.inject(rootView, this);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
    }

    /**
     * 启动Activity
     */
    protected void startActivity(Class<?> clazz) {
        Intent intent = new Intent(mContext, clazz);
        startActivity(intent);
    }

    /**
     * 启动Activity，带结果
     */
    protected void startActivityForResult(Class<?> clazz,int requetCode) {
        Intent intent = new Intent(mContext, clazz);
        startActivityForResult(intent, requetCode);
    }

    protected void showToastShort(String tip) {
        Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
    }

    protected void showToastShort(@StringRes int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    protected void showToastLong(String tip) {
        Toast.makeText(mContext, tip, Toast.LENGTH_LONG).show();
    }

    protected void showToastLong(@StringRes int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }



    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

}
