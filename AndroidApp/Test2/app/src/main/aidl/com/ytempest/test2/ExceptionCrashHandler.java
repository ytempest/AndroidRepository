package com.ytempest.test2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import com.alipay.euler.andfix.util.FileUtil;
import com.ytempest.baselibrary.util.FileUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-12-16.
 */
public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {
    private static String CRASH_FILE_NAME = "crash_file_name";
    private static ExceptionCrashHandler mInstance = null;

    private Context mContext;

    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    private ExceptionCrashHandler() {

    }

    public static ExceptionCrashHandler getInstance() {
        if (mInstance == null) {
            synchronized (ExceptionCrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new ExceptionCrashHandler();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        Thread.currentThread().setUncaughtExceptionHandler(this);

        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String fileName = saveInfoToSD(e);

        cacheCrashFile(fileName);

        mDefaultExceptionHandler.uncaughtException(t, e);

    }

    private void cacheCrashFile(String fileName) {
        SharedPreferences sp = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE);
        sp.edit().putString(CRASH_FILE_NAME, fileName).apply();
    }

    private String saveInfoToSD(Throwable e) {
        String fileName = null;
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : obtainSimpleInfo().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        sb.append(obtainExceptionInfo(e));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(mContext.getFilesDir() + File.separator + "crash" + File.separator);
            if (dir.exists()) {
                FileUtils.deleteDir(dir);
            }
            if (!dir.exists()) {
                dir.mkdir();
            }
            try{
                fileName = dir.toString() + getAssignTime("yyyy_MM_dd_mm") + ".txt";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return fileName;
    }

    private String getAssignTime(String dateFormatStr) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        long currentTime = System.currentTimeMillis();
        return dateFormat.format(currentTime);
    }

    private String obtainExceptionInfo(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private HashMap<String, String> obtainSimpleInfo() {
        HashMap<String, String> map = new HashMap<String, String>();
        PackageManager manager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            map.put("versionName", "" + packageInfo.versionName);
            map.put("versionCode", "" + packageInfo.versionCode);
            map.put("MODEL", Build.MODEL);
            map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
            map.put("product", Build.PRODUCT);
            map.put("MOBILE", obtainMobileInfo());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }



    private String obtainMobileInfo() {
        StringBuilder sb = new StringBuilder();
        Field[] fields = Build.class.getDeclaredFields();
        try{
            for (Field field : fields) {
                field.setAccessible(true);
                String key = field.getName();
                String value = (String) field.get(null);
                sb.append(key).append("=").append(value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
