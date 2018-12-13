package com.ytempest.studentmanage.activity;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RadioButton;

import com.ytempest.baselibrary.view.NotScrollViewPager;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.adapter.FragmentPagerAdapter;
import com.ytempest.studentmanage.fragment.CourseFragment;
import com.ytempest.studentmanage.fragment.ManageFragment;
import com.ytempest.studentmanage.fragment.PersonalFragment;
import com.ytempest.studentmanage.util.LoginInfoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseSkinActivity {

    private static final int FRAGMENT_MANAGE = 0;
    private static final int FRAGMENT_COURSE = 0;
    private static final int FRAGMENT_PERSONAL = 1;

    @BindView(R.id.view_pager)
    protected NotScrollViewPager mViewPager;

    @BindView(R.id.rb_indicator_manage)
    protected RadioButton mManageRb;

    @BindView(R.id.rb_indicator_course)
    protected RadioButton mCourseRb;

    @BindView(R.id.rb_indicator_personal)
    protected RadioButton mPersonalRb;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        List<Fragment> fragments = new ArrayList<>();
        // 只要不是管理员，则添加课程Fragment
        if (!LoginInfoUtils.isManager(this)) {
            fragments.add(new CourseFragment());
            mCourseRb.setVisibility(View.VISIBLE);
            mManageRb.setVisibility(View.GONE);
        } else {
            fragments.add(new ManageFragment());
            mCourseRb.setVisibility(View.GONE);
            mManageRb.setVisibility(View.VISIBLE);
        }
        fragments.add(new PersonalFragment());

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), fragments));
        mViewPager.setOffscreenPageLimit(1);
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.rb_indicator_manage)
    public void onManageClick(View view) {
        setCurrentItem(FRAGMENT_MANAGE);
    }

    @OnClick(R.id.rb_indicator_course)
    public void onMessageClick(View view) {
        setCurrentItem(FRAGMENT_COURSE);
    }

    @OnClick(R.id.rb_indicator_personal)
    public void onPersonalClick(View view) {
        setCurrentItem(FRAGMENT_PERSONAL);
    }

    public void setCurrentItem(int item) {
        if (mViewPager.getCurrentItem() == item) {
            return;
        }
        mViewPager.setCurrentItem(item, false);
    }

}
