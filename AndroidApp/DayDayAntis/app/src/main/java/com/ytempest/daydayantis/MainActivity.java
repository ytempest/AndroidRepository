package com.ytempest.daydayantis;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.fragment.CollectFragment;
import com.ytempest.daydayantis.fragment.HomeFragment;
import com.ytempest.daydayantis.fragment.MessageFragment;
import com.ytempest.daydayantis.fragment.PersonalFragment;
import com.ytempest.daydayantis.fragment.adapter.MainPagerAdapter;
import com.ytempest.framelibrary.base.BaseSkinActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseSkinActivity {

    @ViewById(R.id.vp_fragment)
    private ViewPager mViewPager;

    @ViewById(R.id.rb_home)
    private RadioButton mRbHome;

    @ViewById(R.id.rb_collect)
    private RadioButton mRbCollect;

    @ViewById(R.id.ll_publish)
    private LinearLayout mLlPublish;

    @ViewById(R.id.rb_message)
    private RadioButton mRbMessage;

    @ViewById(R.id.rb_personal)
    private RadioButton mRbPersonal;
    private List<Fragment> mFragments;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initTitle() {
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        mFragments = new ArrayList<>();
        mFragments.add(new HomeFragment());
        mFragments.add(new CollectFragment());
        mFragments.add(new MessageFragment());
        mFragments.add(new PersonalFragment());

        mViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), mFragments));
        mViewPager.addOnPageChangeListener(new MainViewPagerListener());
    }


    @OnClick(R.id.rb_home)
    public void homeClick(View view) {
        mViewPager.setCurrentItem(0, false);
    }

    @OnClick(R.id.rb_collect)
    public void collectClick(View view) {
        mViewPager.setCurrentItem(1, false);
    }

    @OnClick(R.id.ll_publish)
    public void publishClick(View view) {

    }

    @OnClick(R.id.rb_message)
    public void messageClick(View view) {
        mViewPager.setCurrentItem(2, false);
    }

    @OnClick(R.id.rb_personal)
    public void personalClick(View view) {
        mViewPager.setCurrentItem(3, false);
    }


    private class MainViewPagerListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    mRbHome.setChecked(true);
                    break;

                case 1:
                    mRbCollect.setChecked(true);
                    break;

                case 2:
                    mRbMessage.setChecked(true);
                    break;

                case 3:
                    mRbPersonal.setChecked(true);
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}

