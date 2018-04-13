package com.ytempest.smartknife_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ytempest
 *         Description：
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface LinkView {
    int value();
}
