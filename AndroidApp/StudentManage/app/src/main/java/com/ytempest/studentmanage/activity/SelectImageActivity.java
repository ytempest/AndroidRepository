package com.ytempest.studentmanage.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.ytempest.baselibrary.view.dialog.AlertDialog;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.util.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author ytempest
 *         Description：
 */
public abstract class SelectImageActivity extends BaseSkinActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_ALBUM_OR_PHONE = 10;
    private static final int REQUEST_CAMERA = 20;
    private static final int REQUEST_CLIP_PHOTO = 30;

    private static final int REQUEST_WRITE_PERMISSION = 100;
    private static final int REQUEST_PERMISSION_CAMERA = 200;

    /**
     * 头像的存储文件
     */
    protected File mImageFile;


    @Override
    protected void initData() {
        mImageFile = new File(Config.HEAD_IMAGE);
    }

    protected void selectImage() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setContentView(R.layout.dialog_choose_photo)
                .formBottom(true)
                .fullWidth()
                .setCanceledOnTouchOutside(true)
                .show();

        // 从手机或相册中选择图片
        dialog.setOnClickListener(R.id.bt_form_phone, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWrite();
                dialog.cancel();
            }
        });

        // 拍照选择图片
        dialog.setOnClickListener(R.id.bt_form_camera, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCamera();
                dialog.cancel();
            }
        });

    }

    protected void requestWrite() {
        // 申请拍照权限
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            onGranted(REQUEST_WRITE_PERMISSION);
        } else {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            EasyPermissions.requestPermissions(this,
                    "需要获取手机的读写权限", REQUEST_WRITE_PERMISSION, perms);
        }
    }

    /**
     * 申请相机权限
     */
    protected void requestCamera() {
        // 申请拍照权限
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            onGranted(REQUEST_PERMISSION_CAMERA);
        } else {
            String[] perms = {Manifest.permission.CAMERA};
            EasyPermissions.requestPermissions(this,
                    "拍照需要摄像头权限", REQUEST_PERMISSION_CAMERA, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // EasyPermissions 权限处理请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //同意授权
        onGranted(requestCode);
    }

    protected void onGranted(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
            startActivityForResult(intent, REQUEST_CAMERA);
        } else if (requestCode == REQUEST_WRITE_PERMISSION) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, REQUEST_ALBUM_OR_PHONE);
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //拒绝授权
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            showToastShort("请授予拍照的权限");
        } else if (requestCode == REQUEST_WRITE_PERMISSION) {
            showToastShort("请授予对手机读写的权限");
        }
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

    /**
     * 处理剪裁成功后的图片
     */
    protected void clipPhotoSuccess(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            // 获取裁剪后的图片
            Bitmap bitmap = bundle.getParcelable("data");

            onImageSelected(bitmap);

            // 存储到文件中，然后等待上传到服务器中
            saveBitmap(bitmap);
        }
    }

    /**
     * 将 bitmap 保存到手机内存指定的文件夹中
     */
    protected void saveBitmap(Bitmap bitmap) {
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
    protected void clipImage(Uri imageUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");

        // 设置裁剪的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // 设置剪裁后的宽度，单位：像素
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);

        // 设置是否返回裁剪后相片的bitmap对象
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CLIP_PHOTO);
    }

    /**
     * 选择图片后回调该方法，并将选择的图片的Bitmap传递过去
     */
    abstract protected void onImageSelected(Bitmap bitmap);

}
