package com.ytempest.studentmanage.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
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
import com.ytempest.studentmanage.model.MajorInfoResult;
import com.ytempest.studentmanage.model.ManageMajorListResult;
import com.ytempest.studentmanage.thread.MainThreadExecutor;
import com.ytempest.studentmanage.util.Config;

import java.util.List;

import butterknife.BindView;

/**
 * @author ytempest
 *         Description：
 */
public class ManageMajorActivity extends BaseSkinActivity {

    private static final String TAG = "ManageMajorActivity";

    private int mCurrentPageNum = 1;

    @BindView(R.id.recycler_view)
    protected LoadRecyclerView mRecyclerView;
    private ApiService mApiService;
    private List<ManageMajorListResult.DataBean> mDataList;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_manage_list;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(this)
                .setTitle("专业管理中心")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .setRightIcon(R.drawable.icon_add)
                .setRightIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(InsertMajorActivity.class);
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

                mApiService.getMajorList(1, Config.PAGE_SIZE)
                        .enqueue(new BaseCallback<ManageMajorListResult>() {
                            @Override
                            public void onSuccess(ManageMajorListResult result) {
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

        mApiService.getMajorList(mCurrentPageNum++, Config.PAGE_SIZE)
                .enqueue(new BaseCallback<ManageMajorListResult>() {
                    @Override
                    public void onSuccess(ManageMajorListResult result) {
                        LoadingDialog loadingDialog = new LoadingDialog(ManageMajorActivity.this);
                        loadingDialog.show();
                        setAdapterForRecyclerView(result.getData());
                        loadingDialog.dismiss();
                    }
                });

    }

    private void setAdapterForRecyclerView(List<ManageMajorListResult.DataBean> list) {
        mDataList = list;

        CommonRecyclerAdapter<ManageMajorListResult.DataBean> adapter = new CommonRecyclerAdapter<ManageMajorListResult.DataBean>(
                ManageMajorActivity.this, mDataList, R.layout.recycler_view_item_show_list_info) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageMajorListResult.DataBean item) {
                loadDataToLayout(holder, item);
            }
        };

        mRecyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        // 只有专业数量大于等于才添加上拉刷新View
        if (list.size() >= Config.PAGE_SIZE) {
            mRecyclerView.addLoadViewCreator(new DefaultLoadViewCreator());
            mRecyclerView.setOnLoadMoreListener(new LoadRecyclerView.OnLoadMoreListener() {
                @Override
                public void onLoad() {
                    mApiService.getMajorList(mCurrentPageNum++, Config.PAGE_SIZE)
                            .enqueue(new BaseCallback<ManageMajorListResult>() {
                                @Override
                                public void onSuccess(final ManageMajorListResult result) {
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

    private void loadDataToLayout(CommonViewHolder holder, final ManageMajorListResult.DataBean item) {
        holder.setText(R.id.tv_id, "专业编号：" + item.getMajorId());
        holder.setText(R.id.tv_user_name, "专业名：" + item.getName());

        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoadingDialog loadingDialog = new LoadingDialog(ManageMajorActivity.this);
                loadingDialog.show();

                mApiService.getMajorInfoByMajorId(item.getMajorId())
                        .enqueue(new BaseCallback<MajorInfoResult>() {
                            @Override
                            public void onSuccess(MajorInfoResult result) {
                                MajorInfoResult.DataBean data = result.getData();
                                loadingDialog.dismiss();
                                final AlertDialog dialog = getAlertDialog();
                                final int majorId = data.getMajorId();
                                final int departmentId = data.getDepartmentId();
                                dialog.setText(R.id.et_major_id, String.valueOf(majorId));
                                dialog.setText(R.id.et_major_name, data.getName());
                                dialog.setText(R.id.et_major_introduce, data.getIntroductions());

                                dialog.setText(R.id.et_department_id, String.valueOf(departmentId));
                                dialog.setText(R.id.et_department_name, data.getDepartmentName());

                                dialog.setOnClickListener(R.id.bt_confirm, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String majorName = ((EditText) dialog.getView(R.id.et_major_name)).getText().toString();
                                        String majorIntroduction = ((EditText) dialog.getView(R.id.et_major_introduce)).getText().toString();

                                        mApiService.updateMajor(
                                                majorId, departmentId, majorName, majorIntroduction)
                                                .enqueue(new BaseCallback<CommonResult>() {
                                                    @Override
                                                    public void onSuccess(CommonResult result) {
                                                        if (result.getCode() == 0) {
                                                            showToastShort("修改成功");
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
        return new AlertDialog.Builder(ManageMajorActivity.this)
                .setContentView(R.layout.dialog_update_major)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }
}
