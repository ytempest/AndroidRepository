package com.ytempest.daydayantis.activity;

import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.util.ActivityStackManager;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.common.PasswordStatusChangeListener;
import com.ytempest.daydayantis.common.TextWatcherAdapter;
import com.ytempest.daydayantis.data.BaseDataResult;
import com.ytempest.daydayantis.data.UserDataResult;
import com.ytempest.daydayantis.utils.GeneralUtils;
import com.ytempest.daydayantis.utils.UserLoginUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.view.Button.ModifiableButton;
import com.ytempest.framelibrary.view.Button.VerifyButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

/**
 * @author ytempest
 *         Description：用户注册的页面
 */
public class UserRegisterActivity extends BaseSkinActivity {

    private static String TAG = "UserRegisterActivity";

    /**
     * 标志是否已经输入手机号码
     */
    private static int IS_INPUT_PHONE = 0x000033;
    /**
     * 标志是否已经输入验证码
     */
    private static int IS_INPUT_VERIFY_CODE = 0x002200;
    /**
     * 标志是否已经输入密码
     */
    private static int IS_INPUT_PASSWORD = 0x110000;
    /**
     * 标志输入状态
     */
    private static int INPUT_STATUS = 0x00;

    @ViewById(R.id.ll_user_register_root)
    private LinearLayout mRootView;

    @ViewById(R.id.et_register_phone)
    private EditText mEtPhone;

    @ViewById(R.id.et_register_verify_code)
    private EditText mEtVerificationCode;

    @ViewById(R.id.et_register_password)
    private EditText mEtPassword;

    @ViewById(R.id.cb_register_password_status)
    private CheckBox mCbPasswordStatus;
    @ViewById(R.id.vbt_get_code)
    private VerifyButton mVerifyButton;
    @ViewById(R.id.mbt_register)
    private ModifiableButton mMbtRegister;

