package com.ytempest.studentmanage.activity;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.ManageDepartmentListResult;
import com.ytempest.studentmanage.model.ManageIdResult;
import com.ytempest.studentmanage.model.ManageMajorListResult;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author ytempest
 *         Description：
 */
public class InsertClassActivity extends BaseSkinActivity {

    private static final String TAG = "InsertClassActivity";

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

    @BindView(R.id.bt_class_info)
    protected Button mAddButton;


    private ApiService mApiService;

    private List<ManageDepartmentListResult.DataBean> mDepartmentList;
    private int mDepartmentId;

    private List<ManageMajorListResult.DataBean> mMajortList;
    private int mMajorId;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_class_info;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(InsertClassActivity.this)
                .setTitle("添加班级")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }

    @Override
    protected void initView() {
        ((ViewGroup) mClassIdEt.getParent()).setVisibility(View.GONE);
        mAddButton.setText("添加");
    }

    @Override
    protected void initData() {

        mApiService = RetrofitClient.client().create(ApiService.class);

        mClassDepartmentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = createDialog();

                mApiService.getDepartmentList(1, 1000)
                        .enqueue(new BaseCallback<ManageDepartmentListResult>() {
                            @Override
                            public void onSuccess(ManageDepartmentListResult result) {
                                mDepartmentList = result.getData();
                                showDepartmentDialog(dialog);
                            }
                        });
            }
        });

        mClassMajorTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = createDialog();

                mApiService.getMajorList(1, 1000)
                        .enqueue(new BaseCallback<ManageMajorListResult>() {
                            @Override
                            public void onSuccess(ManageMajorListResult result) {
                                mMajortList = result.getData();
                                showMajorDialog(dialog);
                            }
                        });
            }
        });
    }

    private void showMajorDialog(final AlertDialog dialog) {
        RecyclerView recyclerView = getRecyclerView(dialog);
        recyclerView.setAdapter(new CommonRecyclerAdapter<ManageMajorListResult.DataBean>(
                InsertClassActivity.this, mMajortList, R.layout.recycler_view_show_list_item) {
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
                InsertClassActivity.this, mDepartmentList, R.layout.recycler_view_show_list_item) {
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
        recyclerView.setLayoutManager(new LinearLayoutManager(InsertClassActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(InsertClassActivity.this, DividerItemDecoration.VERTICAL));
        return recyclerView;
    }

    private AlertDialog createDialog() {
        return new AlertDialog.Builder(InsertClassActivity.this)
                .setContentView(R.layout.dialog_show_list)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }

    @OnClick(R.id.bt_class_info)
    protected void onConfirmUpdateClick(View view) {
        final String className = mClassNameEt.getText().toString();
        final String classGrade = mClassGradeEt.getText().toString();
        if (TextUtils.isEmpty(className) && TextUtils.isEmpty(classGrade)) {
            showToastShort("请填写完所有信息");

        } else {
            final int majorId = mMajorId;

            final LoadingDialog loadingDialog = new LoadingDialog(InsertClassActivity.this);
            loadingDialog.show();

            mApiService.getNewClassId(classGrade, majorId)
                    .enqueue(new BaseCallback<ManageIdResult>() {
                        @Override
                        public void onSuccess(ManageIdResult result) {
                            int classId = result.getData();
                            int departmentId = mDepartmentId;
                            int majorId = mMajorId;
                            mApiService.insertClass(classId, className, classGrade, departmentId, majorId)
                                    .enqueue(new BaseCallback<CommonResult>() {
                                        @Override
                                        public void onSuccess(CommonResult result) {
                                            if (result.getCode() == 0) {
                                                showToastShort("添加成功");
                                            } else {
                                                showToastShort(result.getMsg());
                                            }
                                            loadingDialog.dismiss();
                                            finish();
                                        }
                                    });
                        }
                    });
        }
    }
}
