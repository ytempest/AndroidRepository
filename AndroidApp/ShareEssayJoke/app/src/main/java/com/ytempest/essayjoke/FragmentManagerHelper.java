package com.ytempest.essayjoke;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

/**
 * @author ytempest
 *         Description：Fragment管理类，用于显示和切换fragment
 */
public class FragmentManagerHelper {
    /**
     * 管理类FragmentManager
     */
    private FragmentManager mFragmentManager;
    /**
     * 容器布局id containerViewId
     */
    private int mContainerViewId;

    /**
     * @param fragmentManager 管理类FragmentManager
     * @param containerViewId 容器布局id containerViewId
     */
    public FragmentManagerHelper(@Nullable FragmentManager fragmentManager, @IdRes int containerViewId) {
        this.mFragmentManager = fragmentManager;
        this.mContainerViewId = containerViewId;
    }

    /**
     * 添加Fragment
     */
    public void add(Fragment fragment) {
        // 开启事务
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        // 第一个参数是Fragment的容器id
        fragmentTransaction.add(mContainerViewId, fragment);
        // 一定要commit事务
        fragmentTransaction.commit();
    }

    /**
     * 切换显示Fragment
     */
    public void switchFragment(Fragment fragment) {
        // 开启事务
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        // 1、先隐藏当前所有的Fragment
        List<Fragment> childFragments = mFragmentManager.getFragments();
        for (Fragment childFragment : childFragments) {
            fragmentTransaction.hide(childFragment);
        }

        // 2、如果容器里面没有要显示的 fragment 我们就添加，否则显示
        if (!childFragments.contains(fragment)) {
            fragmentTransaction.add(mContainerViewId, fragment);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.commit();
    }
}
