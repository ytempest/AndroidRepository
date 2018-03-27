package com.ytempest.daydayantis;

import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.view.load.LoadView;
import com.ytempest.framelibrary.base.BaseSkinActivity;

public class DetailLinkActivity extends BaseSkinActivity {

    public static String URL_KEY = "url_key";

    @ViewById(R.id.web_view)
    private WebView mWebView;
    @ViewById(R.id.ld_loading)
    private LoadView mLoadView;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_detail_link;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        String url = getIntent().getStringExtra(URL_KEY);

        // 获取 WebView 的参数设置
        WebSettings webSettings = mWebView.getSettings();
        // 将图片调整到适合 WebView 的大小
        webSettings.setUseWideViewPort(true);
        // 支持 js
        webSettings.setJavaScriptEnabled(true);
        // 支持自动加载图片
        webSettings.setLoadsImagesAutomatically(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mLoadView.setVisibility(View.GONE);
            }

        });

        mWebView.loadUrl(url);
    }


    @Override
    protected void initData() {

    }
}
