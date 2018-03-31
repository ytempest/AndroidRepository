package com.ytempest.daydayantis.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.activity.UserLoginActivity;
import com.ytempest.daydayantis.data.UserDataResult;
import com.ytempest.daydayantis.utils.UserLoginUtils;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

/**
 * @author ytempest
 *         Description：
 */
public class PersonalFragment extends BaseFragment {

    private static String TAG = "PersonalFragment";

    @ViewById(R.id.ll_personal_root)
    private LinearLayout mRootView;

    @ViewById(R.id.sl_root_view)
    private ScrollView mScrollView;
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

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_personal;
    }

    @Override
    protected void initView() {
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(mContext, mRootView)
                        .hideLeftIcon()
                        .setTitle(getResources().getString(R.string.app_name))
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .build();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();

        // 检查用户的登录状态
        changeUserLoginLayout();

    }

    @OnClick(R.id.tv_login_register)
    private void onLoginRegisterClick(View view) {
        startActivity(UserLoginActivity.class);
    }

    /**
     * 根据用户的登录状态设置相应的布局
     */
    private void changeUserLoginLayout() {
        if (!UserLoginUtils.isUserLogin(mContext)) {
            // 如果用户没有登录，就更改布局为默认布局
            switchDefaultLayout();
        } else {
            // 如果用户已经登录就切换到用户布局
            switchUserLayout();
        }

    }

    /**
     * 退出登录的按钮
     */
    @OnClick(R.id.tv_exit_login)
    private void onExitLoginClick(View view) {
        // 设置用户登录状态为已退出
        boolean isLogin = UserLoginUtils.isUserLogin(mContext);
        if (!isLogin) {
            return;
        }
        UserLoginUtils.saveUserLoginStatus(mContext, false);
        onResume();
    }


    /**
     * 切换到默认的未登录的用户布局
     */
    private void switchDefaultLayout() {
        mDefaultHead.setVisibility(View.VISIBLE);
        mLoginHead.setVisibility(View.GONE);
        // 退出登录后把页面滑动到顶部
        mScrollView.setScrollY(0);
    }

    /**
     * 切换到登录后的用户布局
     */
    private void switchUserLayout() {
        mDefaultHead.setVisibility(View.GONE);
        mLoginHead.setVisibility(View.VISIBLE);
        // 获取用户信息
        String userInfoString = UserLoginUtils.getUserInfo(mContext);
        // 如果存储的用户信息为空则return
        if (TextUtils.isEmpty(userInfoString)) {
            return;
        }
        UserDataResult.DataBean userData = new Gson().fromJson(userInfoString, UserDataResult.DataBean.class);
        ImageLoaderManager.getInstance().showImage(mIvUserHead,
                userData.getMember_info().getMember_avatar(), null);
        mTvUserName.setText(userData.getMember_info().getMember_name());
        mTvUserRegion.setText(userData.getMember_info().getMember_location_text());
    }

}

