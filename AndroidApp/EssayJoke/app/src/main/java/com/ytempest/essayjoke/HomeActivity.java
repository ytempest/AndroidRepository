package com.ytempest.essayjoke;


import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.essayjoke.fragment.FindFragment;
import com.ytempest.essayjoke.fragment.HomeFragment;
import com.ytempest.essayjoke.fragment.MessageFragment;
import com.ytempest.essayjoke.fragment.NewFragment;
import com.ytempest.framelibrary.base.BaseSkinActivity;

/**
 * @author ytempest
 *         Description: 主界面
 */
public class HomeActivity extends BaseSkinActivity {

    private HomeFragment mHomeFragment;
    private FindFragment mFindFragment;
    private NewFragment mNewFragment;
    private MessageFragment mMessageFragment;
    /**
     * Fragment的管理类
     */
    private FragmentManagerHelper mFragmentHelper;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_home;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initData() {
        mFragmentHelper = new FragmentManagerHelper(getSupportFragmentManager(), R.id.fl_main_tab);
        mHomeFragment = new HomeFragment();
        mFragmentHelper.add(mHomeFragment);
    }

    @Override
    protected void initView() {

    }

    @OnClick(R.id.rb_home)
    private void homeRadioButtonClick() {
        if (mHomeFragment == null) {
            mHomeFragment = new HomeFragment();
        }
        mFragmentHelper.switchFragment(mHomeFragment);
    }

    @OnClick(R.id.rb_find)
    private void findRadioButtonClick() {
        if (mFindFragment == null) {
            mFindFragment = new FindFragment();
        }
        mFragmentHelper.switchFragment(mFindFragment);
    }

    @OnClick(R.id.rb_new)
    private void newRadioButtonClick() {
        if (mNewFragment == null) {
            mNewFragment = new NewFragment();
        }
        mFragmentHelper.switchFragment(mNewFragment);
    }

    @OnClick(R.id.rb_message)
    private void messageRadioButtonClick() {
        if (mMessageFragment == null) {
            mMessageFragment = new MessageFragment();
        }
        mFragmentHelper.switchFragment(mMessageFragment);
    }
}
