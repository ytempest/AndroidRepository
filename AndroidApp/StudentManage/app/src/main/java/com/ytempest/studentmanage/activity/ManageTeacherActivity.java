package com.ytempest.studentmanage.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
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
import com.ytempest.studentmanage.model.ManageTeacherListResult;
import com.ytempest.studentmanage.thread.MainThreadExecutor;
import com.ytempest.studentmanage.util.Config;

import java.util.List;

import butterknife.BindView;

/**
 * @author ytempest
 *         Description：
 */
public class ManageTeacherActivity extends BaseSkinActivity {

    private static final String TAG = "ManageTeacherActivity";

    public static final String TEACHER_ID = "teacherId";

    private int mCurrentPageNum = 1;

    @BindView(R.id.recycler_view)
    protected LoadRecyclerView mRecyclerView;
    private ApiService mApiService;
    private List<ManageTeacherListResult.DataBean> mDataList;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_manage_list;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(this)
                .setTitle("教师管理中心")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .setRightIcon(R.drawable.icon_add)
                .setRightIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(InsertTeacherActivity.class);
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

                mApiService.getTeacherList(1, Config.PAGE_SIZE)
                        .enqueue(new BaseCallback<ManageTeacherListResult>() {
                            @Override
                            public void onSuccess(ManageTeacherListResult result) {
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

                        });
            }
        });

    }

    @Override
    protected void initData() {
        mApiService = RetrofitClient.client().create(ApiService.class);

        final LoadingDialog loadingDialog = new LoadingDialog(ManageTeacherActivity.this);
        loadingDialog.show();
        mApiService.getTeacherList(mCurrentPageNum++, Config.PAGE_SIZE)
                .enqueue(new BaseCallback<ManageTeacherListResult>() {
                    @Override
                    public void onSuccess(ManageTeacherListResult result) {
                        setAdapterForRecyclerView(result.getData());
                        loadingDialog.dismiss();
                    }
                });

    }

    private void setAdapterForRecyclerView(List<ManageTeacherListResult.DataBean> list) {
        mDataList = list;

        CommonRecyclerAdapter<ManageTeacherListResult.DataBean> adapter = new CommonRecyclerAdapter<ManageTeacherListResult.DataBean>(
                ManageTeacherActivity.this, mDataList, R.layout.recycler_view_item_student_info) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageTeacherListResult.DataBean item) {

                ImageLoaderManager.getInstance().showImage(holder.getView(R.id.iv_student_image),
                        RetrofitClient.URL + item.getImage(),
                        new LoaderOptions.Builder().errorDrawableId(R.drawable.icon_load_failure).build());

                holder.setText(R.id.tv_student_id, "工号：" + item.getTeacherId());
                holder.setText(R.id.tv_student_name, "姓名：" + item.getName());
                holder.setText(R.id.tv_student_state, item.getIdentity());

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ManageTeacherActivity.this, UpdateTeacherActivity.class);
                        intent.putExtra(ManageTeacherActivity.TEACHER_ID, String.valueOf(item.getTeacherId()));
                        startActivity(intent);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        if (list.size() >= Config.PAGE_SIZE) {
            mRecyclerView.addLoadViewCreator(new DefaultLoadViewCreator());
            mRecyclerView.setOnLoadMoreListener(new LoadRecyclerView.OnLoadMoreListener() {
                @Override
                public void onLoad() {
                    mApiService.getTeacherList(mCurrentPageNum++, Config.PAGE_SIZE)
                            .enqueue(new BaseCallback<ManageTeacherListResult>() {
                                @Override
                                public void onSuccess(final ManageTeacherListResult result) {
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
}
