package com.ytempest.baselibrary.exception;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.ytempest.baselibrary.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ytempest
 * Description:  捕获全局异常的类，并记录异常日志（单线程）
 */
public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "ExceptionCrashHandler";
    private static final String CRASH_FILE_NAME = "crash_file_name";
    /** 系统默认的全局异常类 */
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

    /**
     * 初始化自己的异常捕获器
     * @param context 应用的上下文
     */
    public void init(Context context) {
        this.mContext = context;
        // 设置全局的异常类为本类
        Thread.currentThread().setUncaughtExceptionHandler(this);
        // 获取系统默认的全局异常类
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * 当出现异常时，会调用此方法
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // 全局异常
        Log.e(TAG, "出现异常..");

        // 写入本地文件的步骤
        // 1. 崩溃的详细信息
        // 2. 应用信息 包名 版本号
        // 3. 手机信息
        // 4.保存当前文件，等应用再次启动再上传，（上传文件不在这里处理）


        // 保存日志文件，然后获取该日志文件路径
        String crashFileName = saveInfoToSD(throwable);

        // 缓存日志文件
        cacheCrashFile(crashFileName);

        // 让系统默认的全局异常类按原来的方式处理异常
        mDefaultExceptionHandler.uncaughtException(thread, throwable);
    }


    /**
     * 缓存崩溃的日志信息
     * @param fileName 缓存文件的绝对路径和名称
     */
    private void cacheCrashFile(String fileName) {
        SharedPreferences sp = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE);
        sp.edit().putString(CRASH_FILE_NAME, fileName).apply();
    }

    /**
     * 对外开放，获取保存了日志信息的txt文件
     * @return 保存了异常日志的txt文件
     */
    public File getCrashFile() {
        String fileName = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE).getString(CRASH_FILE_NAME, "");
        return new File(fileName);
    }


    /**
     *  将崩溃信息，崩溃时手机的信息，应用的信息保存到sd卡中（应用程序的内部存储器）
     * @param throwable 崩溃时的throwable
     * @return 保存了异常日志的txt文件的绝对路径和名称
     */
    private String saveInfoToSD(Throwable throwable) {
        // 保存所有信息的文件路径+名称
        String fileName = null;
        StringBuilder sb = new StringBuilder();
        // 获取 应用信息和手机信息
        for (Map.Entry<String, String> entry : obtainSimpleInfo(mContext).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }
        // 获取程序崩溃时未捕获的异常
        sb.append(obtainExceptionInfo(throwable));
        // 判断外部存储器是否可用，不使用程序外的手机内部存储器，因为需要运行时权限
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(mContext.getFilesDir() + File.separator + "crash" + File.separator);
            if (dir.exists()) {
                FileUtils.deleteDir(dir);
            }
            if (!dir.exists()) {
                dir.mkdir();
            }
            try {
                fileName = dir.toString() + File.separator + getAssignTime("yyyy_MM_dd_HH_mm") + ".txt";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG, "saveInfoToSD: fileName = " + fileName);
        return fileName;
    }



    /**
     * 根据传入的字符串格式化时间
     * @param dateFormatStr 格式化的规范
     * @return 格式化后的时间
     */
    private String getAssignTime(String dateFormatStr) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        long currentTime = System.currentTimeMillis();
        return dateFormat.format(currentTime);
    }

    /**
     * 获取手机信息，版本信息，保存到 haspMap 中
     *
     * @param context apk的上下文
     * @return 存储了相关信息的 haspMap
     */
    private HashMap<String, String> obtainSimpleInfo(Context context) {
        HashMap<String, String> hashMap = new HashMap<>();
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            hashMap.put("versionName", packageInfo.versionName);
            hashMap.put("versionCode", "" + packageInfo.versionCode);
            hashMap.put("MODEL", Build.MODEL);
            hashMap.put("SDK_INT", "" + Build.VERSION.SDK_INT);
            hashMap.put("product", Build.PRODUCT);
            hashMap.put("MOBILE_INFO", getMobileInfo());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    /**
     * 获取手机信息
     * @return 包含手机信息的字符串
     */
    private String getMobileInfo() {
        StringBuilder sb = new StringBuilder();
        try {
                Field[] fields = Build.class.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String name = field.getName();
                    String value = field.get(null).toString();
                    sb.append(name).append(" = ").append(value);
                    sb.append("\n");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        return sb.toString();
    }

    /**
     * 获取尚未捕获的异常
     * @param throwable 出现异常时的throwable
     * @return 异常的字符串
     */
    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
