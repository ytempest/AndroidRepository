package com.ytempest.studentmanage.activity;

import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.button.ModifiableButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.listener.PasswordStatusChangeListener;
import com.ytempest.studentmanage.listener.TextWatcherListener;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.util.LoginInfoUtils;
import com.ytempest.studentmanage.util.ResourcesUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author ytempest
 *         Description：
 */
public class UserLoginActivity extends BaseSkinActivity {

    /**
     * 标记账号是否已经输入完毕的标记位
     */
    private static final int FINISH_INPUT_ACCOUNT = 0x000011;
    /**
     * 标记密码是否已经输入完毕的标记位
     */
    private static final int FINISH_INPUT_PASSWORD = 0x001100;
    private int mInputStatus;

    @BindView(R.id.root_view)
    protected LinearLayout mRootView;

    @BindView(R.id.tv_protocol)
    protected TextView mProtocolTv;

    @BindView(R.id.et_account)
    protected EditText mAccountEt;

    @BindView(R.id.et_password)
    protected EditText mPasswordEt;

    @BindView(R.id.cb_password_status)
    protected CheckBox mPasswordStatusCb;

    @BindView(R.id.login)
    protected ModifiableButton mLoginBt;

    @BindView(R.id.rg_user_type)
    protected RadioGroup mUserTypeRg;

    private ApiService mApiService;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_login;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(UserLoginActivity.this, mRootView)
                .setTitle("请先登录")
                .hideLeftIcon()
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }

    @Override
    protected void initView() {
        // 设置底部的协议文字
        mProtocolTv.setText(Html.fromHtml("使用即为同意<font color=#2EB3E8>《学生管理协议》</font>"));

        // 设置Checkbox控制密码的显示
        mPasswordStatusCb.setOnCheckedChangeListener(new PasswordStatusChangeListener(mPasswordEt));

        // 将登录按钮切换到默认不可用
        mLoginBt.switchDisableStatus();

        // 设置控制输入账号的长度以控制登录按钮的可用性
        final int minAccountLength = ResourcesUtils.getInt(R.integer.user_account_min_count);
        final int maxAccountLength = ResourcesUtils.getInt(R.integer.user_account_max_count);
        mAccountEt.addTextChangedListener(new TextWatcherListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= minAccountLength && s.length() <= maxAccountLength) {
                    mInputStatus |= FINISH_INPUT_ACCOUNT;
                } else {
                    mInputStatus &= ~FINISH_INPUT_ACCOUNT;
                }
                checkFinishInput();
            }
        });

        // 设置控制输入密码的长度以控制登录按钮的可用性
        final int minPasswordLength = ResourcesUtils.getInt(R.integer.user_password_min_count);
        final int maxPasswordLength = ResourcesUtils.getInt(R.integer.user_password_max_count);
        mPasswordEt.addTextChangedListener(new TextWatcherListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= minPasswordLength && s.length() <= maxPasswordLength) {
                    mInputStatus |= FINISH_INPUT_PASSWORD;
                } else {
                    mInputStatus &= ~FINISH_INPUT_PASSWORD;
                }
                checkFinishInput();
            }
        });
    }

    @Override
    protected void initData() {
        mApiService = RetrofitClient.client().create(ApiService.class);
    }

    @OnClick(R.id.login)
    protected void onLoginClick(View view) {
        final String account = mAccountEt.getText().toString().trim();
        final String password = mPasswordEt.getText().toString().trim();
        final String userType = getUserType();

        final LoadingDialog loadingDialog = new LoadingDialog(UserLoginActivity.this);
        loadingDialog.show();

        mApiService.login(account, password, userType)
                .enqueue(new BaseCallback<CommonResult>() {
                    @Override
                    public void onSuccess(CommonResult result) {
                        loadingDialog.dismiss();
                        if (result.getCode() == 0) {
                            LoginInfoUtils.saveUserLoginInfo(UserLoginActivity.this, userType, account);
                            startActivity(MainActivity.class);
                            finish();
                        } else {
                            showToastShort(result.getMsg());
                        }
                    }
                });
    }

    /**
     * 获取用户选择的用户类型
     */
    private String getUserType() {
        int checkId = mUserTypeRg.getCheckedRadioButtonId();
        if (checkId == R.id.rb_student) {
            return LoginInfoUtils.USER_TYPE_STUDENT;
        } else if (checkId == R.id.rb_teacher) {
            return LoginInfoUtils.USER_TYPE_TEACHER;
        } else if (checkId == R.id.rb_manager) {
            return LoginInfoUtils.USER_TYPE_MANAGER;
        }

        return "";
    }


    /**
     * 检测用户已经输入完账号和密码，并根据输入结果切换登录按钮的可用状态
     */
    public void checkFinishInput() {
        if (mInputStatus == (FINISH_INPUT_ACCOUNT | FINISH_INPUT_PASSWORD)) {
            mLoginBt.switchNormalStatus();
        } else {
            mLoginBt.switchDisableStatus();
        }
    }
}
