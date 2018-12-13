package com.ytempest.studentmanage.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.view.button.ModifiableButton;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.fragment.CourseFragment;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.listener.TextWatcherListener;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.CourseInfoResult;
import com.ytempest.studentmanage.model.StudentInfoResult;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;

/**
 * @author ytempest
 *         Description：
 */
public class StudentScoreInfoActivity extends BaseSkinActivity {

    private static final String TAG = "StudentScoreInfo";

    @BindView(R.id.scroll_view)
    protected ScrollView mScrollView;

    @BindView(R.id.root_view)
    protected LinearLayout mRootView;

    @BindView(R.id.iv_user_image)
    protected ImageView mImageIv;

    @BindView(R.id.tv_id)
    protected TextView mIdTv;

    @BindView(R.id.tv_user_name)
    protected TextView mNameTv;

    @BindView(R.id.tv_user_department)
    protected TextView mDepartmentTv;

    @BindView(R.id.tv_user_major)
    protected TextView mMajorTv;

    @BindView(R.id.tv_class)
    protected TextView mClassTv;

    @BindView(R.id.tv_phone)
    protected TextView mPhoneTv;

    @BindView(R.id.et_daily_score)
    protected EditText mDailyScoreEt;

    @BindView(R.id.et_exam_score)
    protected EditText mExamScoreEt;

    @BindView(R.id.tv_final_score)
    protected TextView mFinalScoreTv;

    @BindView(R.id.tv_final_credit)
    protected TextView mFinalCreditTv;

    @BindView(R.id.bt_update_score)
    protected ModifiableButton mUpdateScoreBt;

    private double mCredit;
    private ApiService mApiService;
    private int mStudentId;
    private int mChildCourseId;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_student_score_info;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(StudentScoreInfoActivity.this, mRootView)
                .setTitle("学生课程信息")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();
    }

    @Override
    protected void initView() {
        mDailyScoreEt.addTextChangedListener(new TextWatcherListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFinish();
            }
        });

        mExamScoreEt.addTextChangedListener(new TextWatcherListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFinish();
            }
        });

        mUpdateScoreBt.switchDisableStatus();

    }

    private void checkInputFinish() {
        String dailyScoreStr = mDailyScoreEt.getText().toString();
        String examScoreStr = mExamScoreEt.getText().toString();
        if (!TextUtils.isEmpty(dailyScoreStr)
                && !TextUtils.isEmpty(examScoreStr)) {
            mUpdateScoreBt.switchNormalStatus();

            int dailyScore = Integer.valueOf(dailyScoreStr);
            int examScore = Integer.valueOf(examScoreStr);
            double finalScore = (dailyScore + examScore) / 2.0;
            mFinalScoreTv.setText(String.valueOf(finalScore));
            mFinalCreditTv.setText(String.valueOf(finalScore / 100.0 * mCredit));
        } else {
            mUpdateScoreBt.switchNormalStatus();
        }
    }

    @Override
    protected void initData() {
        mStudentId = getIntent().getIntExtra(StudentListActivity.STUDENT_ID, 0);
        mCredit = getIntent().getDoubleExtra(CourseFragment.COURSE_CREDIT, 0);
        mChildCourseId = getIntent().getIntExtra(CourseFragment.CHILD_COURSE_ID, 0);

        mApiService = RetrofitClient.client().create(ApiService.class);
        Call<CourseInfoResult> courseInfoCall = mApiService.getCourseInfo(String.valueOf(mStudentId), mChildCourseId);
        courseInfoCall.enqueue(new BaseCallback<CourseInfoResult>() {
            @Override
            public void onSuccess(CourseInfoResult result) {
                loadScoreToLayout(result.getData());
            }
        });

        Call<StudentInfoResult> call = mApiService.getStudentBaseInfoByStudentId(mStudentId);
        call.enqueue(new BaseCallback<StudentInfoResult>() {
            @Override
            public void onSuccess(StudentInfoResult result) {
                loadDataToLayout(result.getData());
            }
        });
    }

    private void loadScoreToLayout(CourseInfoResult.DataBean data) {
        mDailyScoreEt.setText(String.valueOf(data.getDailyScore()));
        mExamScoreEt.setText(String.valueOf(data.getExamScore()));
        mFinalScoreTv.setText(String.valueOf(data.getFinalScore()));
        mFinalCreditTv.setText(String.valueOf(data.getCredit()));
    }

    private void loadDataToLayout(StudentInfoResult.DataBean data) {
        ImageLoaderManager.getInstance().showImage(mImageIv, RetrofitClient.URL + data.getImage(),
                new LoaderOptions.Builder().errorDrawableId(R.drawable.icon_load_failure).build());
        mIdTv.setText(String.valueOf(data.getStudentId()));
        mNameTv.setText(data.getName());
        mDepartmentTv.setText(data.getDepartmentName());
        mMajorTv.setText(data.getMajorName());
        mClassTv.setText(data.getClassName());
        mPhoneTv.setText(data.getPhone());
    }


    @OnClick(R.id.bt_update_score)
    protected void onUpdateScoreClick(View view) {
        String finalScore = mFinalScoreTv.getText().toString();
        Call<CommonResult> call = mApiService.updateStudentCourseScore(mStudentId,
                mChildCourseId,
                mDailyScoreEt.getText().toString(),
                mExamScoreEt.getText().toString(),
                Math.round(Float.parseFloat(finalScore)),
                mCredit,
                Double.valueOf(finalScore) >= 60 ? 1 : 0);

        call.enqueue(new BaseCallback<CommonResult>() {
            @Override
            public void onSuccess(CommonResult result) {
                showToastShort("修改成绩" + result.getMsg());
                finish();
            }
        });

    }
}
