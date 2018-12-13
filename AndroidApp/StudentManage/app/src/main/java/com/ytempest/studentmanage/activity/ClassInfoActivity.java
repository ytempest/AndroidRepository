package com.ytempest.studentmanage.activity;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.ClassInfoResult;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.ManageDepartmentListResult;
import com.ytempest.studentmanage.model.ManageMajorListResult;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author ytempest
 *         Description：
 */
public class ClassInfoActivity extends BaseSkinActivity {
    private static final String TAG = "ClassInfoActivity";

    @BindView(R.id.et_class_id)
    protected EditText mClassIdEt;

    @BindView(R.id.tv_class_department)
    protected TextView mClassDepartmentTv;

    @BindView(R.id.tv_class_major)
    protected TextView mClassMajorTv;

    @BindView(R.id.et_class_grade)
    protected EditText mClassGradeEt;

    @BindView(R.id.et_class_name)
    protected EditText mClassNameEt;

    private ApiService mApiService;

    private List<ManageDepartmentListResult.DataBean> mDepartmentList;
    private int mDepartmentId = -1;

    private List<ManageMajorListResult.DataBean> mMajorList;
    private int mMajorId = -1;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_class_info;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(ClassInfoActivity.this)
                .setTitle("修改班级信息")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        final LoadingDialog loadingDialog = new LoadingDialog(ClassInfoActivity.this);
        loadingDialog.show();

        int classId = getIntent().getIntExtra(ManageClassActivity.CLASS_ID, 0);
        mApiService = RetrofitClient.client().create(ApiService.class);
        mApiService.getClassInfoByClassId(classId)
                .enqueue(new BaseCallback<ClassInfoResult>() {
                    @Override
                    public void onSuccess(ClassInfoResult result) {
                        loadDataToLayout(result.getData(), loadingDialog);
                    }
                });


        mApiService.getDepartmentList(1, 1000)
                .enqueue(new BaseCallback<ManageDepartmentListResult>() {
                    @Override
                    public void onSuccess(ManageDepartmentListResult result) {
                        mDepartmentList = result.getData();
                    }
                });

        mApiService.getMajorList(1, 1000)
                .enqueue(new BaseCallback<ManageMajorListResult>() {
                    @Override
                    public void onSuccess(ManageMajorListResult result) {
                        mMajorList = result.getData();
                    }
                });
    }

    private void loadDataToLayout(ClassInfoResult.DataBean data, LoadingDialog loadingDialog) {
        mClassIdEt.setText(String.valueOf(data.getClassId()));
        mClassNameEt.setText(data.getName());
        mClassGradeEt.setText(String.valueOf(data.getGrade()));

        mClassDepartmentTv.setText(data.getDepartmentName());
        mClassDepartmentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = createDialog();
                showDepartmentDialog(dialog);

            }
        });
        mClassMajorTv.setText(data.getMajorName());
        mClassMajorTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = createDialog();
                showMajorDialog(dialog);
            }
        });

        loadingDialog.dismiss();
    }

    private void showMajorDialog(final AlertDialog dialog) {
        RecyclerView recyclerView = getRecyclerView(dialog);
        recyclerView.setAdapter(new CommonRecyclerAdapter<ManageMajorListResult.DataBean>(
                ClassInfoActivity.this, mMajorList, R.layout.recycler_view_show_list_item) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageMajorListResult.DataBean item) {
                holder.setText(R.id.tv_item, item.getName());

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClassMajorTv.setText(item.getName());
                        mMajorId = item.getMajorId();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private void showDepartmentDialog(final AlertDialog dialog) {
        RecyclerView recyclerView = getRecyclerView(dialog);
        recyclerView.setAdapter(new CommonRecyclerAdapter<ManageDepartmentListResult.DataBean>(
                ClassInfoActivity.this, mDepartmentList, R.layout.recycler_view_show_list_item) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageDepartmentListResult.DataBean item) {
                holder.setText(R.id.tv_item, item.getName());

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClassDepartmentTv.setText(item.getName());
                        mDepartmentId = item.getDepartmentId();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private RecyclerView getRecyclerView(AlertDialog dialog) {
        RecyclerView recyclerView = dialog.getView(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ClassInfoActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ClassInfoActivity.this, DividerItemDecoration.VERTICAL));
        return recyclerView;
    }

    private AlertDialog createDialog() {
        return new AlertDialog.Builder(ClassInfoActivity.this)
                .setContentView(R.layout.dialog_show_list)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }

    @OnClick(R.id.bt_class_info)
    protected void onConfirmUpdateClick(View view) {
        String classId = mClassIdEt.getText().toString();
        String name = mClassNameEt.getText().toString();
        String grade = mClassGradeEt.getText().toString();
        int departmentId = getDepartmentId();
        int majorId = getMajorId();

        final LoadingDialog loadingDialog = new LoadingDialog(ClassInfoActivity.this);
        loadingDialog.show();

        mApiService.updateClass(classId, name, grade, departmentId, majorId)
                .enqueue(new BaseCallback<CommonResult>() {
                    @Override
                    public void onSuccess(CommonResult result) {
                        if (result.getCode() == 0) {
                            showToastShort("修改成功");
                        } else {
                            showToastShort(result.getMsg());
                        }
                        loadingDialog.dismiss();
                        finish();
                    }
                });
    }

    private int getDepartmentId() {
        if (mDepartmentId == -1) {
            String departmentName = mClassDepartmentTv.getText().toString().trim();
            for (ManageDepartmentListResult.DataBean dataBean : mDepartmentList) {
                if (dataBean.getName().equals(departmentName)) {
                    return dataBean.getDepartmentId();
                }
            }
        }
        return -1;
    }

    private int getMajorId() {
        if (mMajorId == -1) {
            String majorName = mClassMajorTv.getText().toString().trim();
            for (ManageMajorListResult.DataBean dataBean : mMajorList) {
                if (dataBean.getName().equals(majorName)) {
                    return dataBean.getMajorId();
                }
            }
        }
        return -1;
    }
}
