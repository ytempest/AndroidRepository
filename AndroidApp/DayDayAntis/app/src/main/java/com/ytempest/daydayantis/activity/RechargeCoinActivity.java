package com.ytempest.daydayantis.activity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.common.TextWatcherAdapter;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.Button.ModifiableButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

public class RechargeCoinActivity extends BaseSkinActivity {

    @ViewById(R.id.ll_root_view)
    private LinearLayout mRootView;
    @ViewById(R.id.et_coin_num)
    private EditText mEtCoinNum;
    @ViewById(R.id.cb_alipay)
    private CheckBox mCbAlipay;
    @ViewById(R.id.cb_wxpay)
    private CheckBox mCbWxpay;
    @ViewById(R.id.bt_confirm_pay)
    private ModifiableButton mBtConfirmPay;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_recharge_coin;
    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(RechargeCoinActivity.this, mRootView)
                        .setTitle(R.string.activity_recharge_title_text)
                        .setLeftIcon(R.drawable.icon_back)
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .build();


    }

    @Override
    protected void initView() {
        // 设置按钮不可用，当用户输入了购买金币数量再设置可用
        mBtConfirmPay.switchDisableStatus();
        mEtCoinNum.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 当用户输入了数量，设置按钮可用
                if (s.length() > 0) {
                    if (!mBtConfirmPay.isEnabled()) {
                        mBtConfirmPay.switchNormalStatus();
                    }
                } else {
                    if (mBtConfirmPay.isEnabled()) {
                        mBtConfirmPay.switchDisableStatus();
                    }
                }
            }
        });

    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.ll_alipay)
    private void onAlipayClick(View view) {
        mCbAlipay.setChecked(true);
        mCbWxpay.setChecked(false);
    }

    @OnClick(R.id.ll_wxpay)
    private void onWxpayClick(View view) {
        mCbAlipay.setChecked(false);
        mCbWxpay.setChecked(true);
    }

    @OnClick(R.id.bt_confirm_pay)
    private void onConfirmPayClick(View view) {

    }


}
