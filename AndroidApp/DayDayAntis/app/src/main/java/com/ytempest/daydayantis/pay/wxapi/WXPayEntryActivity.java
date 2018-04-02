package com.ytempest.daydayantis.pay.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.ytempest.daydayantis.R;

public class WXPayEntryActivity extends AppCompatActivity implements
		IWXAPIEventHandler {
	private IWXAPI api;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wx_pay_reback);
		api = WXAPIFactory.createWXAPI(this, null);
		api.handleIntent(getIntent(), this);
	}



	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {

	}

	@Override
	public void onResp(BaseResp resp) {// 执行完之后回调这个方法  resp.errCode== 0 :表示支付成功  签名失败
		Log.e("TAG","errStr = "+resp.errStr);
		Log.e("TAG","errCode = "+resp.errCode+"");
		if (resp.errCode == -2) {
			return;
		}
		if (resp.errCode == -1) {
			return;
		}
	}

	private void close() {
		this.finish();
		overridePendingTransition(0, 0);
	}

}