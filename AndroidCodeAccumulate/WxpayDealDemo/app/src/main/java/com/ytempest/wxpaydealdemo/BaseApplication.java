package com.ytempest.wxpaydealdemo;

import android.app.Application;

import com.ytempest.pay.BaseWXPayActivity;
import com.ytempest.wxpay_annotation.WXPayEntry;

/**
 * @author ytempest
 *         Description：
 */
@WXPayEntry(
        entryClass = BaseWXPayActivity.class,
        packageName = "com.ytempest.wxpaydealdemo")
public class BaseApplication extends Application {


}
