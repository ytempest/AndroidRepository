package com.ytempest.studentmanage.fragment;

import android.view.View;
import android.widget.TextView;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
import com.ytempest.framelibrary.view.CircleImageView;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.activity.UpdatePasswordActivity;
import com.ytempest.studentmanage.activity.UserInfoActivity;
import com.ytempest.studentmanage.activity.UserLoginActivity;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.StudentInfoResult;
import com.ytempest.studentmanage.model.TeacherInfoResult;
import com.ytempest.studentmanage.util.LoginInfoUtils;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;


/**
 * @author ytempest
 *         Description：
 */
public class PersonalFragment extends BaseFragment {
    private static final String TAG = "PersonalFragment";

    @BindView(R.id.iv_head)
    protected CircleImageView mHeadIv;

    @BindView(R.id.tv_user_account)
    protected TextView mAccountTv;

    @BindView(R.id.tv_user)
    protected TextView mUserTv;

    @BindView(R.id.tv_update_password)
    protected TextView mUpdatePasswordTv;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_personal;
    }

    @Override
    protected void initView() {
        initViewAccordingUserType();
    }

    private void initViewAccordingUserType() {
        // 设置用户账号和用户类型
        String account = LoginInfoUtils.getUserAccount(getContext());
        mAccountTv.setText("账号：" + account);
        String userType = LoginInfoUtils.getUserType(getContext());
        String user = "学生";
        if (LoginInfoUtils.USER_TYPE_TEACHER.equals(userType)) {
            user = "教师";
        } else if (LoginInfoUtils.USER_TYPE_MANAGER.equals(userType)) {
            user = "管理员";
        }
        mUserTv.setText("身份：" + user);

        // 如果不是管理员则显示更改密码的View
        mUpdatePasswordTv.setVisibility(!LoginInfoUtils.isManager(getContext()) ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initData() {

        ApiService apiService = RetrofitClient.client().create(ApiService.class);
        // 根据用户的类型做不同的处理
        if (LoginInfoUtils.isStudent(getContext())) {
            Call<StudentInfoResult> call = apiService.getStudentInfoByStudentId(LoginInfoUtils.getUserAccount(getContext()));
            call.enqueue(new BaseCallback<StudentInfoResult>() {
                @Override
                public void onSuccess(StudentInfoResult result) {
                    loadHeadImage(RetrofitClient.URL + result.getData().getImage());
                }
            });

        } else if (LoginInfoUtils.isTeacher(getContext())) {

            Call<TeacherInfoResult> call = apiService.getTeacherInfoByTeacherId(LoginInfoUtils.getUserAccount(getContext()));
            call.enqueue(new BaseCallback<TeacherInfoResult>() {
                @Override
                public void onSuccess(TeacherInfoResult result) {
                    loadHeadImage(RetrofitClient.URL + result.getData().getImage());
                }
            });
        }

    }

    private void loadHeadImage(String imageUrl) {
        ImageLoaderManager.getInstance().showImage(mHeadIv, imageUrl,
                new LoaderOptions.Builder().errorDrawableId(R.drawable.personal_fragment_default_head).build());

    }

    /**
     * 头像的点击事件处理
     */
    @OnClick(R.id.iv_head)
    protected void onHeadClick(View view) {
        // 如果不是管理员则打开个人信息页面
        if (!LoginInfoUtils.isManager(getContext())) {
            startActivity(UserInfoActivity.class);
        }
    }


    /**
     * 更改密码按钮的点击事件处理
     */
    @OnClick(R.id.tv_update_password)
    protected void onUpdatePasswordClick(View view) {
        startActivity(UpdatePasswordActivity.class);
    }


    /**
     * 退出登录按钮的点击事件处理
     */
    @OnClick(R.id.tv_sign_up)
    protected void onSignUpClick(View view) {
        LoginInfoUtils.clearUserLoginData(getContext());
        startActivity(UserLoginActivity.class);
        getActivity().finish();
    }

    /**
     * 夜间模式按钮的点击事件处理
     */
    @OnClick(R.id.tv_night_mode)
    protected void onNightModeClick(View view) {

    }


}
