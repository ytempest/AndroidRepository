package com.ytempest.daydayantis.aspect.net;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * @author ytempest
 *         Description：检测网络功能的切面处理类
 */
@Aspect
public class NetAspect {

    /**
     * CheckNetAspect注解必须是其全路径名 + 注解名称
     */
    @Pointcut("execution(@com.ytempest.daydayantis.aspect.net.CheckNetAspect * *(..))")
    public void checkNetBehavior() {

    }


    /**
     * 标记了 CheckNetAspect注解的处理逻辑
     */
    @Around("checkNetBehavior()")
    public Object checkNetAspect(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1.获取 CheckNet 注解  NDK  图片压缩  C++ 调用Java 方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        CheckNetAspect annotation = methodSignature.getMethod().getAnnotation(CheckNetAspect.class);
        if (annotation != null) {
            // 2.判断有没有网络  怎么样获取 context?
            Context context = getContext(joinPoint.getThis());

            // 3.没有网络不要往下执行
            if (!networkAvailable(context)) {
                Toast.makeText(context, "请检查您的网络...", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        return joinPoint.proceed();
    }


    private Context getContext(Object object) {
        if (object instanceof Activity) {
            return (Activity) object;
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof View) {
            return ((View) object).getContext();
        }
        return null;
    }

    /**
     * 判断当前网络是否可用
     */
    private boolean networkAvailable(Context context) {
        // 获取网络连接管理器对象
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
