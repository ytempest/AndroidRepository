package com.ytempest.baselibrary.util;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class FragmentManagerHelper {

    private int mFragmentContainer;
    private FragmentManager mFragmentManager;

    public FragmentManagerHelper(@IdRes int viewContainer, FragmentManager fragmentManager) {
        this.mFragmentContainer = viewContainer;
        this.mFragmentManager = fragmentManager;
    }


    public void add(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(mFragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    public void switchFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        List<Fragment> fragments = mFragmentManager.getFragments();
        for (Fragment f : fragments) {
            fragmentTransaction.hide(f);
        }

        if (!fragments.contains(fragment)) {
            fragmentTransaction.add(mFragmentContainer, fragment);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.commit();

    }

}
