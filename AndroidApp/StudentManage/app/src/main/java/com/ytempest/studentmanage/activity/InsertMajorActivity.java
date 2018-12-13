package com.ytempest.studentmanage.activity;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
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

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author ytempest
 *         Description：
 */
public class InsertMajorActivity extends BaseSkinActivity {

    private static final String TAG = "InsertMajorActivity";

    @BindView(R.id.tv_department_name)
    protected TextView mDepartmentTv;

    @BindView(R.id.et_major_id)
    protected EditText mMajorIdEt;

    @BindView(R.id.et_major_name)
    protected EditText mMajorNameEt;

    @BindView(R.id.et_major_introduce)
    protected EditText mMajorIntroEt;

    @BindView(R.id.bt_add)
    protected Button mAddBt;


    private int mDepartmentId = -1;
    private ApiService mApiService;
    private List<ManageDepartmentListResult.DataBean> mDepartmentList;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_insert_major;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(InsertMajorActivity.this)
                .setTitle("添加专业")
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
        mApiService = RetrofitClient.client().create(ApiService.class);
    }

    @OnClick(R.id.tv_department_name)
    protected void onDepartmentClick(View view) {
        mApiService.getDepartmentList(1, 10000)
                .enqueue(new BaseCallback<ManageDepartmentListResult>() {
                    @Override
                    public void onSuccess(ManageDepartmentListResult result) {
                        mDepartmentList = result.getData();
                        initDepartmentDialog();
                    }
                });

    }

    private void initDepartmentDialog() {
        final AlertDialog dialog = createDialog();

        RecyclerView recyclerView = getRecyclerView(dialog);
        recyclerView.setAdapter(new CommonRecyclerAdapter<ManageDepartmentListResult.DataBean>(
                InsertMajorActivity.this, mDepartmentList, R.layout.recycler_view_show_list_item) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageDepartmentListResult.DataBean item) {
                holder.setText(R.id.tv_item, item.getName());

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final LoadingDialog loadingDialog = new LoadingDialog(InsertMajorActivity.this);
                        loadingDialog.show();

                        mApiService.getNewMajorId(item.getDepartmentId())
                                .enqueue(new BaseCallback<ManageIdResult>() {
                                    @Override
                                    public void onSuccess(ManageIdResult result) {
                                        mMajorIdEt.setText(String.valueOf(result.getData()));
                                        mDepartmentTv.setText(item.getName());
                                        mDepartmentId = item.getDepartmentId();
                                        loadingDialog.dismiss();
                                        dialog.dismiss();
                                    }
                                });


                    }
                });
            }
        });
        dialog.show();
    }

    private RecyclerView getRecyclerView(AlertDialog dialog) {
        RecyclerView recyclerView = dialog.getView(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(InsertMajorActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(InsertMajorActivity.this, DividerItemDecoration.VERTICAL));
        return recyclerView;
    }

    private AlertDialog createDialog() {
        return new AlertDialog.Builder(InsertMajorActivity.this)
                .setContentView(R.layout.dialog_show_list)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }

    @OnClick(R.id.bt_add)
    protected void onAddMajorClick(View view) {
        String majorName = mMajorNameEt.getText().toString();
        String majorIntro = mMajorIntroEt.getText().toString();
        if (mDepartmentId == -1 || TextUtils.isEmpty(majorName) || TextUtils.isEmpty(majorIntro)) {
            showToastShort("请填写完所有信息");

        } else {
            final LoadingDialog loadingDialog = new LoadingDialog(InsertMajorActivity.this);
            loadingDialog.show();
            String majorId = mMajorIdEt.getText().toString();
            mApiService.insertMajor(majorId, mDepartmentId, majorName, majorIntro)
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
    }
}

