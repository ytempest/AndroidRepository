package com.ytempest.retrofitanalysis.sample2.test;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description：
 */
public class Test {

    public static void main(String[] args) {
        try {
            Method method = Test.class.getDeclaredMethod("test", int.class, String.class, long.class);
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                System.out.println("[" + i + "] ---> " + annotations);
                for (int j = 0; j < annotations.length; j++) {
                    System.out.println("[" + i + "][" + j + "] --> " + annotations[j]);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public void test(@A int a, @B String string, long c) {

    }
}
