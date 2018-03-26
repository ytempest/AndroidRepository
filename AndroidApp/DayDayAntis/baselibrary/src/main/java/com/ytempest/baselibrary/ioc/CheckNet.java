package com.ytempest.baselibrary.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ytempest
 *         Description: 该注解用于检测网络状态，然后不联网则会阻止点击事件的产生；
 *         如果要使用该注解，需要将添加权限：android.permission.ACCESS_NETWORK_STATE
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckNet {
}
