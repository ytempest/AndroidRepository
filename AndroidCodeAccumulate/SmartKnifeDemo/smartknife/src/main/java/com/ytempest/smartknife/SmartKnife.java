package com.ytempest.smartknife;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author ytempest
 *         Description：
 */
public class SmartKnife {
    public static UnBinder bind(Activity activity) {
        return getViewBinding(activity);
    }

    public static UnBinder bind(Fragment fragment) {
        return getViewBinding(fragment);
    }


    private static  UnBinder getViewBinding(Object object) {
        try {
            Class<?> bindViewClass = Class.forName(object.getClass().getName() + "_ViewBinding");

            Constructor<?> constructor = bindViewClass.getDeclaredConstructor(object.getClass());

            UnBinder unBinder = (UnBinder) constructor.newInstance(object);

            return unBinder;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
