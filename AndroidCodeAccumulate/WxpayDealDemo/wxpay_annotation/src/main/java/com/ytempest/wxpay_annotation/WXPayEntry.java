package com.ytempest.wxpay_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ytempest
 *         Description：
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface WXPayEntry {
    String packageName();

    Class<?> entryClass();
}
