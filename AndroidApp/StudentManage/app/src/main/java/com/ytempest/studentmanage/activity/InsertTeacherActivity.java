package com.ytempest.studentmanage.activity;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

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
import com.ytempest.studentmanage.model.ManageIdResult;
import com.ytempest.studentmanage.util.PatternUtils;
import com.ytempest.studentmanage.util.ResourcesUtils;

import java.io.File;
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
public class InsertTeacherActivity extends SelectImageActivity {

    private static final String TAG = "InsertTeacherActivity";

    @BindView(R.id.iv_teacher_image)
    protected ImageView mImageIv;

    @BindView(R.id.et_teacher_id)
    protected EditText mIdEt;

    @BindView(R.id.et_teacher_password)
    protected EditText mPasswordEt;

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
    protected Button mConfirmUpdateBt;

    private ApiService mApiService;
    private List<ManageDepartmentListResult.DataBean> mDepartmentList;
    private int mDepartmentId = -1;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_insert_teacher;
    }

    @Override
    protected void initTitle() {
        new DefaultNavigationBar.Builder(InsertTeacherActivity.this)
                .setTitle("添加教师")
                .setLeftIcon(R.drawable.navigation_white_back_arrow)
                .setTitleColor(R.color.title_bar_text_color)
                .setBackground(R.color.title_bar_bg_color)
                .build();

    }

    @Override
    protected void initView() {
        mSexRg.check(R.id.male);
        mStateRg.check(R.id.normal);

    }


    @Override
    protected void initData() {
        super.initData();
        mApiService = RetrofitClient.client().create(ApiService.class);
    }

    @Override
    protected void onImageSelected(Bitmap bitmap) {
        mImageIv.setImageBitmap(bitmap);
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
                InsertTeacherActivity.this, mDepartmentList, R.layout.recycler_view_show_list_item) {
            @Override
            protected void bindViewData(CommonViewHolder holder, final ManageDepartmentListResult.DataBean item) {
                holder.setText(R.id.tv_item, item.getName());

                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDepartmentTv.setText(item.getName());
                        mDepartmentId = item.getDepartmentId();
                        mApiService.getNewTeacherId(mDepartmentId)
                                .enqueue(new BaseCallback<ManageIdResult>() {
                                    @Override
                                    public void onSuccess(ManageIdResult result) {
                                        mIdEt.setText(String.valueOf(result.getData()));
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
            }
        });
        dialog.show();
    }


    @OnClick(R.id.iv_teacher_image)
    protected void onImageSelectClick(View view) {
        selectImage();
    }


    @OnClick(R.id.bt_teacher_insert)
    protected void onInsertClick(View view) {

        if (!PatternUtils.isPhone(mPhoneEt.getText().toString())) {
            showToastShort(ResourcesUtils.getString(R.string.phone_pattern_error));
            return;
        }

        final LoadingDialog loadingDialog = new LoadingDialog(InsertTeacherActivity.this);
        loadingDialog.show();

        if (mDepartmentId != -1) {

            File imageFile = mImageFile;

            MultipartBody.Part imagePart = RetrofitUtils.createPartFromFile("image", imageFile);

            Map<String, RequestBody> partMap = getPartMap();

            mApiService.insertTeacher(imagePart, partMap)
                    .enqueue(new BaseCallback<CommonResult>() {
                        @Override
                        public void onSuccess(CommonResult result) {
                            if (result.getCode() == 0) {
                                showToastShort(ResourcesUtils.getString(R.string.insert_success));
                            } else {
                                showToastShort(result.getMsg());
                            }
                            loadingDialog.dismiss();
                            finish();
                        }
                    });
        }
    }

    @NonNull
    private Map<String, RequestBody> getPartMap() {
        Map<String, RequestBody> partMap;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            partMap = new ArrayMap<>(11);
        } else {
            partMap = new HashMap<>(11);
        }
        partMap.put("teacherId", RetrofitUtils.createBodyFromString(mIdEt.getText().toString()));
        partMap.put("password", RetrofitUtils.createBodyFromString(mPasswordEt.getText().toString()));
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
        recyclerView.setLayoutManager(new LinearLayoutManager(InsertTeacherActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(InsertTeacherActivity.this, DividerItemDecoration.VERTICAL));
        return recyclerView;
    }

    private AlertDialog createDialog() {
        return new AlertDialog.Builder(InsertTeacherActivity.this)
                .setContentView(R.layout.dialog_show_list)
                .addDefaultAnimation()
                .setCanceledOnTouchOutside(true)
                .create();
    }


}

