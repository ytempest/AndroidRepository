package com.ytempest.retrofitanalysis.sample3.imitate.htpp;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author ytempest
 *         Description：
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface POST {
    String value() default "";
}
