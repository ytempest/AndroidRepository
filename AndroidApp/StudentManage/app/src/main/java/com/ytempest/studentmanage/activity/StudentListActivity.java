package com.ytempest.studentmanage.activity;

import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.TextureView;
import android.view.View;

import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.baselibrary.view.recyclerview.LoadRecyclerView;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.fragment.CourseFragment;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.StudentListResult;

import butterknife.BindView;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class StudentListActivity extends BaseSkinActivity {

    public static final String STUDENT_ID = "studentId";

    @BindView(R.id.recycler_view)
    protected LoadRecyclerView mRecyclerView;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_student_list;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(StudentListActivity.this)
                .setTitle("学生列表")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }

    @Override
    protected void initView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(StudentListActivity.this));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(StudentListActivity.this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        final int childCourseId = getIntent().getIntExtra(CourseFragment.CHILD_COURSE_ID, 0);

        ApiService apiService = RetrofitClient.client().create(ApiService.class);
        Call<StudentListResult> call =
                apiService.getStudentListByChildCourseId(childCourseId);

        final LoadingDialog loadingDialog = new LoadingDialog(StudentListActivity.this);
        loadingDialog.show();
        call.enqueue(new BaseCallback<StudentListResult>() {
            @Override
            public void onSuccess(StudentListResult result) {
                mRecyclerView.setAdapter(new CommonRecyclerAdapter<StudentListResult.DataBean>(
                        StudentListActivity.this, result.getData(), R.layout.recycler_view_item_student_info) {
                    @Override
                    protected void bindViewData(CommonViewHolder holder, StudentListResult.DataBean item) {
                        final StudentListResult.DataBean.StudentBean student = item.getStudent();

                        ImageLoaderManager.getInstance().showImage(holder.getView(R.id.iv_student_image),
                                RetrofitClient.URL + student.getImage(),
                                new LoaderOptions.Builder().errorDrawableId(R.drawable.icon_load_failure).build());

                        holder.setText(R.id.tv_student_id, "学号：" + student.getStudentId());
                        holder.setText(R.id.tv_student_name, "姓名：" + student.getName());

                        holder.setOnItemClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(StudentListActivity.this, StudentScoreInfoActivity.class);
                                intent.putExtra(CourseFragment.COURSE_CREDIT,
                                        getIntent().getDoubleExtra(CourseFragment.COURSE_CREDIT, 0));
                                intent.putExtra(CourseFragment.CHILD_COURSE_ID, childCourseId);
                                intent.putExtra(STUDENT_ID, student.getStudentId());
                                startActivity(intent);
                            }
                        });
                    }
                });

                loadingDialog.dismiss();
            }
        });

    }
}
