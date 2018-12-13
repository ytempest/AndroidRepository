package com.ytempest.studentmanage.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

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

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class UpdatePasswordActivity extends BaseSkinActivity {

    private static final String TAG = "UpdatePasswordActivity";

    @BindView(R.id.et_new_password)
    protected EditText mNewPasswordEt;

    @BindView(R.id.et_confirm_password)
    protected EditText mConfirmPasswordEt;

    @BindView(R.id.bt_update_password)
    protected ModifiableButton mUpdatePasswordBt;

    @BindView(R.id.cb_new_password_status)
    protected CheckBox mNewPasswordStatusCb;

    @BindView(R.id.cb_confirm_password_status)
    protected CheckBox mConfirmPasswordStatusCb;


    private String mNewPassword;
    private String mConfirmPassword;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_update_password;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(UpdatePasswordActivity.this)
                .setTitle("修改密码")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }


    @Override
    protected void initView() {

        // 默认修改按钮不可用
        mUpdatePasswordBt.switchDisableStatus();

        // 设置Checkbox控制密码的显示
        mNewPasswordStatusCb.setOnCheckedChangeListener(new PasswordStatusChangeListener(mNewPasswordEt));
        mConfirmPasswordStatusCb.setOnCheckedChangeListener(new PasswordStatusChangeListener(mConfirmPasswordEt));


        mNewPasswordEt.addTextChangedListener(new TextWatcherListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNewPassword = s.toString();
                checkInput();
            }
        });

        mConfirmPasswordEt.addTextChangedListener(new TextWatcherListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mConfirmPassword = s.toString();
                checkInput();
            }
        });
    }

    private void checkInput() {
        if (!TextUtils.isEmpty(mNewPassword) && !TextUtils.isEmpty(mConfirmPassword)) {
            mUpdatePasswordBt.switchNormalStatus();
        } else {
            mUpdatePasswordBt.switchDisableStatus();
        }
    }

    @Override
    protected void initData() {

    }


    @OnClick(R.id.bt_update_password)
    protected void onUpdatePasswordClick(View view) {
        if (mNewPassword.length() != mConfirmPassword.length()) {
            showToastShort("密码长度不一致");
        } else if (!mNewPassword.equals(mConfirmPassword)) {
            showToastShort("密码不一致");
        } else {

            ApiService apiService = RetrofitClient.client().create(ApiService.class);
            Call<CommonResult> call;

            if (LoginInfoUtils.isStudent(UpdatePasswordActivity.this)) {
                call = apiService.updateStudentPassword(
                        LoginInfoUtils.getUserAccount(UpdatePasswordActivity.this),
                        mNewPassword);
            } else {
                call = apiService.updateTeacherPassword(
                        LoginInfoUtils.getUserAccount(UpdatePasswordActivity.this),
                        mNewPassword);
            }

            call.enqueue(new BaseCallback<CommonResult>() {
                @Override
                public void onSuccess(CommonResult result) {
                    showToastShort(result.getMsg());
                    finish();
                }
            });
        }
    }
}
