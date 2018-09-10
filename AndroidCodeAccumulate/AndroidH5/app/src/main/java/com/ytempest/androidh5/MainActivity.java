package com.ytempest.androidh5;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.lang.annotation.Target;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.web_view);

        initHtml();
    }

    private void initHtml() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl("file:///android_asset/login.html");

        mWebView.addJavascriptInterface(new JsInterface(), "android");
    }

    public void onClick(View view) {
        // mWebView.loadUrl("JavaScript:btnClick()");

        evaluateHaveValue();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void evaluateHaveValue() {
        // 可以调用mWebView.evaluateJavascript()方法，该方法只在安卓4.4以上版本适用
        mWebView.evaluateJavascript("sum(1,9)", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Toast.makeText(MainActivity.this, "调用JS函数的返回值为：" + value, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class JsInterface {
        @JavascriptInterface
        public String getString() {
            return "这段文字来自Android";
        }
    }
}
