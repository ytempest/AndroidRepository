package com.ytempest.youdo.activity;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import com.ytempest.baselibrary.util.FragmentManagerHelper;
import com.ytempest.framelibrary.base.SkinCompatActivity;
import com.ytempest.youdo.R;
import com.ytempest.youdo.fragment.HomeFragment;
import com.ytempest.youdo.fragment.MessageFragment;
import com.ytempest.youdo.fragment.PersonFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends SkinCompatActivity {

    @BindView(R.id.fragment_container)
    public FrameLayout mFragmentContainer;

    @BindView(R.id.rb_home)
    public RadioButton mHomeRb;
    @BindView(R.id.rb_message)
    public RadioButton mMessageRb;
    @BindView(R.id.rb_person)
    public RadioButton mPersonRb;

    private FragmentManagerHelper mFragmentHelper;
    private HomeFragment mHomeFragment;
    private MessageFragment mMessageFragment;
    private PersonFragment mPersonFragment;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_home;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        mFragmentHelper = new FragmentManagerHelper(R.id.fragment_container, getSupportFragmentManager());
        mHomeFragment = new HomeFragment();
        mMessageFragment = new MessageFragment();
        mPersonFragment = new PersonFragment();
        mFragmentHelper.add(mHomeFragment);
    }


    @OnClick(R.id.rb_home)
    public void onHomeClick(View view) {
        if (mHomeFragment == null) {
            mHomeFragment = new HomeFragment();
        }
        mHomeRb.setChecked(true);
        mFragmentHelper.switchFragment(mHomeFragment);
    }

    @OnClick(R.id.rb_message)
    public void onMessageClick(View view) {
        if (mMessageFragment == null) {
            mMessageFragment = new MessageFragment();
        }
        mMessageRb.setChecked(true);
        mFragmentHelper.switchFragment(mMessageFragment);
    }

    @OnClick(R.id.rb_person)
    public void onPersonClick(View view) {
        if (mPersonFragment == null) {
            mPersonFragment = new PersonFragment();
        }
        mPersonRb.setChecked(true);
        mFragmentHelper.switchFragment(mPersonFragment);
    }
}
