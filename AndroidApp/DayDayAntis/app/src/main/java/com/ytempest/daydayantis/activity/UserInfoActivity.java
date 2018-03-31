package com.ytempest.daydayantis.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

public class UserInfoActivity extends BaseSkinActivity {

    @ViewById(R.id.ll_user_info_root)
    private LinearLayout mRootView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected void initTitle() {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(UserInfoActivity.this, mRootView)
                        .setTitle(R.string.activity_user_info_title_bar)
                        .setLeftIcon(R.drawable.icon_back)
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .build();


    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }
}
