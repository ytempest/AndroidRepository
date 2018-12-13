package com.ytempest.studentmanage.activity;

import android.text.Selection;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.button.ModifiableButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.listener.LastCharLongClickListener;
import com.ytempest.studentmanage.listener.TextChangeListener;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.StudentInfoResult;
import com.ytempest.studentmanage.model.TeacherInfoResult;
import com.ytempest.studentmanage.util.LoginInfoUtils;
import com.ytempest.studentmanage.util.PatternUtils;
import com.ytempest.studentmanage.util.ResourcesUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class UserInfoActivity extends BaseSkinActivity {

    private static final String TAG = "UserInfoActivity";

    private static final int IS_PHONE_CHANGE = 0x000001;
    private static final int IS_ADDRESS_CHANGE = 0x000010;
    private int isChanged = 0;

    @BindView(R.id.iv_user_image)
    protected ImageView mImageIv;

    @BindView(R.id.tv_user_account)
    protected TextView mAccountTv;

    @BindView(R.id.tv_user_name)
    protected TextView mNameTv;

    @BindView(R.id.tv_user_sex)
    protected TextView mSexTv;

    @BindView(R.id.et_user_phone)
    protected EditText mPhoneEt;

    @BindView(R.id.et_user_address)
    protected EditText mAddressEt;

    @BindView(R.id.tv_user_department)
    protected TextView mDepartmentTv;

    @BindView(R.id.tv_user_major)
    protected TextView mMajorTv;

    @BindView(R.id.tv_user_grade)
    protected TextView mGradeTv;

    @BindView(R.id.tv_user_class)
    protected TextView mClassTv;

    @BindView(R.id.tv_user_political_status)
    protected TextView mPoliticalStatusTv;

    @BindView(R.id.tv_user_enrollment_date)
    protected TextView mEnrollmentDateTv;

    @BindView(R.id.tv_user_state)
    protected TextView mStateTv;

    @BindView(R.id.bt_user_update)
    protected ModifiableButton mConfirmUpdateBt;

    /**
     * 当用户类型为教师，则将入学日期改为入职日期
     */
    @BindView(R.id.tv_user_start_time)
    protected TextView mStartTimeTv;

    private String userAccount;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(UserInfoActivity.this)
                .setTitle("修改个人信息")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();

    }

    @Override
    protected void initView() {
        // 为两个EditText添加可以移动光标的监听器
        mPhoneEt.setOnLongClickListener(new LastCharLongClickListener(mPhoneEt));
        mAddressEt.setOnLongClickListener(new LastCharLongClickListener(mAddressEt));

        // 默认修改按钮不可用
        mConfirmUpdateBt.switchDisableStatus();

        Selection.setSelection(mPhoneEt.getText(), mPhoneEt.getText().length());

    }

    private void watchTextChange() {
        // 监听EditText的文字是否已经更改，如果更改则改变修改按钮为可用
        mPhoneEt.addTextChangedListener(new TextChangeListener(mPhoneEt) {
            @Override
            public void onTextChanged(boolean hasChanged, String text) {
                if (hasChanged && !TextUtils.isEmpty(text)) {
                    isChanged |= IS_PHONE_CHANGE;
                } else {
                    isChanged &= ~IS_PHONE_CHANGE;
                }
                checkTextChange();
            }
        });

        mAddressEt.addTextChangedListener(new TextChangeListener(mAddressEt) {
            @Override
            public void onTextChanged(boolean hasChanged, String text) {
                if (hasChanged && !TextUtils.isEmpty(text)) {
                    isChanged |= IS_ADDRESS_CHANGE;
                } else {
                    isChanged &= ~IS_ADDRESS_CHANGE;
                }
                checkTextChange();
            }
        });
    }


    @Override
    protected void initData() {

        ApiService apiService = RetrofitClient.client().create(ApiService.class);
        userAccount = LoginInfoUtils.getUserAccount(UserInfoActivity.this);

        final LoadingDialog loadingDialog = new LoadingDialog(UserInfoActivity.this);
        loadingDialog.show();

        // 根据用户的类型做不同的处理
        if (LoginInfoUtils.isStudent(UserInfoActivity.this)) {
            apiService.getStudentInfoByStudentId(userAccount)
                    .enqueue(new BaseCallback<StudentInfoResult>() {
                        @Override
                        public void onSuccess(StudentInfoResult result) {
                            loadStudentDataToLayout(result.getData(), loadingDialog);
                        }
                    });

        } else if (LoginInfoUtils.isTeacher(UserInfoActivity.this)) {
            // 如果是教师则改变布局
            changeLayoutForTeacher();
            apiService.getTeacherInfoByTeacherId(userAccount)
                    .enqueue(new BaseCallback<TeacherInfoResult>() {
                        @Override
                        public void onSuccess(TeacherInfoResult result) {
                            loadTeacherDataToLayout(result.getData(), loadingDialog);
                        }
                    });
        }

    }

    private void changeLayoutForTeacher() {
        ((LinearLayout) mMajorTv.getParent()).setVisibility(View.GONE);
        ((LinearLayout) mGradeTv.getParent()).setVisibility(View.GONE);
        ((LinearLayout) mClassTv.getParent()).setVisibility(View.GONE);
        mStartTimeTv.setText("入职日期");
    }

    private void loadStudentDataToLayout(StudentInfoResult.DataBean data, LoadingDialog loadingDialog) {
        showImage(RetrofitClient.URL + data.getImage());
        mAccountTv.setText(String.valueOf(data.getStudentId()));
        mNameTv.setText(data.getName());
        mSexTv.setText("male".equals(data.getSex()) ? "男" : "女");
        mPhoneEt.setText(data.getPhone());
        mAddressEt.setText(data.getAddress());
        mDepartmentTv.setText(data.getDepartmentName());
        mMajorTv.setText(data.getMajorName());
        mGradeTv.setText(String.valueOf(data.getGrade()));
        mClassTv.setText(data.getClassName());
        mPoliticalStatusTv.setText(data.getPoliticalStatus());
        mEnrollmentDateTv.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(data.getEnrollmentDate())));
        String state = data.getState() == 1 ? "正常" : data.getState() == 0 ? "休学" : "已毕业";
        mStateTv.setText(state);

        // 监听是否有更改
        watchTextChange();

        loadingDialog.dismiss();
    }

    private void loadTeacherDataToLayout(TeacherInfoResult.DataBean data, LoadingDialog loadingDialog) {
        showImage(RetrofitClient.URL + data.getImage());
        mAccountTv.setText(String.valueOf(data.getTeacherId()));
        mNameTv.setText(data.getName());
        mSexTv.setText("male".equals(data.getSex()) ? "男" : "女");
        mPhoneEt.setText(data.getPhone());
        mAddressEt.setText(data.getAddress());
        mDepartmentTv.setText(data.getDepartmentName());
        mPoliticalStatusTv.setText(data.getPoliticalStatus());
        mEnrollmentDateTv.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(data.getEnrollmentDate())));
        String state = data.getState() == 1 ? "正常" : data.getState() == 0 ? "辞退" : "离职";
        mStateTv.setText(state);

        // 监听是否有更改
        watchTextChange();

        loadingDialog.dismiss();
    }

    private void checkTextChange() {
        if (isChanged != 0) {
            mConfirmUpdateBt.switchNormalStatus();
        } else {
            mConfirmUpdateBt.switchDisableStatus();
        }
    }

    @OnClick(R.id.bt_user_update)
    protected void onConfirmUpdateClick(View view) {
        String phone = mPhoneEt.getText().toString().trim();
        if (!PatternUtils.isPhone(phone)) {
            showToastShort(ResourcesUtils.getString(R.string.phone_pattern_error));
            return;
        }

        String address = mAddressEt.getText().toString().trim();

        ApiService apiService = RetrofitClient.client().create(ApiService.class);

        Call<CommonResult> call;
        if (LoginInfoUtils.isStudent(UserInfoActivity.this)) {
            call = apiService.updateStudentInfo(
                    userAccount, phone, address);
        } else {
            call = apiService.updateTeacherInfo(
                    userAccount, phone, address);
        }

        call.enqueue(new BaseCallback<CommonResult>() {
            @Override
            public void onSuccess(CommonResult result) {
                showToastShort(result.getMsg());
                finish();
            }
        });
    }

    private void showImage(String imageUrl) {
        ImageLoaderManager.getInstance().showImage(mImageIv, imageUrl,
                new LoaderOptions.Builder().errorDrawableId(R.drawable.icon_load_failure).build());
    }

}
