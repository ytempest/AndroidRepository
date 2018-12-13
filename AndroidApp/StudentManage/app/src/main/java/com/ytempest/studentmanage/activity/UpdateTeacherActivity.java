package com.ytempest.studentmanage.activity;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.imageloader.LoaderOptions;
import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.baselibrary.view.load.LoadingDialog;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.http.ApiService;
import com.ytempest.studentmanage.http.RetrofitClient;
import com.ytempest.studentmanage.http.RetrofitUtils;
import com.ytempest.studentmanage.http.callback.BaseCallback;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.ManageDepartmentListResult;
import com.ytempest.studentmanage.model.TeacherInfoResult;
import com.ytempest.studentmanage.thread.MainThreadExecutor;
import com.ytempest.studentmanage.util.CommonUtils;
import com.ytempest.studentmanage.util.Config;
import com.ytempest.studentmanage.util.PatternUtils;
import com.ytempest.studentmanage.util.ResourcesUtils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author ytempest
 *         Description：
 */
public class UpdateTeacherActivity extends SelectImageActivity {

    private static final String TAG = "UpdateTeacherActivity";

    @BindView(R.id.iv_teacher_image)
    protected ImageView mImageIv;

    @BindView(R.id.et_teacher_id)
    protected EditText mIdEt;

    @BindView(R.id.et_teacher_password)
    protected EditText mPasswordEt;

    @BindView(R.id.cb_teacher_reset_password)
    protected CheckBox mResetPasswordCb;

    @BindView(R.id.et_teacher_name)
    protected EditText mNameEt;

    @BindView(R.id.rg_teacher_sex)
    protected RadioGroup mSexRg;

    @BindView(R.id.tv_teacher_department)
    protected TextView mDepartmentTv;

    @BindView(R.id.tv_teacher_identity)
    protected EditText mIdentityEt;

    @BindView(R.id.et_teacher_political_status)
    protected EditText mPoliticalStatusEt;

    @BindView(R.id.et_teacher_nationality)
    protected EditText mNationalityEt;

    @BindView(R.id.et_teacher_phone)
    protected EditText mPhoneEt;

    @BindView(R.id.et_teacher_address)
    protected EditText mAddressEt;

    @BindView(R.id.et_teacher_enrollment_date)
    protected EditText mEnrollmentDateEt;

    @BindView(R.id.rg_teacher_state)
    protected RadioGroup mStateRg;


    @BindView(R.id.bt_teacher_insert)
    protected Button mButton;

