package com.ytempest.bsdiffdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ytempest.bsdiffdemo.util.ApkUtils;
import com.ytempest.bsdiffdemo.util.Constants;
import com.ytempest.bsdiffdemo.util.DownloadUtils;
import com.ytempest.bsdiffdemo.util.PatchUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void update(View view) {
        updateForNet();
    }


    /**
     * 从服务器获取差分包进行更新
     */
    private void updateForNet() {
        new ApkUpdateTask().execute();
    }

    /**
     * 从本地获取差分包进行增量更新
     */
    private void updateForLocal() {
        if (!new File(Constants.PATCH_FILE_PATH).exists()) {
            Toast.makeText(this, "patch包不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1、获取本地的getPackageResourcePath()apk路径
        String currentApkPath = getPackageResourcePath();

        // 2、下载完差分包之后，调用我们的方法去合并生成新的apk
        // 是一个耗时操作，怎么弄 开线程+，Handler, AsyncTask , RXJava


        // 3、合并差分包生成新版本的APK，新版本的APK路径为 mNewApkPath
        PatchUtils.patch(currentApkPath, Constants.NEW_APK_PATH, Constants.PATCH_FILE_PATH);

        // 4、检验签名，就是获取本地apk的签名，与我们新版本的apk作对比
        checkSignature(currentApkPath, Constants.NEW_APK_PATH);

        // 5、安装新版本的Apk
        ApkUtils.installApk(this, Constants.NEW_APK_PATH);
        Toast.makeText(this, "更新完成", Toast.LENGTH_SHORT).show();
    }

    private void checkSignature(String apkPath1, String apkPath2) {
        try {
            String currentApkSignature = PatchUtils.getSignature(apkPath1);
            String newApkSignature = PatchUtils.getSignature(apkPath2);
            if (currentApkSignature.equals(newApkSignature)) {
                Toast.makeText(this, "检验签名成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "检验签名失败，请检查安装包", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // 开始下载差分包
                DownloadUtils.download(Constants.URL_PATCH_DOWNLOAD, Constants.PATCH_FILE_PATH);

                // 开始合并差分包
                // 获取当前版本的apk文件
                String curApkPath = ApkUtils.getSourceApkPath(MainActivity.this, Constants.PACKAGE_NAME);

                PatchUtils.patch(curApkPath, Constants.NEW_APK_PATH, Constants.PATCH_FILE_PATH);

                // 检验签名，就是获取本地apk的签名，与我们新版本的apk作对比
                checkSignature(curApkPath, Constants.NEW_APK_PATH);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(MainActivity.this, "现在进行更新无流量更新，点击进行安装", Toast.LENGTH_SHORT).show();
                ApkUtils.installApk(MainActivity.this, Constants.NEW_APK_PATH);
            }
        }
    }
}
