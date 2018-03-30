package com.ytempest.daydayantis.activity;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.ioc.CheckNet;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.security.MD5Utils;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.activity.mode.UserDataResult;
import com.ytempest.daydayantis.utils.UserLoginUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

/**
 * @author ytempest
 *         Description：这是一个用于登录的一个Activity，登录成功后会返回结果给上一层
 */
public class UserLoginActivity extends BaseSkinActivity {

    private static String TAG = "UserLoginActivity";

    @ViewById(R.id.ll_user_login_root)
    private LinearLayout mRootView;

    @ViewById(R.id.et_input_user)
    private EditText mEtUserName;

    @ViewById(R.id.et_input_password)
    private EditText mEtPassword;

    @ViewById(R.id.cb_password_status)
    private CheckBox mCbPasswordStatus;
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
                        .setTitle("登录")
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .setRightText("注册")
                        .setRightTextColor(R.color.title_bar_text_color)
                        .setRightClickListener(new View.OnClickListener() {
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
            showToastShort("手机号码不正确或密码错误");
        } else {
            // 登录成功
            isLoginSuccess = true;
            // 设置用户状态为已经登录
            UserLoginUtils.saveUserLoginStatus(UserLoginActivity.this, isLoginSuccess);
            Gson gson = new Gson();
            String userInfo = gson.toJson(result.getData());
            // 存储用户数据
            UserLoginUtils.saveUserInfo(UserLoginActivity.this, userInfo);
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

