package com.ytempest.daydayantis.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.view.load.LoadView;
import com.ytempest.daydayantis.R;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

public class MyCoinActivity extends BaseSkinActivity {

    private final static String mUrl = "http://app.ffu365.com/pages/about_coin.html";

    @ViewById(R.id.web_view)
    private WebView mWebView;
    @ViewById(R.id.load_view)
    private LoadView mLoadView;
    @ViewById(R.id.ll_root_view)
    private LinearLayout mRootView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_my_coin;
    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(MyCoinActivity.this, mRootView)
                        .setTitle(R.string.activity_my_coin_title_bar_text)
                        .setLeftIcon(R.drawable.icon_back)
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .setRightText(R.string.activity_my_coin_title_bar_right_text)
                        .setRightTextColor(R.color.title_bar_text_color)
                        .setRightClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO: 2018/4/3/003
                                showToastLong("账单");
                            }
                        })
                        .build();

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadView.setVisibility(View.GONE);
            }
        });
        mWebView.loadUrl(mUrl);
    }

    @OnClick(R.id.bt_recharge_coin)
    private void onRechargeCoinClick(View view) {
        startActivity(RechargeCoinActivity.class);
    }


}
