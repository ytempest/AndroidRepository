package com.ytempest.daydayantis.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ytempest.baselibrary.http.EngineCallBack;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.permission.PermissionFail;
import com.ytempest.baselibrary.permission.PermissionSucceed;
import com.ytempest.baselibrary.permission.SmartPermission;
import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.daydayantis.R;
import com.ytempest.daydayantis.data.BaseDataResult;
import com.ytempest.daydayantis.data.UserDataResult;
import com.ytempest.daydayantis.utils.UserInfoUtils;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.http.HttpCallBack;
import com.ytempest.framelibrary.view.navigation.DefaultNavigationBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

public class UserInfoActivity extends BaseSkinActivity {



    private final int REQUEST_ALBUM_OR_PHONE = 100;
    private final int REQUEST_CAMERA = 200;
    private final int REQUEST_CLIP_PHOTO = 300;

    /**
     * 申请拍照权限的请求码
     */
    private final int REQUEST_PERMISSION_CAMERA = 0x0011;

    @ViewById(R.id.ll_user_info_root)
    private LinearLayout mRootView;

    @ViewById(R.id.iv_user_head)
    private ImageView mUserHead;

    @ViewById(R.id.tv_user_name)
    private TextView mUserName;
    @ViewById(R.id.tv_user_phone)
    private TextView mUserPhone;

    /**
     * 更换头像后的照片文件
     */
    private File mImageFile;
    private UserDataResult.DataBean mUserDataResult;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected void initTitle() {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        DefaultNavigationBar navigationBar =
                new DefaultNavigationBar.Builder(UserInfoActivity.this, mRootView)
                        .setTitle(R.string.activity_user_info_title_bar)
                        .setLeftIcon(R.drawable.icon_back)
                        .setTitleColor(R.color.title_bar_text_color)
                        .setBackground(R.color.title_bar_bg_color)
                        .build();


    }

    @Override
    protected void initView() {
        // 获取用户信息
        mUserDataResult = getUserInfo();
        // 设置用户信息
        setUserDataToLayout();
    }

    private UserDataResult.DataBean getUserInfo() {
        String userInfo = UserInfoUtils.getUserInfo(UserInfoActivity.this);
        if (!TextUtils.isEmpty(userInfo)) {
            return new Gson().fromJson(userInfo, UserDataResult.DataBean.class);
        }
        return null;
    }

    /**
     * 将用户信息设置到界面中
     */
    private void setUserDataToLayout() {
        if (mUserDataResult == null) {
            return;
        }
        // 设置用户头像
        ImageLoaderManager.getInstance().showImage(mUserHead,
                mUserDataResult.getMember_info().getMember_avatar(), null);

        // 设置用户名
        mUserName.setText(mUserDataResult.getMember_info().getMember_name());

        // 设置用户手机号码
        mUserPhone.setText(mUserDataResult.getMember_info().getMember_cell_phone());
    }

    @Override
    protected void initData() {
        // 创建存储头像图片的文件夹
        File targetDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "user");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        // 初始化头像图片文件的路径
        mImageFile = new File(targetDir, "head.png");

    }

    /**
     * 用户头像的点击事件
     */
    @OnClick(R.id.iv_user_head)
    private void onUserHeadClick(View view) {
        final AlertDialog dialog = new AlertDialog.Builder(UserInfoActivity.this)
                .setContentView(R.layout.dialog_choose_photo)
                .formBottom(true)
                .fullWidth()
                .setCanceledOnTouchOutside(true)
                .show();

        // 从手机或相册中选择图片
        dialog.setOnClickListener(R.id.bt_form_phone, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_ALBUM_OR_PHONE);
                dialog.cancel();
            }
        });

        // 拍照选择图片
        dialog.setOnClickListener(R.id.bt_form_camera, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 申请拍照权限
                SmartPermission.with(UserInfoActivity.this)
                        .requestCode(REQUEST_PERMISSION_CAMERA)
                        .requestPermission(Manifest.permission.CAMERA)
                        .request();
                dialog.cancel();
            }
        });

        // 取消
        dialog.setOnClickListener(R.id.bt_cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    /**
     * 提交按钮的点击事件
     */
    @OnClick(R.id.bt_commit)
    private void onCommitClick(View view) {
        // 把用户修改后的数据上传到服务器
        uploadImage();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ALBUM_OR_PHONE:
                if (resultCode == RESULT_OK) {
                    // 获取用户选择后的图片的uri地址
                    Uri imageUri = data.getData();
                    // 使用系统的Activity裁剪图片
                    clipImage(imageUri);
                }
                break;

            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    clipImage(Uri.fromFile(mImageFile));
                }
                break;

            case REQUEST_CLIP_PHOTO:
                // 裁剪成功后
                if (resultCode == RESULT_OK) {
                    clipPhotoSuccess(data);
                }
                break;

            default:
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        SmartPermission.onRequestPermissionResult(UserInfoActivity.this,
                REQUEST_PERMISSION_CAMERA, grantResults);

    }

    @PermissionSucceed(requestCode = REQUEST_PERMISSION_CAMERA)
    private void requestCameraSucceed() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @PermissionFail(requestCode = REQUEST_PERMISSION_CAMERA)
    private void requestCameraFail() {
        showToastShort("请授予应用拍照的权限");
    }

    /**
     * 处理剪裁成功后的图片
     */
    private void clipPhotoSuccess(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            // 获取裁剪后的图片
            Bitmap bitmap = bundle.getParcelable("data");

            // 1、设置用户头像
            mUserHead.setImageBitmap(bitmap);

            // 2、存储到文件中，然后等待上传到服务器中
            saveBitmap(bitmap);
        }
    }

    /**
     * 上传到服务器
     */
    private void uploadImage() {
        HttpUtils.with(UserInfoActivity.this)
                .addParam("appid", "1")
                .addParam("uid", mUserDataResult.getMember_info().getUid())
                .addParam("file", mImageFile)
                .post()
                .url("http://v2.ffu365.com/index.php?m=Api&c=Member&a=userUploadAvatar")
                .execute(new HttpCallBack<BaseDataResult>() {
                    @Override
                    public void onPreExecute() {

                    }

                    @Override
                    public void onSuccess(BaseDataResult result) {
                        showToastShort(result.getErrmsg());
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    /**
     * 将 bitmap 保存到手机内存指定的文件夹中
     */
    private void saveBitmap(Bitmap bitmap) {
        try {
            OutputStream os = new FileOutputStream(mImageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用系统的Activity裁剪图片
     */
    private void clipImage(Uri imageUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");

        // 设置裁剪的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // 设置剪裁后的宽度，单位：像素
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        // 设置是否返回裁剪后相片的bitmap对象
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CLIP_PHOTO);
    }
}
