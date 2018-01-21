package com.ytempest.test2;


import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.ioc.ViewUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;

/**
 * @author Administrator
 */
public class MainActivity extends BaseSkinActivity {
    private static final String TAG = "TestActivity";

    @ViewById(R.id.tab_layout)
    private TabLayout mTabLayout;

    @ViewById(R.id.view_pager)
    private ViewPager mViewPager;

    private String[] mItems = {"one", "two", "three", "four", "five", "six","seven","eight","night","ten"};


    /**
     * 获取布局layout的Id
     *
     * @return 布局Id
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initTitle() {
        ViewUtils.inject(this);
    }



    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ItemFragment.newInstance(mItems[position]);
            }

            @Override
            public int getCount() {
                return mItems.length;
            }
        };
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mItems.length; i++) {
            //获得每一个tab
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                //给每一个tab设置view
                tab.setCustomView(R.layout.tab_item);
            }
            if (i == 0) {
                // 设置第一个tab的TextView是被选择的样式
                //第一个tab被选中
                TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tab_text);
                textView.setTextColor(Color.RED);
                textView.setSelected(true);
            }
            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tab_text);
            textView.setText(mItems[i]);//设置tab上的文字
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e(TAG, "onTabSelected:tab.getPosition() --> " + tab.getPosition());
                TextView view = (TextView)tab.getCustomView().findViewById(R.id.tab_text);
                view.setTextColor(Color.RED);
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.e(TAG, "onTabUnselected:tab.getPosition() --> " + tab.getPosition());
                TextView textView = (TextView)tab.getCustomView().findViewById(R.id.tab_text);
                textView.setTextColor(Color.BLACK);
                textView.setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.e(TAG, "onTabReselected:tab.getPosition() --> " + tab.getPosition());
                showToastShort("you click " + mItems[tab.getPosition()]);
            }
        });

    }
}


