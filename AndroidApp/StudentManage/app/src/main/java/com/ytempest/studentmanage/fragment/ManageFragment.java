package com.ytempest.studentmanage.fragment;

import android.view.View;
import android.widget.LinearLayout;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.activity.ManageClassActivity;
import com.ytempest.studentmanage.activity.ManageDepartmentActivity;
import com.ytempest.studentmanage.activity.ManageMajorActivity;
import com.ytempest.studentmanage.activity.ManageStudentActivity;
import com.ytempest.studentmanage.activity.ManageTeacherActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author ytempest
 *         Description：
 */
public class ManageFragment extends BaseFragment {
    private static final String TAG = "ManageFragment";

    @BindView(R.id.root_view)
    protected LinearLayout mRootView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_manage;
    }

    @Override
    protected void initView() {
        new DefaultNavigationBar.Builder(getActivity(), mRootView)
                .setTitle("管理中心")
                .hideLeftIcon()
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }

    @Override
    protected void initData() {

    }


    @OnClick(R.id.bt_manage_student)
    protected void onManageStudentClick(View view) {
        startActivity(ManageStudentActivity.class);
    }

    @OnClick(R.id.bt_manage_teacher)
    protected void onManageTeacherClick(View view) {
        startActivity(ManageTeacherActivity.class);
    }


    @OnClick(R.id.bt_manage_department)
    protected void onManageDepartmentClick(View view) {
        startActivity(ManageDepartmentActivity.class);
    }

    @OnClick(R.id.bt_manage_major)
    protected void onManageMajorClick(View view) {
        startActivity(ManageMajorActivity.class);
    }

    @OnClick(R.id.bt_manage_class)
    protected void onManageClassClick(View view) {
        startActivity(ManageClassActivity.class);
    }

}
