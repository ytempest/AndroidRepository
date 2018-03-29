package com.ytempest.daydayantis.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.gson.Gson;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.security.MD5Utils;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.activity.mode.UserDataResult;
import com.ytempest.daydayantis.utils.UserLoginUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.http.HttpCallBack;

public class UserLoginActivity extends BaseSkinActivity {

    private static String TAG = "UserLoginActivity";
    public static String IS_SIGN_IN_SUCCESS = "is_sign_in_success";

    @ViewById(R.id.et_input_user)
    private EditText mEtUserName;

    @ViewById(R.id.et_input_password)
    private EditText mEtPassword;

    @ViewById(R.id.cb_password_status)
    private CheckBox mCbPasswordStatus;

    @ViewById(R.id.bt_sign_in)
    private Button mBtSignIn;

    private boolean isLoginSuccess = false;

    @Override
    protected int getLayoutResId() {

        return R.layout.activity_user_login;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        mCbPasswordStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 显示密码
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // 隐藏密码
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

                // 把光标移动到最后
                Editable editable = mEtPassword.getText();
                Selection.setSelection(editable, editable.length());
            }
        });
    }

    @Override
    protected void initData() {

    }


    @OnClick(R.id.bt_sign_in)
    private void onSignInClick(View view) {
        if (!checkUserAndPassword()) {
            return;
        }
        startSignIn();
    }


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
     * 登录成功后做后续处理
     */
    private void changeUserStatue() {
        finish();
    }

    /**
     * 处理登录返回的数据
     */
    private void dealDataResult(UserDataResult result) {
        int loginErrorCode = result.getErrcode();
        // 登录失败
        if (loginErrorCode == 0) {
            showToastShort("手机号码不正确或密码错误");
        } else {
            isLoginSuccess = true;
            // 设置用户状态为已经登录
            UserLoginUtils.saveUserLoginStatus(UserLoginActivity.this,isLoginSuccess);
            Gson gson = new Gson();
            String userInfo = gson.toJson(result.getData());
            // 存储用户数据
            UserLoginUtils.setUserInfo(UserLoginActivity.this, userInfo);

            changeUserStatue();
        }
    }

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
     * 登录结果返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int isSuccess = isLoginSuccess ? RESULT_OK : RESULT_CANCELED;
        setResult(isSuccess);
    }
}

