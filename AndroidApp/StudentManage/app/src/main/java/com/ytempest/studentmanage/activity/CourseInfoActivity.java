package com.ytempest.studentmanage.activity;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.fragment.CourseFragment;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.CourseInfoResult;
import com.ytempest.studentmanage.model.TeacherInfoResult;
import com.ytempest.studentmanage.util.LoginInfoUtils;

import butterknife.BindView;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class CourseInfoActivity extends BaseSkinActivity {
    private static final String TAG = "CourseInfoActivity";

    @BindView(R.id.tv_course_name)
    protected TextView mNameTv;

    @BindView(R.id.tv_course_department)
    protected TextView mDepartmentTv;

    @BindView(R.id.tv_course_intro)
    protected TextView mIntroduceTv;

    @BindView(R.id.tv_course_time)
    protected TextView mTimeTv;

    @BindView(R.id.tv_course_state)
    protected TextView mStateTv;
    @BindView(R.id.tv_course_teacher)
    protected TextView mTeacherTv;

    @BindView(R.id.tv_course_credit)
    protected TextView mCreditTv;

    @BindView(R.id.tv_course_daily_score)
    protected TextView mDailyScoreTv;
    @BindView(R.id.tv_course_exam_score)
    protected TextView mExamScoreTv;

    @BindView(R.id.tv_course_final_score)
    protected TextView mFinalScoreTv;

    @BindView(R.id.tv_course_final_credit)
    protected TextView mFinalCreditTv;

    @BindView(R.id.tv_course_complete_state)
    protected TextView mCompleteStateTv;
    private ApiService mApiService;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_course_info;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(CourseInfoActivity.this)
                .setTitle("课程信息")
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
        int childCourseId = getIntent().getIntExtra(CourseFragment.CHILD_COURSE_ID, 0);
        String account = LoginInfoUtils.getUserAccount(this);

        mApiService = RetrofitClient.client().create(ApiService.class);
        Call<CourseInfoResult> call = mApiService.getCourseInfo(account, childCourseId);
        call.enqueue(new BaseCallback<CourseInfoResult>() {
            @Override
            public void onSuccess(CourseInfoResult result) {
                loadDataToLayout(result);
            }
        });


    }

    private void loadDataToLayout(CourseInfoResult result) {
        final CourseInfoResult.DataBean data = result.getData();
        mNameTv.setText(data.getCourseDetail().getName());
        mDepartmentTv.setText(data.getCourseDetail().getCourseDto().getDepartmentName());
        mIntroduceTv.setText(data.getCourseDetail().getCourseDto().getIntroductions());
        mTimeTv.setText(data.getCourseDetail().getClassTime());
        mStateTv.setText(data.getCourseDetail().getState() == 1 ? "正常上课" : "已结课");
        mTeacherTv.setText(Html.fromHtml(String.format("<font color=#73d6dc><u>%s</u></font>", data.getTeacherBase().getTeacherName())));
        mCreditTv.setText(String.valueOf(data.getCourseDetail().getCredit()));
        mDailyScoreTv.setText(String.valueOf(data.getDailyScore()));
        mExamScoreTv.setText(String.valueOf(data.getExamScore()));
        mFinalScoreTv.setText(String.valueOf(data.getFinalScore()));
        mFinalCreditTv.setText(String.valueOf(data.getCredit()));
        mCompleteStateTv.setText(data.getState() == 1 ? "合格" : "挂科");

        mTeacherTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Call<TeacherInfoResult> call = mApiService.getTeacherBaseByTeacherId(data.getTeacherBase().getTeacherId());
                call.enqueue(new BaseCallback<TeacherInfoResult>() {
                    @Override
                    public void onSuccess(TeacherInfoResult result) {
                        if (result.getCode() != 0) {
                            return;
                        }
                        TeacherInfoResult.DataBean baseInfo = result.getData();

                        AlertDialog dialog = new AlertDialog.Builder(CourseInfoActivity.this)
                                .setContentView(R.layout.dialog_teacher_base_info)
                                .addDefaultAnimation()
                                .setCanceledOnTouchOutside(true)
                                .create();

                        ImageLoaderManager.getInstance().showImage(
                                dialog.getContentView().findViewById(R.id.iv_base_teacher_image),
                                RetrofitClient.URL + baseInfo.getImage(), null);
                        dialog.setText(R.id.tv_base_teacher_name, baseInfo.getName());
                        dialog.setText(R.id.tv_base_teacher_identity, baseInfo.getIdentity());
                        dialog.setText(R.id.tv_base_teacher_phone, baseInfo.getPhone());
                        dialog.setText(R.id.tv_base_teacher_address, baseInfo.getOfficeAddress());


                        dialog.show();
                    }
                });
            }
        });
    }
}
