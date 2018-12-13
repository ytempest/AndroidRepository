package com.ytempest.daydayantis.activity;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.common.TextWatcherAdapter;
import com.ytempest.daydayantis.data.AlipayDataResult;
import com.ytempest.daydayantis.data.PackageListData;
import com.ytempest.daydayantis.data.UserDataResult;
import com.ytempest.daydayantis.data.WxpayDataResult;
import com.ytempest.daydayantis.pay.alipay.AlipayUtils;
import com.ytempest.daydayantis.utils.GeneralUtils;
import com.ytempest.daydayantis.utils.UserInfoUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.view.button.ModifiableButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

import java.util.List;

/**
 * @author ytempest
 *         Description：这是一个购买金币的Activity
 */
public class RechargeCoinActivity extends BaseSkinActivity {

    /**
     * 向后台请求调用支付宝、微信支付的参数，固定的，不可修改
     */
    private final static String PAY_USE_ALIPAY = "1";
    private final static String PAY_USE_WXPAY = "2";

    @ViewById(R.id.ll_root_view)
    private LinearLayout mRootView;

    @ViewById(R.id.rv_package_list)
    private RecyclerView mPackageList;

    @ViewById(R.id.et_coin_num)
    private EditText mEtCoinNum;
    @ViewById(R.id.cb_alipay)
    private CheckBox mCbAlipay;
    @ViewById(R.id.cb_wxpay)
    private CheckBox mCbWxpay;

    @ViewById(R.id.bt_confirm_pay)
    private ModifiableButton mBtConfirmPay;

