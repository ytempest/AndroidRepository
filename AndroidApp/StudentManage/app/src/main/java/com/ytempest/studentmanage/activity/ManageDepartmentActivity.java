package com.ytempest.studentmanage.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.baselibrary.view.recyclerview.LoadRecyclerView;
import com.ytempest.baselibrary.view.recyclerview.RefreshRecyclerView;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.baselibrary.view.recyclerview.division.DividerItemDecoration;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.adapter.DefaultLoadViewCreator;
import com.ytempest.studentmanage.adapter.DefaultRefreshViewCreator;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.DepartmentInfoResult;
import com.ytempest.studentmanage.model.ManageDepartmentListResult;
import com.ytempest.studentmanage.model.ManageIdResult;
import com.ytempest.studentmanage.thread.MainThreadExecutor;
import com.ytempest.studentmanage.util.Config;
import com.ytempest.studentmanage.util.ResourcesUtils;

import java.util.List;

import butterknife.BindView;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class ManageDepartmentActivity extends BaseSkinActivity {

    private static final String TAG = "DepartmentManage";

    private int mCurrentPageNum = 1;

    @BindView(R.id.recycler_view)
    protected LoadRecyclerView mRecyclerView;
    private ApiService mApiService;
    private List<ManageDepartmentListResult.DataBean> mDataList;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_manage_list;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(this)
                .setTitle("院系管理中心")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .setRightIcon(R.drawable.icon_add)
                .setRightIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final LoadingDialog loadingDialog = new LoadingDialog(ManageDepartmentActivity.this);
                        loadingDialog.show();

                        mApiService.getNewDepartmentId().enqueue(new BaseCallback<ManageIdResult>() {
                            @Override
                            public void onSuccess(ManageIdResult result) {

                                loadingDialog.dismiss();

                                final AlertDialog dialog = getAlertDialog();

                                final String departmentId = String.valueOf(result.getData());
                                dialog.setText(R.id.bt_confirm, "确定添加");
                                dialog.setText(R.id.tv_id, departmentId);

                                dialog.setOnClickListener(R.id.bt_confirm, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        String name = ((EditText) dialog.getView(R.id.et_department_name)).getText().toString();
                                        String introductions = ((EditText) dialog.getView(R.id.et_department_introduce)).getText().toString();

                                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(introductions)) {
                                            showToastShort(ResourcesUtils.getString(R.string.please_input_all_info));
                                        } else {

                                            mApiService.insertDepartment(departmentId, name, introductions)
                                                    .enqueue(new BaseCallback<CommonResult>() {
                                                        @Override
                                                        public void onSuccess(CommonResult result) {
                                                            if (result.getCode() == 0) {
                                                                showToastShort(ResourcesUtils.getString(R.string.insert_success));
                                                            } else {
                                                                showToastShort(result.getMsg());
                                                            }
                                                            dialog.dismiss();
                                                        }
                                                    });

                                        }
                                    }
                                });
                                dialog.show();
                            }
                        });

                    }
                })
                .build();

    }

    @Override
    protected void initView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyeler_view_division_line));
        mRecyclerView.addItemDecoration(itemDecoration);

        mRecyclerView.addRefreshViewCreator(new DefaultRefreshViewCreator());
        mRecyclerView.setOnRefreshMoreListener(new RefreshRecyclerView.OnRefreshMoreListener() {
            @Override
            public void onRefresh() {
                // 记录请求开始时间
                final long startTime = System.currentTimeMillis();

                mApiService.getDepartmentList(1, Config.PAGE_SIZE)
                        .enqueue(new BaseCallback<ManageDepartmentListResult>() {
                            @Override
                            public void onSuccess(ManageDepartmentListResult result) {
                                if (mDataList != null) {
                                    // 重置页码数
                                    mCurrentPageNum = 1;

                                    // 判断当前是否已经加载了两页或以上的数据，如果是则将第一页之后
                                    // 的数据全部删除
                                    int itemCount = mDataList.size();
                                    if (itemCount > Config.PAGE_SIZE) {
                                        for (int i = mDataList.size() - 1; i >= Config.PAGE_SIZE; i--) {
                                            mDataList.remove(i);
                                        }
                                        mRecyclerView.getAdapter().notifyItemRangeRemoved(
                                                Config.PAGE_SIZE + 1, itemCount);
                                    }

                                    // 更新第一页的数据
                                    mDataList.clear();
                                    mDataList.addAll(result.getData());
                                    mRecyclerView.getAdapter().notifyItemRangeChanged(1, mDataList.size());

                                    // 加载的时间低于阀值，则将加载的时间延长，以提高用户体验
                                    long duration = System.currentTimeMillis() - startTime;
                                    if (duration < Config.MIN_LOAD_TIME) {
                                        MainThreadExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                mRecyclerView.stopRefresh();
                                            }
                                        }, Config.MIN_LOAD_TIME - duration);
                                    } else {
                                        mRecyclerView.stopRefresh();
                                    }

                                }
                            }
                        });

            }
        });


    }

    @Override
    protected void initData() {
        mApiService = RetrofitClient.client().create(ApiService.class);

        Call<ManageDepartmentListResult> call = mApiService.getDepartmentList(mCurrentPageNum++, Config.PAGE_SIZE);
        call.enqueue(new BaseCallback<ManageDepartmentListResult>() {
            @Override
            public void onSuccess(ManageDepartmentListResult result) {
                LoadingDialog loadingDialog = new LoadingDialog(ManageDepartmentActivity.this);
                loadingDialog.show();
                setAdapterForRecyclerView(result.getData());
                loadingDialog.dismiss();
            }
        });

    }

    private void setAdapterForRecyclerView(List<ManageDepartmentListResult.DataBean> list) {
        mDataList = list;

        mRecyclerView.setAdapter(new CommonRecyclerAdapter<ManageDepartmentListResult.DataBean>(
                ManageDepartmentActivity.this, mDataList, R.layout.recycler_view_item_show_list_info) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageDepartmentListResult.DataBean item) {
                loadDataToLayout(holder, item);
            }
        });
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        if (list.size() >= Config.PAGE_SIZE) {
            mRecyclerView.addLoadViewCreator(new DefaultLoadViewCreator());
            mRecyclerView.setOnLoadMoreListener(new LoadRecyclerView.OnLoadMoreListener() {
                @Override
                public void onLoad() {
                    mApiService.getDepartmentList(mCurrentPageNum++, Config.PAGE_SIZE)
                            .enqueue(new BaseCallback<ManageDepartmentListResult>() {
                                @Override
                                public void onSuccess(final ManageDepartmentListResult result) {
                                    mDataList.addAll(result.getData());
                                    mRecyclerView.getAdapter().notifyItemRangeChanged(
                                            mDataList.size(), result.getData().size());
                                    mRecyclerView.stopLoad();
                                }
                            });
                }
            });

        }
    }

    private void loadDataToLayout(CommonViewHolder holder, final ManageDepartmentListResult.DataBean item) {
        holder.setText(R.id.tv_id, "院系编号：" + item.getDepartmentId());
        holder.setText(R.id.tv_user_name, "院系名：" + item.getName());

        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoadingDialog loadingDialog = new LoadingDialog(ManageDepartmentActivity.this);
                loadingDialog.show();

                mApiService.getDepartmentInfoByDepartmentId(item.getDepartmentId())
                        .enqueue(new BaseCallback<DepartmentInfoResult>() {
                            @Override
                            public void onSuccess(DepartmentInfoResult result) {
                                final DepartmentInfoResult.DataBean departmentInfo = result.getData();
                                loadingDialog.dismiss();

                                final AlertDialog dialog = getAlertDialog();

                                dialog.setText(R.id.tv_id, String.valueOf(departmentInfo.getDepartmentId()));
                                dialog.setText(R.id.et_department_name, departmentInfo.getName());
                                dialog.setText(R.id.et_department_introduce, departmentInfo.getIntroductions());

                                dialog.setOnClickListener(R.id.bt_confirm, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String name = ((EditText) dialog.getView(R.id.et_department_name)).getText().toString();
                                        String introduction = ((EditText) dialog.getView(R.id.et_department_introduce)).getText().toString();

                                        mApiService.updateDepartment(departmentInfo.getDepartmentId(), name, introduction)
                                                .enqueue(new BaseCallback<CommonResult>() {
                                                    @Override
                                                    public void onSuccess(CommonResult result) {
                                                        if (result.getCode() == 0) {
                                                            showToastShort(ResourcesUtils.getString(R.string.update_success));
                                                        } else {
                                                            showToastShort(result.getMsg());
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                });

                                    }
                                });
                                dialog.show();
                            }
                        });
            }
        });
    }

    private AlertDialog getAlertDialog() {
        return new AlertDialog.Builder(ManageDepartmentActivity.this)
                .setContentView(R.layout.dialog_update_department)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }
}
