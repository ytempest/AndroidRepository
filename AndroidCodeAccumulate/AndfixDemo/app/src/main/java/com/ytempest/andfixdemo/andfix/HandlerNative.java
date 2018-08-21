package com.ytempest.andfixdemo.andfix;

import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description：这个是用于在底层将错误方法替换成正确方法从而进行热修复的类
 */
public class HandlerNative {

    static {
        System.loadLibrary("andfix");
    }

    /**
     * 用于初始化底层的修复逻辑，因为SDK10之前和之后使用的修复逻辑会有所区别
     *
     * @param sdkVersion sdk的版本号
     */
    public static native void init(int sdkVersion);

    /**
     * 在dalvik层将错误的Method替换成正确的Method
     *
     * @param srcMethod    出现bug的Method方法
     * @param targetMethod 修复了bug的Method方法
     */
    public static native void replaceMethod(Method srcMethod, Method targetMethod);
}
