package com.ytempest.andfixdemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ytempest.andfixdemo.andfix.PatchManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final File mPatchFile = new File("/mnt/shared/Other/fix.jar");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onCalculateClick(View view) {
        int result = new Calculator().calculate();
        Toast.makeText(this, result + "", Toast.LENGTH_SHORT).show();
    }


    public void onFixClick(View view) {
        String patchPath = mPatchFile.getAbsolutePath();
        Toast.makeText(this, mPatchFile.exists() ? "文件存在" : "文件不存在", Toast.LENGTH_SHORT).show();
        PatchManager patchManager = new PatchManager(MainActivity.this);
        patchManager.loadPatch(patchPath);
    }


}
