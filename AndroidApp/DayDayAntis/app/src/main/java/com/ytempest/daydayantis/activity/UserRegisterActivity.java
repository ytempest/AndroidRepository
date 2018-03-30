package com.ytempest.daydayantis.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

public class UserRegisterActivity extends BaseSkinActivity {

    @ViewById(R.id.ll_user_register_root)
    private LinearLayout mRootView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_register;
    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(UserRegisterActivity.this, mRootView)
                        .setTitle("注册")
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