    @ViewById(R.id.tv_protocol)
    private TextView mTvProtocol;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_register;
    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(UserRegisterActivity.this, mRootView)
                        .setTitle(R.string.activity_user_register_title_bar_text)
                        .setLeftIcon(R.drawable.icon_back)
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .build();
    }

    @Override
    protected void initView() {
        // 设置显示密码的CheckBox的监听
        mCbPasswordStatus.setOnCheckedChangeListener(new PasswordStatusChangeListener(mEtPassword));

        // 设置按钮为不可用状态
        mVerifyButton.switchDisableStatus();
        mMbtRegister.switchDisableStatus();

        mEtPhone.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 如果手机号码的格式正确，同时验证码按钮没有在倒计时
                if (GeneralUtils.judgePhoneQual(mEtPhone) && !mVerifyButton.isInCountDown()) {
                    mVerifyButton.switchNormalStatus();
                    INPUT_STATUS |= IS_INPUT_PHONE;
                } else {
                    // 如果验证码按钮是可用状态就设置为不可用，避免重复设置不可用状态
                    if (mVerifyButton.isEnabled()) {
                        mVerifyButton.switchDisableStatus();
                    }
                    INPUT_STATUS &= ~IS_INPUT_PHONE;
                }
                checkFinishInput();
            }

        });

        // 设置验证码的最大位数
        final int verificationCount = getResources().getInteger(R.integer.verification_code_count);
        mEtVerificationCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(verificationCount)});
        mEtVerificationCode.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= verificationCount) {
                    INPUT_STATUS |= IS_INPUT_VERIFY_CODE;
                } else {
                    INPUT_STATUS &= ~IS_INPUT_VERIFY_CODE;
                }
                checkFinishInput();
            }
        });

        // 设置密码的最大位数
        final int minCount = getResources().getInteger(R.integer.user_password_min_count);
        final int maxCount = getResources().getInteger(R.integer.user_password_max_count);
        mEtPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxCount)});
        mEtPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= minCount && s.length() <= maxCount) {
                    INPUT_STATUS |= IS_INPUT_PASSWORD;
                } else {
                    INPUT_STATUS &= ~IS_INPUT_PASSWORD;
                }
                checkFinishInput();
            }
        });

        // 设置验证码按钮倒计时结束的监听
        mVerifyButton.setOnCountDownListener(new VerifyButton.OnCountDownListener() {
            @Override
            public void onFinish() {
                // 倒计时结束就设置手机号码输入框可用
                mEtPhone.setEnabled(true);
            }
        });

        // 改变底部同意注册按钮的文字颜色
        changeProtocolTextColor();

    }



    @Override
    protected void initData() {

    }

    private void changeProtocolTextColor() {
        CharSequence charSequence;
        String colorValue = GeneralUtils.getColorValue(UserRegisterActivity.this, R.color.activity_user_register_protocol_text);
        String content = "我已阅读并同意<font color='" + colorValue + "'>《天天防腐》用户协议</font>";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            charSequence = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            charSequence = Html.fromHtml(content);
        }
        mTvProtocol.setText(charSequence);
    }

    private void checkFinishInput() {
        // 如果全部信息都输入
        if (INPUT_STATUS == (IS_INPUT_PHONE | IS_INPUT_VERIFY_CODE | IS_INPUT_PASSWORD)) {
            mMbtRegister.switchNormalStatus();
        } else {
            if (mMbtRegister.isEnabled()) {
                mMbtRegister.switchDisableStatus();
            }
        }
    }


    @OnClick(R.id.vbt_get_code)
    private void onGetVerificationCodeClick(View view) {
        // 设置手机号码输入框不可用
        mEtPhone.setEnabled(false);
        HttpUtils.with(UserRegisterActivity.this)
                .addParam("appid", "1")
                .addParam("sms_type", "3")
                .addParam("cell_phone", mEtPhone.getText().toString().trim())
                .url("http://v2.ffu365.com/index.php?m=Api&c=Util&a=sendVerifyCode")
                .post()
                .execute(new HttpCallBack<BaseDataResult>() {
                    @Override
                    public void onPreExecute() {
                        mVerifyButton.startRequestCode();
                    }

                    @Override
                    public void onSuccess(BaseDataResult result) {
                        if (result.getErrcode() == 1) {
                            mVerifyButton.startCountDown(60);
                        } else {
                            mVerifyButton.switchNormalStatus();
                            showToastShort(getResources().getString(R.string.activity_user_register_code_get_fail_text));
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        mVerifyButton.switchNormalStatus();
                        showToastShort(getResources().getString(R.string.activity_user_register_internet_error_text));
                    }
                });
    }

    @OnClick(R.id.mbt_register)
    private void onRegisterClick(View view) {
        HttpUtils.with(UserRegisterActivity.this)
                .addParam("appid", "1")
                .addParam("verify_code", mEtVerificationCode.getText().toString().trim())
                .addParam("cell_phone", mEtPhone.getText().toString().trim())
                .addParam("password", mEtPassword.getText().toString().trim())
                .url("http://v2.ffu365.com/index.php?m=Api&c=Member&a=register")
                .execute(new HttpCallBack<UserDataResult>() {
                    @Override
                    public void onPreExecute() {

                    }

                    @Override
                    public void onSuccess(UserDataResult result) {
                        dealRegisterResult(result);
                    }

                    @Override
                    public void onError(Exception e) {
                        showToastShort(getResources().getString(R.string.activity_user_register_internet_error_text));
                    }
                });
    }

    /**
     * 处理注册返回的数据
     */
    private void dealRegisterResult(UserDataResult result) {
        int loginErrorCode = result.getErrcode();
        if (loginErrorCode == 0) {
            // 登录失败
            String errorMsg = result.getErrmsg();
            showToastShort(errorMsg);
        } else {
            // 登录成功
            // 设置用户状态为已经登录
            UserLoginUtils.saveUserLoginStatus(UserRegisterActivity.this, true);
            Gson gson = new Gson();
            String userInfo = gson.toJson(result.getData());
            // 存储用户数据
            UserLoginUtils.saveUserInfo(UserRegisterActivity.this, userInfo);
            ActivityStackManager.getInstance().finishActivity(UserRegisterActivity.class);
            ActivityStackManager.getInstance().finishActivity(UserLoginActivity.class);
        }
    }

}