    private IWXAPI mWXAPI;

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
        mEtCoinNum.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 如果首位数字时0就重置EditText
                if (s.toString().trim().startsWith("0")) {
                    mEtCoinNum.setText("");
                } else {
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
            }
        });

        // 设置点击输入框就将光标移动到文本末尾
        mEtCoinNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 把光标移动到最后
                GeneralUtils.setCursorLast(mEtCoinNum);
            }
        });

        // 初始化购买金币的套餐列表
        requestPackageList();

    }

    @Override
    protected void initData() {
        // 微信的id String
        mWXAPI = WXAPIFactory.createWXAPI(RechargeCoinActivity.this, "wxa8080d15a32e2ff7");
        // 将 App 注册到微信
        mWXAPI.registerApp("wxa8080d15a32e2ff7");


    }

    /**
     * 向后台请求获取金币套餐列表数据
     */
    private void requestPackageList() {
        // 请求数据
        HttpUtils.with(RechargeCoinActivity.this)
                .addParam("appid", "1")
                .url("http://v2.ffu365.com/index.php?m=Api&c=Member&a=coinSale")
                .post()
                .execute(new HttpCallBack<PackageListData>() {
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void onSuccess(PackageListData result) {
                        showPackageList(result.getData().getMeal());
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
    }

    /**
     * 显示金币套餐数据
     */
    private void showPackageList(List<PackageListData.DataBean.MealBean> data) {
        mPackageList.setAdapter(new PackageListAdapter(RechargeCoinActivity.this, data));
        mPackageList.setLayoutManager(new LinearLayoutManager(RechargeCoinActivity.this));
        mPackageList.addItemDecoration(new DividerItemDecoration(RechargeCoinActivity.this, DividerItemDecoration.VERTICAL));
    }


    @OnClick(R.id.ll_alipay)
    private void onAlipayClick(View view) {
        mCbAlipay.setChecked(true);
        mCbWxpay.setChecked(false);
    }


    @OnClick(R.id.ll_wxpay)
    private void onWxpayClick(View view) {
        mCbWxpay.setChecked(true);
        mCbAlipay.setChecked(false);
    }

    /**
     * 确认支付按钮的点击事件
     */
    @OnClick(R.id.bt_confirm_pay)
    private void onConfirmPayClick(View view) {
        String coinNumber = mEtCoinNum.getText().toString().trim();
        if (TextUtils.isEmpty(coinNumber)) {
            return;
        }

        // 判断是哪一种支付方式
        if (mCbAlipay.isChecked()) {
            useAlipay(coinNumber);
        } else if (mCbWxpay.isChecked()) {
            useWxPay(coinNumber);
        }

    }

    /**
     * 使用支付宝支付
     */
    private void useAlipay(String coinNumber) {
        // 1、 开始向后台获取使用支付宝支付的一些参数
        startPay(coinNumber, PAY_USE_ALIPAY, new HttpCallBack<AlipayDataResult>() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void onSuccess(AlipayDataResult result) {
                callLocalAlipay(result.getData());
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    /**
     * 支付宝支付后的支付结果回调
     */
    private void callLocalAlipay(AlipayDataResult.DataBean data) {
        String payInfo = data.getPayinfo();
        AlipayUtils alipayUtils = new AlipayUtils(RechargeCoinActivity.this);
        // 2、调用本地的支付宝支付
        alipayUtils.alipay(payInfo);

        // 3、设置支付宝支付结果回调
        alipayUtils.setPayListener(new AlipayUtils.PayListener() {
            @Override
            public void paySuccess() {
                showToastShort(getStringById(R.string.activity_recharge_coin_pay_success));
                finish();
            }

            @Override
            public void payFail() {
                showToastShort(getStringById(R.string.activity_recharge_coin_pay_fail));
            }
        });
    }


    /**
     * 使用微信支付
     */
    private void useWxPay(String coinNumber) {
        //1、开始向后台获取使用微信支付的一些参数
        startPay(coinNumber, PAY_USE_WXPAY, new HttpCallBack<WxpayDataResult>() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void onSuccess(WxpayDataResult result) {
                callLocalWxpay(result.getData());
            }

            @Override
            public void onError(Exception e) {

            }
        });


    }

    private void callLocalWxpay(WxpayDataResult.DataBean data) {
        // 2、调用本地的微信进行支付
        WxpayDataResult.DataBean.PayinfoBean payInfo = data.getPayinfo();
        PayReq payReq = new PayReq();
        payReq.appId = payInfo.getAppid();
        payReq.partnerId = payInfo.getPartnerid();
        payReq.prepayId = payInfo.getPrepayid();
        payReq.packageValue = payInfo.getPackage_value();
        payReq.nonceStr = payInfo.getNoncestr();
        payReq.timeStamp = payInfo.getTimestamp() + "";
        payReq.sign = payInfo.getSign();
        mWXAPI.sendReq(payReq);

        // 3、处理微信支付结果的回调，回调结果在 WXPayEntryActivity 中
    }

    /**
     * 根据 payWay 向后台获取支付需要的一些参数
     *
     * @param coinNumber   金币数量
     * @param payWay       支付方式
     * @param httpCallBack 后台请求结果回调
     */
    private void startPay(String coinNumber, String payWay, HttpCallBack<?> httpCallBack) {
        String userInfo = UserInfoUtils.getUserInfo(RechargeCoinActivity.this);
        if (TextUtils.isEmpty(userInfo)) {
            showToastShort(getStringById(R.string.activity_recharge_coin_lose_login));
            startActivity(UserLoginActivity.class);
            return;
        }
        UserDataResult.DataBean dataBean = new Gson().fromJson(userInfo, UserDataResult.DataBean.class);
        HttpUtils.with(RechargeCoinActivity.this)
                .addParam("appid", "1")
                .addParam("uid", dataBean.getMember_info().getUid())
                .addParam("coin_nums", coinNumber)
                .addParam("payment_tool", payWay)
                .url("http://v2.ffu365.com/index.php?m=Api&c=V2Payment&a=coinPrepareToPay")
                .post()
                .execute(httpCallBack);
    }


    /**
     * @author ytempest
     *         Description：金币套餐列表的RecyclerView适配器
     */
    private class PackageListAdapter extends CommonRecyclerAdapter<PackageListData.DataBean.MealBean> {

        PackageListAdapter(Context context, List<PackageListData.DataBean.MealBean> dataList) {
            super(context, dataList, R.layout.item_rv_package_list);
        }

        @Override
        protected void bindViewData(CommonViewHolder holder, final PackageListData.DataBean.MealBean item) {
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtCoinNum.setText(item.getMeal_coin());
                    // 把光标移动到最后
                    GeneralUtils.setCursorLast(mEtCoinNum);
                }
            });
            holder.setText(R.id.tv_purchase_coin_num, item.getMeal_name());
            holder.setText(R.id.tv_purchase_desc, item.getMeal_desc());
        }
    }
}
