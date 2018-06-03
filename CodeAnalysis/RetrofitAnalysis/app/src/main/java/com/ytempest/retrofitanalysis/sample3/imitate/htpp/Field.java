package com.ytempest.retrofitanalysis.sample3.imitate.htpp;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author ytempest
 *         Description：
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Field {
    String value() default "";
}
