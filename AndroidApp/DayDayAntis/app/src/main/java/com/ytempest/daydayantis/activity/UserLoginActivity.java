package com.ytempest.daydayantis.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.ioc.CheckNet;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.security.MD5Utils;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.common.PasswordStatusChangeListener;
import com.ytempest.daydayantis.common.TextWatcherAdapter;
import com.ytempest.daydayantis.data.UserDataResult;
import com.ytempest.daydayantis.utils.GeneralUtils;
import com.ytempest.daydayantis.utils.UserInfoUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.view.button.ModifiableButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

/**
 * @author ytempest
 *         Description：这是一个用于登录的一个Activity，登录成功后会返回结果给上一层
 */
public class UserLoginActivity extends BaseSkinActivity {

    private static String TAG = "UserLoginActivity";

    /**
     * 标志是否已经输入手机号码
     */
    private static int IS_INPUT_PHONE = 0x000033;
    /**
     * 标志是否已经输入密码
     */
    private static int IS_INPUT_PASSWORD = 0x110000;
    /**
     * 标志输入状态
     */
    private static int INPUT_STATUS = 0x00;

    @ViewById(R.id.ll_user_login_root)
    private LinearLayout mRootView;

    @ViewById(R.id.et_input_user)
    private EditText mEtUserName;

    @ViewById(R.id.et_input_password)
    private EditText mEtPassword;

    @ViewById(R.id.cb_password_status)
    private CheckBox mCbPasswordStatus;

    @ViewById(R.id.mbt_sign_in)
    private ModifiableButton mMbtSignIn;

    /**
     * 是否登录成功
     */
    private boolean isLoginSuccess = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_login;
    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(UserLoginActivity.this, mRootView)
                        .setTitle(R.string.activity_user_login_title_bar_text)
                        .setLeftIcon(R.drawable.icon_back)
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .setRightText(R.string.activity_user_login_title_bar_right_text)
                        .setRightTextColor(R.color.title_bar_text_color)
                        .setRightTextClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(UserRegisterActivity.class);
                            }
                        })
                        .build();

    }

    @Override
    protected void initView() {
        // 设置显示密码的CheckBox的监听
        mCbPasswordStatus.setOnCheckedChangeListener(new PasswordStatusChangeListener(mEtPassword));

        // 设置登录按钮为不可用状态
        mMbtSignIn.switchDisableStatus();

        // 通过动态监听用户名输入框和密码输入框的内容来改变登录按钮的可用状态

        // 1、监听用户名输入框的状态
        mEtUserName.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (GeneralUtils.judgePhoneQual(s.toString().trim())) {
                    INPUT_STATUS |= IS_INPUT_PHONE;
                } else {
                    INPUT_STATUS &= ~IS_INPUT_PHONE;
                }
                checkFinishInput();
            }
        });

        // 2、监听密码输入框的状态
        // 设置密码的最大位数
        final int minCount = getResources().getInteger(R.integer.user_password_min_count);
        final int maxCount = getResources().getInteger(R.integer.user_password_max_count);
        mEtPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= minCount && s.length() <= maxCount){
                    INPUT_STATUS |= IS_INPUT_PASSWORD;
                } else{
                    INPUT_STATUS &= ~IS_INPUT_PASSWORD;
                }
                checkFinishInput();
            }
        });
    }

    @Override
    protected void initData() {

    }

    private void checkFinishInput() {
        // 如果全部信息都输入
        if (INPUT_STATUS == (IS_INPUT_PHONE | IS_INPUT_PASSWORD)) {
            if (!mMbtSignIn.isEnabled()) {
                mMbtSignIn.switchNormalStatus();
            }
        } else {
            if (mMbtSignIn.isEnabled()) {
                mMbtSignIn.switchDisableStatus();
            }
        }
    }

    @OnClick(R.id.mbt_sign_in)
    @CheckNet
    private void onSignInClick(View view) {
        if (!checkUserAndPassword()) {
            return;
        }
        startSignIn();
    }


    /**
     * 对用户输入的用户名和密码进行判空
     */
    private boolean checkUserAndPassword() {
        String user = mEtUserName.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(user)) {
            showToastShort("请输入手机号码");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showToastShort("请输入密码");
            return false;
        }
        return true;
    }

    /**
     * 登录请求数据
     */
    private void startSignIn() {
        String userPhone = mEtUserName.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        HttpUtils.with(UserLoginActivity.this)
                .addParam("appid", "1")
                .addParam("cell_phone", userPhone)
                .addParam("password", MD5Utils.stringToMD5(password))
                .url("http://v2.ffu365.com/index.php?m=Api&c=Member&a=login")
                .post()
                .execute(new HttpCallBack<UserDataResult>() {
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void onSuccess(UserDataResult result) {
                        dealDataResult(result);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
    }

    /**
     * 处理登录返回的数据
     */
    private void dealDataResult(UserDataResult result) {
        int loginErrorCode = result.getErrcode();
        if (loginErrorCode == 0) {
            // 登录失败
            isLoginSuccess = false;
            showToastShort(result.getErrmsg());
        } else {
            // 登录成功
            isLoginSuccess = true;
            // 设置用户状态为已经登录
            UserInfoUtils.saveUserLoginStatus(UserLoginActivity.this, isLoginSuccess);
            Gson gson = new Gson();
            String userInfo = gson.toJson(result.getData());
            // 存储用户数据
            UserInfoUtils.saveUserInfo(UserLoginActivity.this, userInfo);
            changeUserStatue();
        }
    }


    /**
     * 登录成功后做后续处理
     */
    private void changeUserStatue() {
        finish();
    }

    @Override
    public void finish() {
        // 设置返回结果给启动本Activity的Activity或Fragment
        setActivityResult();
        super.finish();
    }

    /**
     * 设置登录结果
     */
    private void setActivityResult() {
        int isSuccess = isLoginSuccess ? RESULT_OK : RESULT_CANCELED;
        setResult(isSuccess);
    }

}

