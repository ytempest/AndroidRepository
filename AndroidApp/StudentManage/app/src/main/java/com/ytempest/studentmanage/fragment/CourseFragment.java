package com.ytempest.studentmanage.fragment;

import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.LinearLayout;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.baselibrary.view.recyclerview.LoadRecyclerView;
import com.ytempest.baselibrary.view.recyclerview.RefreshRecyclerView;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.activity.CourseInfoActivity;
import com.ytempest.studentmanage.activity.StudentListActivity;
import com.ytempest.studentmanage.adapter.DefaultLoadViewCreator;
import com.ytempest.studentmanage.adapter.DefaultRefreshViewCreator;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.CourseListResult;
import com.ytempest.studentmanage.thread.MainThreadExecutor;
import com.ytempest.studentmanage.util.Config;
import com.ytempest.studentmanage.util.LoginInfoUtils;

import java.util.List;

import butterknife.BindView;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class CourseFragment extends BaseFragment {

    private static final String TAG = "CourseFragment";

    public static final String CHILD_COURSE_ID = "childCourseId";
    public static final String COURSE_CREDIT = "child_credit";

    @BindView(R.id.course_fragment_root_view)
    protected LinearLayout mRootView;

    @BindView(R.id.recycler_view)
    protected LoadRecyclerView mRecyclerView;
    private LoadingDialog mDialog;

    private List<CourseListResult.DataBean> mCourseList;

    private int mCurrentPageNum = 1;

    private ApiService mApiService;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_course;
    }

    @Override
    protected void initView() {
        // 初始化标题栏
        new DefaultNavigationBar.Builder(getActivity(), mRootView)
                .setTitle("课程列表")
                .hideLeftIcon()
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyeler_view_division_line));
        mRecyclerView.addItemDecoration(itemDecoration);

        mRecyclerView.addRefreshViewCreator(new DefaultRefreshViewCreator());
        mRecyclerView.setOnRefreshMoreListener(new RefreshRecyclerView.OnRefreshMoreListener() {
            @Override
            public void onRefresh() {
                // 记录请求开始时间
                final long startTime = System.currentTimeMillis();

                String id = LoginInfoUtils.getUserAccount(getContext());
                Call<CourseListResult> call;
                if (LoginInfoUtils.isStudent(getContext())) {
                    call = mApiService.getChildCourseByStudentId(id, 1, Config.PAGE_SIZE);
                } else {
                    call = mApiService.getChildCourseByTeacherId(id, 1, Config.PAGE_SIZE);
                }

                call.enqueue(new BaseCallback<CourseListResult>() {
                    @Override
                    public void onSuccess(CourseListResult result) {
                        mCurrentPageNum++;

                        // 判断当前是否已经加载了两页或以上的数据，如果是则将第一页之后
                        // 的数据全部删除
                        int itemCount = mCourseList.size();
                        if (itemCount > Config.PAGE_SIZE) {
                            for (int i = mCourseList.size() - 1; i >= Config.PAGE_SIZE; i--) {
                                mCourseList.remove(i);
                            }
                            mRecyclerView.getAdapter().notifyItemRangeRemoved(
                                    Config.PAGE_SIZE + 1, itemCount);
                        }

                        // 更新第一页的数据
                        mCourseList.clear();
                        mCourseList.addAll(result.getData());

                        mRecyclerView.getAdapter().notifyItemRangeChanged(1, mCourseList.size());

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

        String id = LoginInfoUtils.getUserAccount(getContext());
        Call<CourseListResult> call;
        if (LoginInfoUtils.isStudent(getContext())) {
            call = mApiService.getChildCourseByStudentId(id, mCurrentPageNum++, Config.PAGE_SIZE);
        } else {
            call = mApiService.getChildCourseByTeacherId(id, mCurrentPageNum++, Config.PAGE_SIZE);
        }

        // 初始化加载动画
        mDialog = new LoadingDialog(getContext());
        mDialog.show();
        call.enqueue(new BaseCallback<CourseListResult>() {
            @Override
            public void onSuccess(final CourseListResult result) {
                mCourseList = result.getData();

                mRecyclerView.setAdapter(new CommonRecyclerAdapter<CourseListResult.DataBean>(
                        getContext(), mCourseList, R.layout.recycler_view_item_course_info) {
                    @Override
                    protected void bindViewData(CommonViewHolder holder, final CourseListResult.DataBean item) {
                        mCourseList = result.getData();
                        holder.setText(R.id.tv_class_name, "课程名：" + item.getChildCourse().getName());
                        holder.setText(R.id.tv_class_time, "上课时间：" + item.getChildCourse().getClassTime());
                        String status = item.getChildCourse().getState() == 1 ? "正常上课" : "已结课";
                        holder.setText(R.id.tv_class_status, status);

                        holder.setOnItemClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Class targetActivity = LoginInfoUtils.isStudent(getContext()) ?
                                        CourseInfoActivity.class : StudentListActivity.class;
                                Intent intent = new Intent(getActivity(), targetActivity);

                                if (LoginInfoUtils.isTeacher(getContext())) {
                                    intent.putExtra(COURSE_CREDIT, item.getChildCourse().getCredit());
                                }
                                intent.putExtra(CHILD_COURSE_ID, item.getChildCourse().getChildCourseId());
                                startActivity(intent);
                            }
                        });
                    }
                });
                ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

                if (mCourseList.size() >= Config.PAGE_SIZE) {
                    mRecyclerView.addLoadViewCreator(new DefaultLoadViewCreator());
                    mRecyclerView.setOnLoadMoreListener(new LoadRecyclerView.OnLoadMoreListener() {
                        @Override
                        public void onLoad() {
                            loadCourseListByPageNum(mCurrentPageNum++);
                        }
                    });
                }

                mDialog.dismiss();
            }
        });

    }


    public void loadCourseListByPageNum(final int pageNum) {
        mApiService.getChildCourseByStudentId(
                LoginInfoUtils.getUserAccount(getContext()), pageNum, Config.PAGE_SIZE)
                .enqueue(new BaseCallback<CourseListResult>() {
                    @Override
                    public void onSuccess(CourseListResult result) {
                        mCourseList.addAll(result.getData());
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        mRecyclerView.stopLoad();
                        mDialog.dismiss();
                    }
                });
    }


    @Override
    public void onPause() {
        super.onPause();
        mDialog.dismiss();
    }
}

