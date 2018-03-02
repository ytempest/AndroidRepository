package com.ytempest.bsdiffdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ytempest.bsdiffdemo.util.PatchUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String mNewApkPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "version_2.0.apk";

    private String mPatchPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "version_1.0_2.0.patch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void update(View view) {
        if (!new File(mPatchPath).exists()) {
            Toast.makeText(this, "patch包不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        // 1、获取本地的getPackageResourcePath()apk路径
        String currentApkPath = getPackageResourcePath();

        // 2、下载完差分包之后，调用我们的方法去合并生成新的apk
        // 是一个耗时操作，怎么弄 开线程+，Handler, AsyncTask , RXJava


        ///3、合并差分包生成新版本的APK，新版本的APK路径为 mNewApkPath
        PatchUtils.combine(currentApkPath, mNewApkPath, mPatchPath);

        ///4、检验签名，就是获取本地apk的签名，与我们新版本的apk作对比
        try {
            String currentApkSignature = PatchUtils.getSignature(getPackageResourcePath());
            String newApkSignature = PatchUtils.getSignature(mNewApkPath);
            if (currentApkSignature.equals(newApkSignature)) {
                Toast.makeText(this, "检验签名成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "检验签名失败，请检查安装包", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5、安装新版本的Apk
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(mNewApkPath)),
                "application/vnd.android.package-archive");
        startActivity(intent);
        Toast.makeText(this, "更新完成", Toast.LENGTH_SHORT).show();
    }
}
