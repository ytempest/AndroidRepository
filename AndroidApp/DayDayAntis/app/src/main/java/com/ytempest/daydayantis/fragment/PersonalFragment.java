package com.ytempest.daydayantis.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.activity.UserLoginActivity;
import com.ytempest.daydayantis.activity.mode.UserDataResult;
import com.ytempest.daydayantis.utils.UserLoginUtils;

/**
 * @author ytempest
 *         Description：
 */
public class PersonalFragment extends BaseFragment {

    private static String TAG = "PersonalFragment";

    @ViewById(R.id.sl_root_view)
    private ScrollView mScrollView;
    @ViewById(R.id.tv_login_register)
    private TextView mTvLoginRegister;
    @ViewById(R.id.lo_default_head)
    private View mDefaultHead;
    @ViewById(R.id.lo_login_head)
    private View mLoginHead;
    @ViewById(R.id.iv_login_user_head)
    private ImageView mIvUserHead;
    @ViewById(R.id.tv_login_user_name)
    private TextView mTvUserName;
    @ViewById(R.id.tv_login_user_region)
    private TextView mTvUserRegion;
    @ViewById(R.id.tv_exit_login)
    private TextView mTvExitLogin;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_personal;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();

        // 检查用户的登录状态
        changeUserLoginStatus();

    }


    @OnClick(R.id.tv_login_register)
    private void onLoginRegisterClick(View view) {
        startActivity(UserLoginActivity.class);
    }

    /**
     * 退出登录的按钮
     */
    @OnClick(R.id.tv_exit_login)
    private void onExitLoginClick(View view) {
        exitUserLogin();
    }

    private void changeUserLoginStatus() {
        // 如果还没有登录
        if (!UserLoginUtils.isUserLogin(mContext)) {
            exitUserLogin();
        } else {
            signInUserLogin();
        }

    }

    private void signInUserLogin() {
        mDefaultHead.setVisibility(View.GONE);
        mLoginHead.setVisibility(View.VISIBLE);
        // 获取用户信息
        String userInfoString = UserLoginUtils.saveUserInfo(mContext);
        // 如果存储的用户信息为空则return
        if (TextUtils.isEmpty(userInfoString)) {
            return;
        }
        UserDataResult.DataBean userData = new Gson().fromJson(userInfoString, UserDataResult.DataBean.class);
        Glide.with(mContext).load(userData.getMember_info().getMember_avatar()).into(mIvUserHead);
        mTvUserName.setText(userData.getMember_info().getMember_name());
        mTvUserRegion.setText(userData.getMember_info().getMember_location_text());
    }

    private void exitUserLogin() {
        mDefaultHead.setVisibility(View.VISIBLE);
        mLoginHead.setVisibility(View.GONE);
        // 设置用户登录状态为已退出
        UserLoginUtils.saveUserLoginStatus(mContext, false);
        // 退出登录后把页面滑动到顶部
        mScrollView.setScrollY(0);
    }

}