    private ApiService mApiService;
    private List<ManageDepartmentListResult.DataBean> mDepartmentList;
    private int mDepartmentId = -1;
    private String mImageUrl;
    private String mPassword;
    private boolean isChangedImage = false;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_insert_teacher;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(UpdateTeacherActivity.this)
                .setTitle("修改教师信息")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();

    }

    @Override
    protected void initView() {
        mButton.setText("确认修改");
        mPasswordEt.setVisibility(View.GONE);
        mResetPasswordCb.setVisibility(View.VISIBLE);
    }


    @Override
    protected void initData() {
        super.initData();
        mApiService = RetrofitClient.client().create(ApiService.class);

        final LoadingDialog loadingDialog = new LoadingDialog(UpdateTeacherActivity.this);
        loadingDialog.show();

        final String teacherId = getIntent().getStringExtra(ManageTeacherActivity.TEACHER_ID);
        mApiService.getTeacherInfoByTeacherId(teacherId)
                .enqueue(new BaseCallback<TeacherInfoResult>() {
                    @Override
                    public void onSuccess(TeacherInfoResult result) {
                        TeacherInfoResult.DataBean data = result.getData();

                        mImageUrl = RetrofitClient.URL + data.getImage();
                        ImageLoaderManager.getInstance().showImage(mImageIv,
                                mImageUrl,
                                new LoaderOptions.Builder().errorDrawableId(R.drawable.icon_load_failure).build());
                        mIdEt.setText(teacherId);
                        mPassword = data.getPassword();
                        mPasswordEt.setText("");
                        mPasswordEt.setHint("如需修改密码请输入新密码");
                        mNameEt.setText(data.getName());
                        int sex = "male".equals(data.getSex()) ? R.id.male : R.id.female;
                        ((RadioButton) UpdateTeacherActivity.this.findViewById(sex)).setChecked(true);
                        mDepartmentTv.setText(data.getDepartmentName());
                        mDepartmentId = data.getDepartmentId();
                        mIdentityEt.setText(data.getIdentity());
                        mPoliticalStatusEt.setText(data.getPoliticalStatus());
                        mNationalityEt.setText(data.getNationality());
                        mPhoneEt.setText(data.getPhone());
                        mAddressEt.setText(data.getAddress());
                        mEnrollmentDateEt.setText(
                                new SimpleDateFormat("yyyy-MM-dd").format(new Date(data.getEnrollmentDate())));
                        int state = data.getState() == 1 ? R.id.normal : (data.getState() == 0 ? R.id.dismiss : R.id.quit);
                        ((RadioButton) UpdateTeacherActivity.this.findViewById(state)).setChecked(true);
                        loadingDialog.dismiss();
                    }
                });
    }


    @Override
    protected void onImageSelected(Bitmap bitmap) {
        mImageIv.setImageBitmap(bitmap);
        isChangedImage = true;
    }

    @OnClick(R.id.iv_teacher_image)
    protected void onImageClick(View view) {
        selectImage();
    }


    @OnClick(R.id.tv_teacher_department)
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
                UpdateTeacherActivity.this, mDepartmentList, R.layout.recycler_view_show_list_item) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageDepartmentListResult.DataBean item) {
                holder.setText(R.id.tv_item, item.getName());

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDepartmentTv.setText(item.getName());
                        mDepartmentId = item.getDepartmentId();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }


    @OnClick(R.id.bt_teacher_insert)
    protected void onUpdateClick(View view) {

        if (!PatternUtils.isPhone(mPhoneEt.getText().toString())) {
            showToastShort(ResourcesUtils.getString(R.string.phone_pattern_error));
            return;
        }


        final LoadingDialog loadingDialog = new LoadingDialog(UpdateTeacherActivity.this);
        loadingDialog.show();

        // 缓存头像，如果不修改头像，则上传原来的头像
        mApiService.downloadFile(mImageUrl).enqueue(new BaseCallback<InputStream>() {
            @Override
            public void onSuccess(final InputStream inputStream) {

                CommonUtils.writeToFile(inputStream, Config.HEAD_TEM);

                MainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        commitData(loadingDialog);
                    }
                });

            }
        });

    }


    private void commitData(final LoadingDialog loadingDialog) {
        File imageFile;
        if (isChangedImage) {
            // 选择修改后的图片
            imageFile = mImageFile;
        } else {
            imageFile = new File(Config.HEAD_TEM);
        }

        MultipartBody.Part imagePart = RetrofitUtils.createPartFromFile("image", imageFile);

        Map<String, RequestBody> partMap = getPartMap();

        mApiService.updateTeacher(imagePart, partMap)
                .enqueue(new BaseCallback<CommonResult>() {
                    @Override
                    public void onSuccess(CommonResult result) {
                        if (result.getCode() == 0) {
                            showToastShort(ResourcesUtils.getString(R.string.update_success));
                        } else {
                            showToastShort(result.getMsg());
                        }
                        loadingDialog.dismiss();
                        finish();
                    }
                });
    }

    @NonNull
    private Map<String, RequestBody> getPartMap() {
        Map<String, RequestBody> partMap;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            partMap = new ArrayMap<>(11);
        } else {
            partMap = new HashMap<>(11);
        }

        String password = mPassword;
        if (mResetPasswordCb.isChecked()) {
            password = "1234";
        }

        partMap.put("teacherId", RetrofitUtils.createBodyFromString(mIdEt.getText().toString()));
        partMap.put("password", RetrofitUtils.createBodyFromString(password));
        partMap.put("name", RetrofitUtils.createBodyFromString(mNameEt.getText().toString()));
        partMap.put("sex", RetrofitUtils.createBodyFromString(
                mSexRg.getCheckedRadioButtonId() == R.id.male ? "male" : "female"));
        partMap.put("departmentId", RetrofitUtils.createBodyFromString(String.valueOf(mDepartmentId)));
        partMap.put("identity", RetrofitUtils.createBodyFromString(mIdentityEt.getText().toString()));
        partMap.put("politicalStatus", RetrofitUtils.createBodyFromString(mPoliticalStatusEt.getText().toString()));
        partMap.put("nationality", RetrofitUtils.createBodyFromString(mNationalityEt.getText().toString()));
        partMap.put("enrollmentDate", RetrofitUtils.createBodyFromString(mEnrollmentDateEt.getText().toString()));
        partMap.put("phone", RetrofitUtils.createBodyFromString(mPhoneEt.getText().toString()));
        partMap.put("address", RetrofitUtils.createBodyFromString(mAddressEt.getText().toString()));
        partMap.put("state", RetrofitUtils.createBodyFromString(
                mStateRg.getCheckedRadioButtonId() == R.id.normal ?
                        "1" : (mStateRg.getCheckedRadioButtonId() == R.id.dismiss ? "0" : "2")));
        return partMap;
    }

    private RecyclerView getRecyclerView(AlertDialog dialog) {
        RecyclerView recyclerView = dialog.getView(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(UpdateTeacherActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(UpdateTeacherActivity.this, DividerItemDecoration.VERTICAL));
        return recyclerView;
    }

    private AlertDialog createDialog() {
        return new AlertDialog.Builder(UpdateTeacherActivity.this)
                .setContentView(R.layout.dialog_show_list)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }
}

