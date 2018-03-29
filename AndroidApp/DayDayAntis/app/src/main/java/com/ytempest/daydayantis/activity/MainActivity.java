package com.ytempest.daydayantis.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.fragment.CollectFragment;
import com.ytempest.daydayantis.fragment.HomeFragment;
import com.ytempest.daydayantis.fragment.MessageFragment;
import com.ytempest.daydayantis.fragment.PersonalFragment;
import com.ytempest.daydayantis.fragment.adapter.MainPagerAdapter;
import com.ytempest.daydayantis.utils.UserLoginUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseSkinActivity {

    private static String TAG = "MainActivity";

    private final static int LOGIN_REQUEST_CODE = 100;

    private final int FRAGMENT_HOME = 0;
    private final int FRAGMENT_COLLECT = 1;
    private final int FRAGMENT_MESSAGE = 2;
    private final int FRAGMENT_PERSONAL = 3;
    private int mCurrentItem = FRAGMENT_HOME;

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


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initTitle() {
    }

    @Override
    protected void initView() {
        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new HomeFragment());
        mFragments.add(new CollectFragment());
        mFragments.add(new MessageFragment());
        mFragments.add(new PersonalFragment());

        mViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), mFragments));
        // 缓存ViePager的页数，缓存当前页面的左右两边各3个
        mViewPager.setOffscreenPageLimit(3);
    }

    @Override
    protected void initData() {
    }


    /**
     * 首页按钮
     */
    @OnClick(R.id.rb_home)
    public void homeClick(View view) {
        setCurrentItem(FRAGMENT_HOME);
    }

    /**
     * 收藏按钮
     */
    @OnClick(R.id.rb_collect)
    public void collectClick(View view) {
        // 如果没有登录
        if (!UserLoginUtils.isUserLogin(MainActivity.this)) {
            startActivityForResult(UserLoginActivity.class, LOGIN_REQUEST_CODE);
        } else {
            setCurrentItem(FRAGMENT_COLLECT);
        }
    }


    /**
     * 发布按钮
     */
    @OnClick(R.id.ll_publish)
    public void publishClick(View view) {

    }

    /**
     * 消息按钮
     */
    @OnClick(R.id.rb_message)
    public void messageClick(View view) {
        setCurrentItem(FRAGMENT_MESSAGE);
    }

    /**
     * 个人中心按钮
     */
    @OnClick(R.id.rb_personal)
    public void personalClick(View view) {
        setCurrentItem(FRAGMENT_PERSONAL);

    }

    /**
     * 启动Activity的返回结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 如果是用户登录
            case LOGIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    setCurrentItem(FRAGMENT_COLLECT);
                } else {
                    setCurrentItem(mCurrentItem);
                }
                break;
            default:
                break;

        }
    }

    /**
     * 根据位置设置指定的ViewPager页面同时将 RadioButton 的状态更改
     */
    public void setCurrentItem(int position) {
        mCurrentItem = position;
        switch (position) {
            case FRAGMENT_HOME:
                mRbHome.setChecked(true);
                break;
            case FRAGMENT_COLLECT:
                mRbCollect.setChecked(true);
                break;
            case FRAGMENT_MESSAGE:
                mRbMessage.setChecked(true);
                break;
            case FRAGMENT_PERSONAL:
                mRbPersonal.setChecked(true);
                break;
            default:
                break;
        }
        mViewPager.setCurrentItem(position, false);
    }
}

